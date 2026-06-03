Xem Biên Nhận Khách Hàng Đã Thanh Toán (View Receipt for Paid Customer)
Khi khách hàng đã thanh toán hóa đơn, hóa đơn đó sẽ không còn xuất hiện trong danh sách chưa thanh toán (debt_list) và chưa phát hành (to_publish_list) trên server. Vì thế, khi tải lại trang chi tiết của khách hàng đã thanh toán, cả hai danh sách đều trống và currentInvoice sẽ nhận giá trị null, làm ứng dụng không biết invoiceId để gọi API biên nhận (GET /receipt/{id}).

Để giải quyết vấn đề này, chúng ta sẽ lưu trữ cục bộ (local cache) ID của hóa đơn theo mã khách hàng và kỳ hóa đơn (customerCode, ym) -> invoiceId vào Settings khi hóa đơn được tải hoặc khi thanh toán thành công.

Đề xuất thay đổi

1. Lớp Storage cục bộ (Local Storage)
   [MODIFY]
   CredentialsStorage.kt
   Thêm các hàm hỗ trợ lưu và lấy ID hóa đơn:

saveInvoiceId(customerCode: String, ym: String, invoiceId: Long): Lưu ID hóa đơn theo khoá invoice*id*{customerCode}\_{ym}.
getInvoiceId(customerCode: String, ym: String): Long?: Lấy ID hóa đơn đã lưu. 2. Lớp API (Network Layer)
[MODIFY]
TvanApiService.kt
Cập nhật hàm khởi tạo (constructor) để nhận thêm CredentialsStorage.
Trong getToPublishList và getDebtList, sau khi phân tách danh sách hóa đơn từ server, duyệt qua các hóa đơn để tự động lưu ID của chúng vào CredentialsStorage.
[MODIFY]
AppModule.kt
Cập nhật Koin DSL để truyền get() (CredentialsStorage) vào hàm khởi tạo của TvanApiService.
Truyền get() (CredentialsStorage) vào hàm khởi tạo của MeterReadingViewModel. 3. Lớp ViewModel & Presentation
[MODIFY]
MeterReadingViewModel.kt
Thêm hai trạng thái mới:
isTvanPaid: StateFlow<Boolean>: Trạng thái đã thanh toán.
tvanPaidInvoiceId: StateFlow<Long?>: ID hóa đơn đã thanh toán.
Lưu lại loadedCustomerCode và loadedYm khi bắt đầu loadCustomerData.
Trong loadCustomerData, nếu cả inDebt và inToPublish đều null, thực hiện tra cứu trong CredentialsStorage. Nếu tìm thấy cachedId, cập nhật isTvanPaid = true và tvanPaidInvoiceId = cachedId.
Trong payCash(), sau khi thanh toán thành công, lưu ID hóa đơn vào CredentialsStorage, đồng thời cập nhật isTvanPaid = true và tvanPaidInvoiceId = invoiceId.
Cập nhật hàm loadReceipt(id: Long? = null) để ưu tiên lấy từ tvanPaidInvoiceId nếu id là null.
[MODIFY]
MeterReadingScreen.kt
Theo dõi trạng thái isTvanPaid và tvanPaidInvoiceId từ ViewModel.
Tại khu vực các nút bấm TVAN (Submit / TVAN Buttons):
Nếu isTvanPaid == true, hiển thị một hàng gồm hai nút:
Nút bên trái (disabled): hiển thị "Đã Thu Tiền" với màu nền xanh lá nhạt.
Nút bên phải (enabled): hiển thị "Xem Biên Nhận" (màu xanh dương). Khi bấm nút này sẽ kích hoạt viewModel.loadReceipt(tvanPaidInvoiceId) để hiển thị biên nhận.
Kế hoạch kiểm thử (Verification Plan)
Kiểm thử thủ công (Manual Verification)
Khởi chạy ứng dụng với tài khoản luongtd@toctienltd.vn mật khẩu 123123.
Đổi kỳ ghi hoặc tuyến đường về tuyến KH, tìm khách hàng mã 03700005.
Nhập và lưu chỉ số của khách hàng nếu chưa làm (hoặc tải danh sách TVAN).
Thực hiện phát hành hóa đơn TVAN nếu chưa làm.
Thực hiện "Thu tiền" (Pay Cash) để tạo phiếu thu.
Xác nhận: Biên nhận hiển thị thành công.
Đóng biên nhận, thoát ra danh sách khách hàng ngoài và quay trở lại màn hình ghi chỉ số của khách hàng 03700005.
Xác nhận: Màn hình hiển thị trạng thái nút bấm TVAN là "Đã Thu Tiền" và nút "Xem Biên Nhận".
Nhấp vào nút "Xem Biên Nhận" và xác nhận phiếu biên nhận thanh toán hiển thị đúng thông tin của hóa đơn đã thanh toán.
