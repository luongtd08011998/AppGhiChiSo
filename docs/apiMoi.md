Sử dụng API chương trình Quản lý khách hàng cho app Thu Ngan

Nhận danh sách các hoá đơn chưa phát hành sang TVAN theo kỳ hoá đơn và tuyến đường

GET http:/toctienltd.vn/cm-portlet/api/to_publish_list/ym=YYYYMM?rc=CM

Ở đây:
ym là kỳ hoá đơn ở dạng năm/tháng (YYYYMM)
rc là mã tuyến đường  (ví dụ CM = Cái mép)
Cần cung cấp thông tin HEADER cho API. Cụ thể cần cung cấp giá trị cho Authorization là “Basic <chuỗi mã hoá base64>” (chuỗi mã hoá base64 được sinh ra bằng cách lấy username ghép với password và được mã hoá dạng Base64. 
Ví dụ: username là thanhdt@toctienltd.vn password là 123456 thì chuỗi cần mã hoá là “thanhdt@toctienltd.vn:123456”. Sau khi mã hoá, kết quả là “dGhhbmhkdEB0b2N0aWVubHRkLnZuOjEyMzQ1Ng==”

Kết quả: Danh sách các hoá đơn chuẩn bị phát hành sang TVAV, thông tin hoá đơn như sau
Tên trường	Kiểu dữ liệu	Mô tả
id	Long	ID của bản ghi hoá đơn trong DB
invNumber	String	Số hoá đơn (sẽ là rỗng)
type	int	Dạng hoá đơn: Gốc = 1
custCode	String	Mã khách hàng
custName	String	Tên khách hàng
custAddress	String	Địa chỉ khách hàng
totalAmount	double	Tổng số tiền hoá đơn
oldIndex	long	Chỉ số đồng hồ tháng cũ
newIndex	long	Chỉ số đồng hồ tháng hiện tại
empPhone	String	Số điện thoại nhân viên ghi thu


Ví dụ:
curl “http://toctienltd.vn/cm-portlet/api/to_publish_list/ym=202601?rc=CM”

Kết quả trả về:

id=781455
custCode=00700013
custName=Chu Văn Trung
oldIndex=389
newIndex=550
totalMount=3740030.0

id=781456
custCode=00700002
custName=NGÔ MINH TRÍ
oldIndex=2327
newIndex=2360
totalMount=454825.0

Phát hành hoá đơn sang TVAN

POST http://localhost:8080/cm-portlet/api/publish_to_tvan

Ở đây: tham số là cấu trúc JSON , danh sách Id bản ghi hoá đơn
   [781455, 781456]
Tên trường	Kiểu dữ liệu	Mô tả
id	Long	ID bản ghi hoá đơn



Cần cung cấp thông tin HEADER cho API. Cụ thể cần cung cấp giá trị cho Authorization là “Basic <chuỗi mã hoá base64>” (chuỗi mã hoá base64 được sinh ra bằng cách lấy username ghép với password và được mã hoá dạng Base64. 
Ví dụ: username là thanhdt@toctienltd.vn password là 123456 thì chuỗi cần mã hoá là “thanhdt@toctienltd.vn:123456”. Sau khi mã hoá, kết quả là “dGhhbmhkdEB0b2N0aWVubHRkLnZuOjEyMzQ1Ng==”


Kết quả: 
Nếu phát hành thành công sẽ trả về
{
    “retCode”: “ERR_OK”,
    “retMsg”: “Successful”,
    “result”: “x”  // x Số lượng hoá đơn đã phát hành
}
Nếu nếu không thành công, sẽ trả về mã lỗi ở bảng dưới tài liệu.

Ví dụ:
curl -d “[781455, 781456]” -H “Content-Type: application/json” -H ‘Authorization: dGhhbmhkdEB0b2N0aWVubHRkLnZuOjEyMzQ1Ng==’ -X POST 
http://toctienltd.vn/cm-portlet/api/publish_to_tvan

Kết quả là
{
    “retCode”: “ERR_OK”,
    “retMsg”: “Successful”,
    “result”: “2”
}

Nhận danh sách các hoá đơn đã phát hành nhưng chưa thanh toán theo kỳ hoá đơn và tuyến đường

GET http:/toctienltd.vn/cm-portlet/api/debt_list/ym=YYYYMM?rc=CM

Ở đây:
ym là kỳ hoá đơn ở dạng năm/tháng (YYYYMM)
rc là mã tuyến đường  (ví dụ CM = Cái mép)
Cần cung cấp thông tin HEADER cho API. Cụ thể cần cung cấp giá trị cho Authorization là “Basic <chuỗi mã hoá base64>” (chuỗi mã hoá base64 được sinh ra bằng cách lấy username ghép với password và được mã hoá dạng Base64. 
Ví dụ: username là thanhdt@toctienltd.vn password là 123456 thì chuỗi cần mã hoá là “thanhdt@toctienltd.vn:123456”. Sau khi mã hoá, kết quả là “dGhhbmhkdEB0b2N0aWVubHRkLnZuOjEyMzQ1Ng==”

Kết quả: Danh sách các hoá đơn đã phát hành nhưng chưa thanh toán
Tên trường	Kiểu dữ liệu	Mô tả
id	Long	ID của bản ghi hoá đơn trong DB
invNumber	String	Số hoá đơn
type	int	Dạng hoá đơn: Gốc = 1
custCode	String	Mã khách hàng
custName	String	Tên khách hàng
custAddress	String	Địa chỉ khách hàng
totalAmount	double	Tổng số tiền hoá đơn
oldIndex	long	Chỉ số đồng hồ tháng cũ
newIndex	long	Chỉ số đồng hồ tháng hiện tại
empPhone	String	Số điện thoại nhân viên ghi thu


Ví dụ:
curl “http://toctienltd.vn/cm-portlet/api/debt_list/ym=202601?rc=CM”

Kết quả trả về:

id=781455
custCode=00700013
custName=Chu Văn Trung
oldIndex=389
newIndex=550
totalMount=3740030.0

id=781456
custCode=00700002
custName=NGÔ MINH TRÍ
oldIndex=2327
newIndex=2360
totalMount=454825.0

Lấy thông tin để in giấy báo tiền nước

Thông tin trả về ở trên, có thể tạo ra GIẤY BÁO TIỀN NƯỚC, bao gồm Tên khách hàng, Địa chỉ, Mã Khách hàng, Kỳ thanh toán, Chỉ số mới, Chỉ số cũ, Khối lượng tiêu thụ (m3) (= Chỉ số mới - Chỉ số cũ), Điện thoại nhân viên ghi thu.
 
Lập phiếu thu tiền mặt trực tiếp tại nhà khách hàng

POST http://localhost:8080/cm-portlet/api/pay_cash

Ở đây: tham số là cấu trúc JSON
Tên trường	Kiểu dữ liệu	Mô tả
id	Long	ID của bản ghi hoá đơn trong DB



Cần cung cấp thông tin HEADER cho API. Cụ thể cần cung cấp giá trị cho Authorization là “Basic <chuỗi mã hoá base64>” (chuỗi mã hoá base64 được sinh ra bằng cách lấy username ghép với password và được mã hoá dạng Base64. 
Ví dụ: username là loanttb@toctienltd.vn password là 123123 thì chuỗi cần mã hoá là “loanttb@toctienltd.vn:123123”. 


Kết quả: 
Nếu thành công
{"retCode":"ERR_OK",
"retMsg":"Successfull",
"result":"x"
}
Nếu có lỗi, xem bảng lỗi ở dưới.

Ví dụ:
curl -d "781457" -H "Content-Type: application/json" -H 'Authorization: dGhhbmhkdEB0b2N0aWVubHRkLnZuOjEyMzQ1Ng==' -X POST http://toctienltd.vn/cm-portlet/api/pay_cash

Kết quả trả về: 1
{"retCode":"ERR_OK",
"retMsg":"Successfull",
"result":"2618236"  // số phiếu thu
}



Lấy thông tin Giấy biên nhận

GET http:/toctienltd.vn/cm-portlet/api/receipt/{id}

Ở đây:
id là ID bản ghi hoá đơn trong DB
Cần cung cấp thông tin HEADER cho API. Cụ thể cần cung cấp giá trị cho Authorization là “Basic <chuỗi mã hoá base64>” (chuỗi mã hoá base64 được sinh ra bằng cách lấy username ghép với password và được mã hoá dạng Base64. 
Ví dụ: username là thanhdt@toctienltd.vn password là 123456 thì chuỗi cần mã hoá là “thanhdt@toctienltd.vn:123456”. Sau khi mã hoá, kết quả là “dGhhbmhkdEB0b2N0aWVubHRkLnZuOjEyMzQ1Ng==”

Kết quả: Thông tin Giấy biên nhận như sau

Tên trường	Kiểu dữ liệu	Mô tả
id	Long	ID của bản ghi hoá đơn trong DB
invNumber	String	Số hoá đơn
custCode	String	Mã khách hàng
custName	String	Tên khách hàng
custAddress	String	Địa chỉ khách hàng
custTaxCode	String	Mã số thuế khách hàng
numOfHouseHold	int	Số hộ sử dụng
timeToUsedFrom	String	Thời gian sử dụng từ ngày tháng năm
timeToUsedTo	String	Thời gian sử dụng đến ngày tháng năm
period	String	Kỳ hoá đơn
oldIndex	long	Chỉ số đồng hồ tháng cũ
newIndex	long	Chỉ số đồng hồ tháng hiện tại
volumn0	String	Mức sử dụng 1
volumn1	String	Mức sử dụng 2
volumn2	String	Mức sử dụng 3
volumn3	String	Mức sử dụng 4
price0	String	Đơn giá 1
price1	String	Đơn giá 2
price2	String	Đơn giá 3
price3	String	Đơn giá 4
amount0	String	Thành tiền 1
amount1	String	Thành tiền 2
amount2	String	Thành tiền 3
amount3	String	Thành tiền 4
amount	String	Tiền nước tính thuế
taxFee	String	Thuế GTGT (5%)
envFee	String	Phí BVMT (10%)
totalAmount	String	Tổng số tiền thanh toán
totalAmountInWord	String	Bằng chữ (Tổng tiền thanh toán)
lookupCode	String	Mã tra cứu
paymentLineNum	String	Số phiếu thu
paymentLineDate	String	Ngày tạo phiếu thu (YYYYMMDD)

Ví dụ:
curl "http://toctienltd.vn/cm-portlet/api/receipt/781457"

Kết quả trả về:
invNumber=00002487
custCode=00700003
custName=NGUYỄN THỊ THƯƠNG
custAddress=KCN Cái Mép, Phường Tân Phước, TP Hồ Chí Minh
custTaxCode=null
numOfHouseHold=1
timeToUsedFrom=01/01/2026
timeToUsedTo=31/01/2026
period=01/2026
oldIndex=1104
newIndex=1133

Vol0=10
Pr0=9.400
Amn0=94.000
Vol1=10
Pr1=12.600
Amn1=126.000
Vol2=9
Pr2=13.500
Amn2=121.500
Vol3=null
Pr3=null
Amn3=null

amount=341.500
taxFee=17.075
envFee=34.150
totalAmount=392.725
totalAmountInWord=Ba  trăm chín mươi hai ngàn bảy trăm hai mươi lăm  đồng
LookupCode=02601529981




Bảng mã lỗi

Khi thiếu hoặc sai các thông tin, API sẽ trả về Mã lỗi và thông báo lỗi sau.



