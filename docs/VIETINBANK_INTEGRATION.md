# Tài liệu tích hợp Thu hộ định danh VietQR – VietinBank

> Dựa trên đặc tả kỹ thuật `THVQR_API_SPEC_2026 v1.0` (ban hành 25/03/2026) do VietinBank gửi.
> Ngày soạn: 18/06/2026 · Mục đích: handoff team BE + mang đi gặp VietinBank để chốt nghiệp vụ.

---

## 0. Tổng quan vai trò

| Bên | Trách nhiệm |
|-----|-------------|
| **Mobile app** (dự án này) | Sinh QR trên biên nhận in giấy; kiểm tra/hiển thị trạng thái thanh toán từ BE |
| **BE** (công ty nước) | Triển khai **2 endpoint** cho VTB gọi vào + đối soát FTP/SFTP + ký số RSA + đồng bộ trạng thái với thu tiền mặt |
| **VietinBank (VTB)** | Trung gian thu tiền, hạch toán, gọi webhook, đẩy file đối soát |

> ⚠️ Cả 2 API (`inq-bill`, `notify-trans`) đều là **inbound (VTB gọi vào BE)**. BE KHÔNG chủ động gọi ra VTB (trừ việc pull/push file đối soát qua FTP/SFTP).

---

# PHẦN A — HANDOFF CHO TEAM BE

## A1. Kiến trúc & luồng end-to-end

```
Khách quét QR trên biên nhận ──chuyển tiền──► VietinBank ──hạch toán──► TK chính công ty nước
                                                  │
                                  ┌───────────────┴─────────────────┐
                                  ▼ (cần hỏi nợ)                    ▼ (KH đã trả)
                          POST /api/v1/inq-bill (1100)     POST /api/v1/notify-bill (1200)
                                  │                                │
                                  ▼                                ▼
                          BE trả 1110 (số tiền)           BE verify + GẠCH NỢ → trả 1210
                                                                   │
                          ──── FTP/SFTP: đối soát T+1 ◄───────────┘
```

Ngoài ra, **kênh thu tiền mặt** (NV bấm trên app) cũng cập nhật cùng trạng thái hóa đơn với notify-trans → cần đồng bộ (xem A10).

## A2. Quyết định mô hình: ĐỀ XUẤT ĐỘNG (`transType=2`)

Tài liệu hỗ trợ cả **tĩnh** (`transType=3`, TKĐD riêng/KH) và **động** (`transType=2`, parse `remark`).

**Đề xuất mô hình ĐỘNG** vì:
- App mobile hiện tại đã dùng **1 STK chung** (`113601145666`) + `addInfo` phân biệt KH → khớp tinh thần mô hình động.
- Không cần xin VTB cấp TKĐD cho từng KH → triển khai nhanh, dễ mở rộng.

> Nếu VTB chốt **tĩnh**: BE cần thêm bảng mapping `custCode ↔ recvVirtualAcctId`; mobile sửa `VietQrBuilder.buildVietQrUrl()` nhận `accountNo` theo từng KH thay vì hardcode 1 STK.

### A2-bis. Phương án MÔ HÌNH TĨNH (`transType=3`) — nếu VTB chốt

Nếu VTB chọn tĩnh (mỗi KH 1 TKĐD riêng `recvVirtualAcctId`):

**Mobile — sửa `buildVietQrUrl`:**
```kotlin
fun buildVietQrUrl(
    amount: Double, custCode: String,
    virtualAccountNo: String,   // TKĐD riêng KH, lấy từ BE
    yearMonth: String, invNumber: String
): String {
    val bankId = "970415"
    val accountNo = virtualAccountNo   // thay vì hardcode "113601145666"
    // ...phần còn lại giữ nguyên
}
```
→ Model `Customer`/`Invoice` thêm field `virtualAccountNo`; BE trả kèm khi lấy DS hóa đơn.

**BE:**
- Thêm bảng `customers.recv_virtual_acct_id` (mapping `custCode ↔ recvVirtualAcctId`).
- Nhận dữ liệu TKĐD từ VTB qua **file batch** hoặc **API mở TKĐD** (chốt B4).
- Khi nhận `notify-trans`: nhận diện KH qua `recvVirtualAcctId` (chính) thay vì parse `remark`.

**Quy trình in biên nhận:**
- TKĐD cố định/KH → có thể in 1 QR cố định trên thẻ KH (dùng nhiều kỳ).
- `amount` đổi theo kỳ → đề xuất **QR có amount** (sinh mỗi kỳ) để KH không nhập sai số tiền.

**Rủi ro trùng:** vẫn cần `overpayments` + idempotent (KH chuyển nhiều lần vào cùng TKĐD). Bớt được rủi ro parse sai nội dung.

**Câu hỏi bổ sung cho VTB (nếu tĩnh):**
- Cấp TKĐD qua **file batch** hay **API mở TKĐD**? Tần suất khi có KH mới?
- 1 KH có **1 TKĐD cố định** (dùng mọi kỳ) hay đổi theo kỳ?
- Cấu trúc `recvVirtualAcctId`?

## A3. API 1 — `inq-bill` (VTB gọi BE để hỏi thông tin nợ)

| Mục | Giá trị |
|-----|---------|
| URL | `POST https://<be-domain>/api/v1/inq-bill` |
| Content-Type | `application/json; charset=utf-8` |
| Auth | Verify `header.signature` bằng **public key VTB** + whitelist IP VTB |
| Mục đích | VTB hỏi: "KH `custCode` này nợ bao nhiêu, kỳ nào?" |

**Request (1100) — trường chính:**
```json
{
  "header": {
    "msgId": "...", "msgType": "1100", "channelId": "211701",
    "providerId": "...", "merchantId": "...", "productId": "900000",
    "timestamp": "yyyyMMddhhmiss", "signature": "<base64 RSA>", "encrypt": ""
  },
  "data": {
    "transId": "...", "transTime": "...", "transType": "...",
    "custCode": "<M — mã KH nước>",
    "custName": "", "custAcct": ""
  }
}
```
**Chuỗi ký số request:** `data.transId + channelId + transTime + transType + custCode + custName + custAcct` (bỏ trường rỗng, **không** thêm ký tự ngăn cách)

**Response (1110):**
```json
{
  "header": {
    "msgType": "1110", "timestamp": "...",
    "signature": "<base64 RSA, ký bởi BE>"
  },
  "data": {
    "errors": { "errorCode": "00", "errorDesc": "Successful" },
    "details": {
      "transId": "...", "transTime": "...", "custCode": "...",
      "custName": "<TênCty>_<custCode>_<amount>VND",
      "amount": "500000", "billId": "...", "billTerm": "<kỳ>",
      "idxStart": "<chỉ số đầu>", "idxEnd": "<chỉ số cuối>",
      "consume": "<lượng tiêu thụ>", "vat": "...",
      "billInfo1": [ { "...nhiều kỳ nếu có..." } ]
    }
  }
}
```
**Chuỗi ký số response:** `details.transId + channelId + transTime + transType + custCode + custName + billId + billTerm + amount + errors.errorCode`

## A4. API 2 — `notify-bill` (VTB báo KH đã trả → BE gạch nợ) ⭐ chính

| Mục | Giá trị |
|-----|---------|
| URL | `POST https://<be-domain>/api/v1/notify-bill` |
| Mục đích | VTB báo giao dịch thanh toán → BE ghi nhận + gạch nợ hóa đơn |

> Lưu ý: tài liệu ghi URL là `notify-bill`, tên bản tin là `notify-trans`.

**Request (1200) — trường chính:**
```json
{
  "msgId": "...", "providerId": "...",
  "transId": "<M>", "transTime": "<M>", "transType": "<M>",
  "custCode": "<mã KH>",
  "sendBankId": "", "sendAcctId": "", "sendAcctName": "",
  "recvAcctId": "", "recvVirtualAcctId": "", "recvVirtualAcctName": "",
  "amount": "<M>", "bankTransId": "<số chứng từ>",
  "remark": "<M — nội dung CK, chứa custCode/kỳ>",
  "currencyCode": "VND",
  "signature": "<M>"
}
```
**Chuỗi ký số request:** `transId + transTime + custCode + amount + bankTransId + remark`

**Response (1210):**
```json
{
  "transId": "...", "providerId": "...",
  "errorCode": "00", "errorDesc": "Thanh cong",
  "signature": "<ký bởi BE>"
}
```
**Chuỗi ký số response:** `transId + errorCode + errorDesc`

**Logic xử lý BE khi nhận `notify-trans` (5 bước):**
1. **Verify** `signature` bằng public key VTB → sai trả `01`.
2. Kiểm tra **trùng `transId`** → đã xử lý rồi trả `05`.
3. Parse `custCode` (từ field, hoặc từ `remark` nếu mô hình động) → tìm hóa đơn theo `custCode + billTerm` → không có trả `02`.
4. **Gạch nợ** qua service `recordPayment(...)` (xem A10):
   - Chưa PAID → mark `PAID`, `paidSource=QR`, lưu `bankTransId` → trả `00`.
   - Đã PAID (KH trả mặt/trùng) → ghi `overpayments`, **vẫn trả `00`** (tiền đã nhận) → xử lý hoàn tiền nội bộ.
5. **Sign** response bằng private key BE → trả `1210`. Lỗi khác → `99`.

## A5. Bảo mật — Ký số RSA-SHA256 (bắt buộc mọi bản tin)

- Thuật toán: **SHA256withRSA**, key **2048-bit**, public key dạng `.cer`.
- Trao đổi public key **1 lần**: BE gửi `.cer` cho VTB, VTB gửi `.cer` cho BE.
- **Private key BE** chỉ lưu trên server (vault/KMS) — không bao giờ trên mobile.

**Thuật toán sign/verify (pseudo — áp dụng cho mọi stack):**
```text
# Bên gửi (sign)
signData = concatenation của các trường theo thứ tự đặc tả (bỏ trường rỗng, KHÔNG thêm ký tự ngăn cách)
signature = Base64( RSA_SHA256_Sign(privateKey, UTF8(signData)) )

# Bên nhận (verify)
ok = RSA_SHA256_Verify(publicKey, UTF8(signData), Base64Decode(signature))
```
Ví dụ `signData` thực tế (từ mẫu VTB, bản tin 1200):
```
501690869202402011406342NDVNDV24012358711875800164T24200GKAJ7BYCT DEN:164T24200GKAJ7BY CK
```

## A6. Đối soát FTP/SFTP (hàng ngày T+1)

- Tần suất: **hàng ngày** (kể cả lễ/Tết). VTB đẩy file ~7h, BE phản hồi **trước 15h** ngày T+1.
- Thư mục: `VIETINBANK_IN` (VTB upload), `VIETINBANK_OUT` (BE phản hồi), `VIETINBANK_BACKUP`.
- File: `YYYYMMDD_{PROVIDERID}_THUHOTKAO_IN.txt` / `..._OUT.txt`.
  - Định dạng txt: dòng `002` = detail, `003` = pending bổ sung, `009` = footer/kết thúc.
- **Key đối soát chính:** `TransId + amount + transTime`.
- **`reconcileStatus`** (BE ghi vào file OUT):
  - `00` cân khớp · `01` VTB có/BE không · `02` ngược lại · `03` sai lệch dữ liệu.
- **Toàn vẹn file:**
  - `recordChecksum` = `MD5(nối dữ liệu từng dòng + private_key_đối_soát)`, **không tính dấu `|`**.
  - `fileChecksum` = `MD5(toàn bộ dữ liệu file)`.

## A7. DB schema đề xuất (tối thiểu)

```
customers(id, cust_code UNIQUE, name, recv_virtual_acct_id?, ...)
invoices(id, cust_code FK, bill_id, bill_term, period,
         idx_start, idx_end, amount, status[PENDING/PAID],
         paid_source[CASH/QR/BANK_TRANSFER], paid_trans_id, paid_at)
payments(trans_id PK, provider_id, cust_code, amount, bank_trans_id,
         remark, trans_time, trans_type, source, raw_payload, created_at)
overpayments(id, trans_id, cust_code, bill_term, amount, reason[DUPLICATE/WRONG_PERIOD],
             refund_status, created_at)
reconciliations(id, recon_date, provider_id, vtb_count, be_count,
                match_status, file_name, processed_at)
vtb_keys(env, public_key_cer, valid_from, valid_to)
config(env, main_url, client_id, client_secret, provider_id,
       merchant_id, product_id, ftp_host, ftp_user, ftp_pass, recon_private_key)
```

## A8. Bảng mã lỗi BE phải trả đúng

| Mã | Khi nào trả |
|----|-------------|
| `00` | Xử lý thành công |
| `01` | Không xác nhận được chữ ký số |
| `02` | Mã KH / hóa đơn không tồn tại |
| `03` | Lỗi khi gạch nợ |
| `05` | Trùng giao dịch (`transId`) |
| `06` | Không có tài khoản |
| `99` | Lỗi không xác định |

## A9. Checklist triển khai BE
- [ ] 2 endpoint `/api/v1/inq-bill`, `/api/v1/notify-bill` + verify signature + sign response
- [ ] Lưu trữ & luân chuyển private key an toàn (vault/KMS)
- [ ] Whitelist IP VTB cho inbound (cả UAT + prod)
- [ ] Idempotency theo `transId` (trả `05` khi trùng transId) **và** theo `custCode + billTerm` (xử lý trùng/thừa — A10)
- [ ] Job đối soát FTP/SFTP hàng ngày (parse `IN` → sinh `OUT` với `reconcileStatus`)
- [ ] API nội bộ cho mobile: kiểm tra trạng thái hóa đơn + ghi nhận thu tiền mặt (`payCash`)
- [ ] Log raw payload mọi bản tin (audit)
- [ ] Môi trường UAT song song prod

## A10. Đồng bộ 3 kênh thanh toán & xử lý trùng/thừa ⭐ (then chốt nghiệp vụ)

**Quy trình thực tế:** NV in biên nhận giấy có QR đưa cho KH. **Quy tắc nghiệp vụ: nếu đã thu tiền mặt thì KHÔNG phát biên nhận có QR nữa** → 2 kênh (a) và (b) loại trừ nhau tại thời điểm phát biên nhận.

**3 kênh thanh toán:**
- (a) KH trả **tiền mặt** cho NV → NV bấm "thu tiền mặt" (API `payCash` nội bộ) → **không phát biên nhận có QR** (chỉ in phiếu thu mặt).
- (b) KH chưa trả mặt / chọn chuyển khoản → NV phát **biên nhận có QR** → KH quét → VTB `notify-trans`.
- (c) KH **chuyển khoản không qua QR** (chuyển thẳng STK) → NV đối soát sổ thủ công.

**2 nguồn mark PAID:** NV trên app **và** VTB `notify-trans`.

> ✅ Quy tắc "thu mặt thì không phát QR" **loại bỏ rủi ro "KH trả mặt xong rồi quét QR"**.
> Rủi ro trùng còn lại (xử lý ở BE + mobile):
> - KH **quét QR nhiều lần** (giữ biên nhận, screenshot, quét lại sau) → trả thừa.
> - KH **quét QR xong nhưng NV chưa đồng bộ** → NV cần check status trước khi thu mặt.
> - Kênh **(c) sai cú pháp** → VTB không notify → đối soát sổ trễ.

⇒ **BE = single source of truth.** Service chung:

```
recordPayment(custCode, billTerm, amount, source, transId):
  invoice = find(custCode, billTerm)
  if not invoice: return NOT_FOUND        // trả 02 cho VTB
  if invoice.paid_trans_id == transId: return ALREADY_PROCESSED  // idempotent
  if invoice.status == PAID:
      save overpayment(custCode, billTerm, amount, source, transId, reason=DUPLICATE)
      return OK                             // vẫn trả 00 cho VTB (tiền đã nhận)
  invoice.status = PAID
  invoice.paid_source = source              // CASH | QR | BANK_TRANSFER
  invoice.paid_trans_id = transId
  return OK
```

**Mobile NV (dự án này):**
- Trước khi bấm "thu tiền mặt" → call BE kiểm tra `status` → nếu `PAID` (KH quét QR/chuyển khoản trước) → **block + cảnh báo**, tránh thu trùng.
- Khi hóa đơn `PAID` → UI hiển thị "Đã thanh toán" + `bankTransId` (nếu có).

**Định hướng nghiệp vụ:** Khuyến khích KH dùng **kênh (b) QR** để tự động. Kênh (c) chỉ là backup — nếu KH chuyển thẳng STK mà nội dung **sai cú pháp** → VTB **không parse được** → **không notify** → BE chỉ phát hiện qua đối soát sổ (trễ). Cần chốt với VTB (xem B15).

### A10-bis. So sánh khả năng CHẶN TRÙNG theo mô hình ⭐

| Mô hình | Chặn trước ở ngân hàng | Xử lý sau (BE) |
|---|---|---|
| **Động** (STK chung `transType=2`) | ❌ **Không thể** — STK thường nhận mọi giao dịch | ✅ idempotent + hoàn tiền (A11) |
| **Tĩnh** (theo tài liệu, `transType=3`) | ❌ Không — TKĐD chưa có chế độ khóa | ✅ idempotent + hoàn tiền |
| **Tĩnh + TKĐD one-time/amount-locked** (B18) | ✅ **Có** — KH quét lại bị từ chối tận gốc | ít cần |

> 🔑 **Kết luận:** Muốn **chặn trước** (KH quét trùng bị lỗi ngay) → bắt buộc dùng **tĩnh + TKĐD one-time** (phải hỏi VTB, B18). Mô hình **động chỉ xử lý sau** (nhận trùng → hoàn tiền).

## A11. Quy trình HOÀN TIỀN khi KH chuyển trùng/thừa ⭐

> ⚠️ **Bản API này KHÔNG có API hoàn tiền.** Hoàn tiền = nghiệp vụ thủ công qua kế toán + VietinBank.

**Cơ chế (theo tài liệu mục 3.5):** Tiền đã vào TK chính công ty → **công ty lập Ủy nhiệm chi (UNC) gửi VTB** → VTB trích TK công ty → hoàn về TK KH.

**Thông tin cần để hoàn = lấy từ `notify-trans`:**

| Trường `notify-trans` | Ý nghĩa | Lưu ý |
|---|---|---|
| `sendAcctId` | Số TK của KH (người gửi) | ⚠️ **Mặc định RỖNG** — phải yêu cầu VTB bật (B17) |
| `sendAcctName` | Tên KH | ⚠️ Mặc định rỗng |
| `sendBankId` | Mã NH của KH | ⚠️ Mặc định rỗng |

→ **Nếu VTB không trả `sendAcctId`, công ty KHÔNG biết hoàn cho ai.**

**Quy trình 5 bước:**
1. BE phát hiện trùng qua `recordPayment` → đẩy `overpayments` + **lưu `sendAcctId`/`sendAcctName`**.
2. Kế toán review `overpayments`, xác nhận đúng là trùng/thừa.
3. Lập **Ủy nhiệm chi** (dùng TK KH ở bước 1) → gửi VTB.
4. VTB trích TK công ty → chuyển về TK KH.
5. BE cập nhật `overpayments.refund_status = DONE`.

**Khó khăn thực tế:**
- Thủ công, chậm → tốn công kế toán nếu KH quét trùng nhiều.
- `sendAcctId` có thể rỗng → phải xin VTB bật.
- **Kênh (c) chuyển thẳng không notify** → không có `sendAcctId` → **không biết hoàn cho ai** → phải liên hệ trực tiếp KH (rất khó) → lý do nữa để định hướng KH dùng QR.
- KH chuyển từ TK không đúng tên → VTB có thể từ chối hoàn (chống rửa tiền).

**Mobile (dự án này):** không trực tiếp hoàn tiền. Khi phát hiện trùng, app NV hiển thị cảnh báo:
> "⚠️ KH đã thanh toán, có giao dịch thừa {amount}. Chuyển kế toán xử lý hoàn tiền."

## A12. Trải nghiệm KH khi quét QR (làm rõ cho team/VTB)

QR (cả động lẫn tĩnh) **nhúng sẵn** mọi thông tin → KH **không phải nhập tay** TK/tiền/nội dung:

| Trường trong QR | Ai điền khi quét? |
|---|---|
| Số TK nhận (STK chung hoặc **TKĐD**) | ✅ App ngân hàng đọc từ QR |
| Số tiền (`amount`) | ✅ Đọc từ QR (nếu QR có amount) |
| Nội dung (`addInfo`/`remark`) | ✅ Đọc từ QR |

→ KH chỉ **quét → xác nhận (PIN/sinh trắc)**. Hàm `buildVietQrUrl` đã nhúng đủ `amount + addInfo`.

**3 trường hợp KH mới phải nhập tay:**
- Quét QR **đầy đủ** (cách đang làm): không nhập gì.
- Quét QR **thiếu amount**: nhập số tiền.
- **Chuyển khoản thường** (không quét): nhập STK + tiền + nội dung.

**TKĐD one-time/amount-locked (B18):** QR chứa TKĐD + amount đúng → KH quét → app điền đúng → OK. Nếu KH cố đổi amount tay → ngân hàng **từ chối** → chặn sai số tiền/trùng tận gốc.

## A13. BE KHÔNG cần xử lý toàn bộ trường trong schema ⭐

Tài liệu quy định rất nhiều trường vì nó là **schema tổng quát đa ngành** (điện, nước, học phí, bảo hiểm, chi hộ…). BE **chỉ cần dùng subset cho nghiệp vụ nước**, không phải implement/validate toàn bộ.

**BE chỉ làm 4 việc:**
1. Parse JSON → lấy vài trường cần thiết (bỏ qua phần còn lại).
2. Verify `signature` → chỉ nối đúng các trường trong chuỗi ký số.
3. Xử lý nghiệp vụ (gạch nợ / trả thông tin nợ).
4. Trả response tối thiểu + ký `signature`.

**Phân loại trường (cho `notify-trans` — API chính):**

| Nhóm | Trường | Xử lý |
|---|---|---|
| ✅ Bắt buộc | `signature`, `transId`, `custCode`, `amount`, `bankTransId`, `remark`, `sendAcctId`, `sendAcctName` | Verify / idempotency / tìm hóa đơn / lưu để hoàn tiền |
| 🔁 Echo | `msgId`, `transTime`, `providerId` | Điền lại response / log |
| ⏭️ Bỏ qua | `sendBankId`, `sendBranchId`, `recvAcctId`, `recvVirtualAcctId`, `recvVirtualAcctName`, `currencyCode`(chỉ check `=VND`), `encrypt`, `token`, `clientIP`, `recordNum`, `preseve1-3`, `additionalProperties` | Không cần |

> ~40 trường request → BE thực sự **dùng ~7-8 trường**. Response `1210` chỉ cần 5 trường (`transId`, `providerId`, `errorCode`, `errorDesc`, `signature`).

**Verify signature chỉ nối đúng số trường quy định** (không phải toàn bộ):
- `notify-trans`: `transId + transTime + custCode + amount + bankTransId + remark`
- `inq-bill` request: `data.transId + channelId + transTime + transType + custCode + custName + custAcct`

→ Đọc đúng các trường đó để verify là đủ, **không cần validate trường tùy chọn (`O`)**.

1. **Mô hình thu hộ**: động (`transType=2`, parse `remark`) hay tĩnh (`transType=3`, TKĐD/KH)? *Đề xuất động.*
2. **Cấu trúc `remark` chuẩn** để VTB parse → map `custCode`. Mobile hiện dùng `"CN.TOCTIEN {custCode} {kỳ} {sốHĐ}"` — có chấp nhận không?
3. **Cấu trúc `custCode`**: dùng mã KH của công ty nước hay mã VTB sinh? Định dạng/độ dài?
4. **Cấp TKĐD** (nếu tĩnh): file batch hay API mở TKĐD? Tần suất cập nhật khi có KH mới?
5. **Thông tin kết nối theo môi trường** (UAT + prod): `MAIN_URL`, `client-id`, `client-secret`, `providerId`, `merchantId`, `productId`.
6. **Trao đổi key**: kênh gửi `.cer` (email/portal)? `SHA256` + `2048-bit` đúng chưa? Chu kỳ luân chuyển key?
7. **IP VTB** gọi vào BE (để whitelist) — cả UAT và prod.
8. **FTP/SFTP đối soát**: host, port, user/pass, thư mục, `private_key` đối soát (cho MD5 checksum), giờ.
9. **`custName` hiển thị** có đúng chuẩn `"[Tên công ty]_[custCode]_[amount]VND"` (không dấu)?
10. **Có mã lỗi nghiệp vụ bổ sung** ngoài bảng `00–99` không? Ý nghĩa `06`, `99` cụ thể với nghiệp vụ nước?
11. **UAT/Sandbox**: dữ liệu test, cách KH test thanh toán, thời hạn test.
12. **Retry policy** khi BE trả lỗi / timeout — VTB có retry không, bao nhiêu lần?
13. **`inq-bill` có bắt buộc** không, hay chỉ cần `notify-trans` (vì QR động đã có sẵn `amount`)?
14. **SLA** endpoint BE (timeout tối đa VTB chấp nhận), thời gian cut-off hạch toán hằng ngày.
15. **Kênh (c) chuyển khoản thông thường**: VTB có `notify-trans` cho giao dịch KH chuyển thẳng STK (không qua QR/TKĐD) không? Nếu nội dung **đúng cú pháp** thì parse được không? *(Quyết định kênh (c) có tự động được không — hiện cho là trễ/thủ công.)*
16. **Thanh toán trùng/thừa**: khi 1 `custCode` thanh toán trùng (đã trả mặt rồi mới quét QR), quy trình hoàn tiền thế nào? VTB có hỗ trợ hoàn tiền qua API/file, hay công ty phải lập **Ủy nhiệm chi** thủ công? Có cảnh báo khi 1 `custCode` thanh toán 2 lần trong kỳ không?
17. **Thông tin người gửi (`sendAcctId`/`sendAcctName`/`sendBankId`)** trong `notify-trans`: tài liệu ghi *mặc định trả về rỗng*. **Yêu cầu VTB bật** các trường này — công ty cần số TK của KH để **hoàn tiền** khi trùng/thừa. Việc bật có ảnh hưởng tốc độ notify không?
18. **Chặn trùng tận gốc (rất quan trọng)**: VTB có hỗ trợ cấu hình **TKĐD chế độ nâng cao** không?
    - (a) **Chỉ nhận đúng số tiền** (amount-locked) — KH chuyển sai số tiền bị từ chối?
    - (b) **One-time / đóng sau 1 lần thanh toán** — KH quét lại lần 2 bị từ chối → chặn trùng tận gốc?
    
    *Tài liệu hiện tại (mô hình tĩnh `transType=3`) không có cơ chế khóa → nếu VTB hỗ trợ (a)/(b) thì mới chặn được trùng phía ngân hàng; không thì phải xử lý sau bằng `overpayments` + hoàn tiền (A11).*
19. **BIỂU PHÍ DỊCH VỤ** (tài liệu kỹ thuật không ghi — nằm trong hợp đồng thương mại): xin VTB cung cấp biểu phí so sánh giữa **mô hình động** và **mô hình tĩnh**:
    - Phí giao dịch thu hộ (theo % hay cố định/giao dịch)?
    - Phí cấp/tạo TKĐD (chỉ tĩnh)? Phí duy trì hàng tháng?
    - Phí đối soát/file? Phí hoàn tiền (Ủy nhiệm chi)?
    - Có miễn phí giai đoạn đầu/ưu đãi không?
    *So sánh tổng chi phí giữa 2 mô hình để chọn mô hình tối ưu về cả kỹ thuật lẫn chi phí.*

---

## Phụ lục — Tham chiếu code hiện tại (mobile)
- Sinh QR: [`VietQrBuilder.kt`](../composeApp/src/commonMain/kotlin/com/example/appghichiso/utils/VietQrBuilder.kt) — hàm `buildVietQrUrl(amount, custCode, yearMonth, invNumber)`.
- Hiển thị QR trên biên nhận: [`TvanComponents.kt`](../composeApp/src/commonMain/kotlin/com/example/appghichiso/presentation/reading/TvanComponents.kt).
- BIN VietinBank `970415` (đúng), STK nhận hardcode `113601145666`, `addInfo = "CN.TOCTIEN {custCode} {yearMonth} {invNumber}"`.

> Nếu VTB chốt mô hình **tĩnh**, cần sửa `buildVietQrUrl` nhận `accountNo` (= TKĐD) theo từng KH thay vì hardcode 1 STK.
