hông tin chung
Mục Giá trị
Path
/api/v1/qlkh/month-invoices/readings
Method
GET
Base URL
Theo môi trường (vd https://your-server.com)
Auth
Hiện không bắt buộc — không cần Authorization
API 1 — Một kỳ (yearMonth)
Mục đích: Lấy danh sách chỉ số đồng hồ (đầu/cuối kỳ) cho mọi khách có hóa đơn trong một kỳ YYYYMM.

Request:

GET {baseUrl}/api/v1/qlkh/month-invoices/readings?yearMonth=202501

Query Bắt buộc Định dạng
yearMonth
Có
YYYYMM — đúng 6 chữ số
Không gửi kèm fromYearMonth / toYearMonth.

Response 200 (JSON):

Wrapper ApiResponse: statusCode (200), message, data (mảng).
Mỗi phần tử data[]:
digiCode (String) — mã KH
oldVal (number hoặc null) — chỉ số đầu kỳ
newVal (number hoặc null) — chỉ số cuối kỳ
Ví dụ:

{
"statusCode": 200,
"message": "Lấy danh sách thành công",
"data": [
{ "digiCode": "KH001234", "oldVal": 120, "newVal": 135 },
{ "digiCode": "KH005678", "oldVal": 88, "newVal": 104 }
]
}
API 2 — Khoảng kỳ (fromYearMonth + toYearMonth)
Mục đích: Lấy chỉ số cho mọi khách có hóa đơn trong khoảng kỳ bao gồm hai đầu.

Request:

GET {baseUrl}/api/v1/qlkh/month-invoices/readings?fromYearMonth=202501&toYearMonth=202504

Query Bắt buộc Định dạng
fromYearMonth
Có
YYYYMM
toYearMonth
Có
YYYYMM
Ràng buộc: fromYearMonth ≤ toYearMonth (so sánh chuỗi YYYYMM).

Không gửi kèm yearMonth.

Response 200: Cùng cấu trúc như API 1. Cùng một digiCode có thể lặp nhiều lần (nhiều kỳ / nhiều dòng hóa đơn). Thứ tự do server: yearMonth tăng dần, rồi customerId, rồi id hóa đơn.
