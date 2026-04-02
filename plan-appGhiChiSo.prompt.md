# Plan: Ứng dụng Ghi Chỉ Số Nước — KMP Architecture & WBS

Xây dựng app đa nền tảng (Android/iOS) theo **Clean Architecture + MVVM/MVI** với **Ktor** gọi API, **Koin** DI, **SQLDelight** cache offline, và **Jetbrains Navigation Compose**. Project hiện tại là KMP scaffold sạch, cần bổ sung toàn bộ các lớp từ data → domain → presentation.

---

## 🏗️ Kiến Trúc Tổng Thể

```
┌─────────────────────────────────────────────────────┐
│              Compose UI (commonMain)                 │
│  LoginScreen → RouteListScreen → CustomerListScreen  │
│                              → MeterReadingScreen    │
├─────────────────────────────────────────────────────┤
│           Presentation Layer (ViewModel)             │
│   AuthVM    RouteVM    CustomerVM    ReadingVM       │
├─────────────────────────────────────────────────────┤
│              Domain Layer (Use Cases)                │
│  LoginUC  GetRoutesUC  GetCustomersUC  SubmitReadUC │
├────────────────────────┬────────────────────────────┤
│   Data / Remote        │   Data / Local              │
│   Ktor HTTP Client     │   SQLDelight (cache)        │
│   API Services         │   DataStore (token)         │
└────────────────────────┴────────────────────────────┘
```

---

## 📦 Tech Stack

| Thành phần | Thư viện | Lý do |
|---|---|---|
| UI | Compose Multiplatform 1.10.x | ✅ Đã có |
| Navigation | `jetbrains.navigation.compose` | KMP native |
| HTTP Client | `ktor-client` (OkHttp/Darwin) | KMP standard |
| Serialization | `kotlinx-serialization-json` | Ktor plugin |
| DI | `koin-compose-multiplatform` | KMP, lightweight |
| Local DB | `SQLDelight 2.x` | KMP, offline cache |
| Token storage | `datastore-preferences` KMP | Secure, KMP |
| Async | `kotlinx-coroutines` | ✅ Đã có transitively |
| Image | `coil3-compose` (optional) | Ảnh đồng hồ |

---

## 📁 Cấu Trúc Thư Mục

```
composeApp/src/commonMain/kotlin/com/example/appghichiso/
├── data/
│   ├── api/
│   │   ├── AuthApiService.kt
│   │   ├── RouteApiService.kt
│   │   ├── CustomerApiService.kt
│   │   └── MeterReadingApiService.kt
│   ├── local/
│   │   ├── TokenStorage.kt          # DataStore
│   │   └── db/                      # SQLDelight schema
│   └── repository/
│       ├── AuthRepositoryImpl.kt
│       ├── RouteRepositoryImpl.kt
│       ├── CustomerRepositoryImpl.kt
│       └── MeterReadingRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Route.kt
│   │   ├── Customer.kt
│   │   └── MeterReading.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── RouteRepository.kt
│   │   ├── CustomerRepository.kt
│   │   └── MeterReadingRepository.kt
│   └── usecase/
│       ├── LoginUseCase.kt
│       ├── GetRoutesUseCase.kt
│       ├── GetCustomersByRouteUseCase.kt
│       └── SubmitMeterReadingUseCase.kt
├── presentation/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── AuthViewModel.kt
│   ├── route/
│   │   ├── RouteListScreen.kt
│   │   └── RouteViewModel.kt
│   ├── customer/
│   │   ├── CustomerListScreen.kt
│   │   └── CustomerViewModel.kt
│   ├── reading/
│   │   ├── MeterReadingScreen.kt
│   │   └── MeterReadingViewModel.kt
│   └── common/
│       ├── LoadingIndicator.kt
│       ├── ErrorView.kt
│       └── UiState.kt
├── navigation/
│   ├── AppNavGraph.kt
│   └── Screen.kt
└── di/
    ├── NetworkModule.kt
    ├── StorageModule.kt
    ├── RepositoryModule.kt
    ├── UseCaseModule.kt
    └── ViewModelModule.kt
```

---

## 📋 WBS (Work Breakdown Structure)

### 1. Project Setup & Dependencies
- 1.1 Thêm Ktor, Koin, SQLDelight, Navigation, Serialization, DataStore vào `libs.versions.toml`
- 1.2 Cập nhật `composeApp/build.gradle.kts` với tất cả dependencies
- 1.3 Tạo cấu trúc thư mục Clean Architecture
- 1.4 Cấu hình SQLDelight plugin và schema
- 1.5 Cấu hình Kotlin Serialization plugin

### 2. Lớp Data — Remote
- 2.1 Cấu hình Ktor HttpClient (base URL, headers, logging, timeout)
- 2.2 Cài đặt auth interceptor (đính kèm JWT Bearer token vào request)
- 2.3 `AuthApiService` — `POST /auth/login` → trả về token + thông tin nhân viên
- 2.4 `RouteApiService` — `GET /routes` → danh sách tuyến được giao cho nhân viên
- 2.5 `CustomerApiService` — `GET /routes/{routeId}/customers` → danh sách khách hàng
- 2.6 `MeterReadingApiService` — `POST /readings` → gửi chỉ số mới

### 3. Lớp Data — Local & Storage
- 3.1 `TokenStorage` với DataStore — lưu/đọc/xóa JWT token
- 3.2 SQLDelight schema: bảng `routes`, `customers`, `last_readings`
- 3.3 Implement SQLDelight queries: insert/select/delete
- 3.4 Repository implementations với logic cache (remote → local fallback)

### 4. Lớp Domain
- 4.1 Định nghĩa domain models: `User`, `Route`, `Customer`, `MeterReading`
- 4.2 Định nghĩa repository interfaces (abstraction)
- 4.3 `LoginUseCase` — xác thực, lưu token, trả về User
- 4.4 `GetRoutesUseCase` — lấy danh sách tuyến (cache-aware)
- 4.5 `GetCustomersByRouteUseCase` — lấy danh sách khách hàng theo tuyến
- 4.6 `SubmitMeterReadingUseCase` — validate chỉ số (mới ≥ cũ), submit API

### 5. Lớp Presentation — UI & ViewModel

#### 5.1 Common UI
- `UiState<T>` sealed class: `Loading | Success(data) | Error(message)`
- `LoadingIndicator` composable
- `ErrorView` composable với nút Retry

#### 5.2 LoginScreen
- Form nhập username / password
- Nút đăng nhập với loading state
- Hiển thị lỗi xác thực (sai mật khẩu, network error)
- `AuthViewModel` xử lý login flow

#### 5.3 RouteListScreen
- Danh sách tuyến dạng Card (tên tuyến, số khách hàng)
- Pull-to-refresh
- Trạng thái loading / error / empty
- `RouteViewModel` gọi `GetRoutesUseCase`

#### 5.4 CustomerListScreen
- Danh sách khách hàng theo tuyến đã chọn
- Tìm kiếm/lọc theo tên hoặc mã khách hàng
- Hiển thị trạng thái đã ghi / chưa ghi trong tháng
- `CustomerViewModel` gọi `GetCustomersByRouteUseCase`

#### 5.5 MeterReadingScreen
- Hiển thị thông tin khách hàng (tên, địa chỉ, mã đồng hồ)
- Chỉ số kỳ trước (readonly, từ API/cache)
- Ô nhập chỉ số kỳ này (numeric keyboard)
- Tự động tính lượng tiêu thụ = chỉ số mới − chỉ số cũ
- Nút chụp ảnh đồng hồ (optional)
- Nút Lưu / Submit với confirmation dialog
- `MeterReadingViewModel` gọi `SubmitMeterReadingUseCase`

### 6. Navigation
- 6.1 Định nghĩa `sealed class Screen`: `Login`, `RouteList`, `CustomerList(routeId)`, `MeterReading(customerId)`
- 6.2 `AppNavGraph` — NavHost kết nối tất cả màn hình
- 6.3 Auth guard — kiểm tra token khi khởi động, redirect về Login nếu hết hạn
- 6.4 Xử lý back stack đúng (CustomerList back về RouteList, không back về Login)

### 7. Dependency Injection (Koin)
- 7.1 `NetworkModule` — HttpClient, API services
- 7.2 `StorageModule` — TokenStorage, SQLDelight driver
- 7.3 `RepositoryModule` — Repository implementations
- 7.4 `UseCaseModule` — Use cases
- 7.5 `ViewModelModule` — ViewModels
- 7.6 Khởi tạo Koin trong `androidMain` (`Application.onCreate`) và `iosMain` (`MainViewController`)

### 8. Platform-Specific (expect/actual)
- 8.1 SQLDelight driver: `AndroidSqliteDriver` vs `NativeSqliteDriver`
- 8.2 DataStore path: Android Context vs iOS NSDocumentDirectory
- 8.3 (Optional) Camera: Android CameraX vs iOS UIImagePickerController

### 9. Testing
- 9.1 Unit test Use Cases với mock repositories
- 9.2 Unit test ViewModels với Turbine (Flow testing)
- 9.3 Integration test API services với MockEngine (Ktor)

### 10. Build & Release
- 10.1 Cấu hình build flavors (dev/staging/prod) với base URL khác nhau
- 10.2 Android: ký APK/AAB, ProGuard rules cho Ktor/Koin
- 10.3 iOS: cấu hình Xcode scheme, export IPA

---

## 🔄 Luồng Người Dùng (User Flow)

```
App Start
   │
   ├─ Token tồn tại & hợp lệ ──→ RouteListScreen
   │
   └─ Không có token ──→ LoginScreen
                              │
                         Đăng nhập thành công
                              │
                         RouteListScreen
                         (Danh sách tuyến)
                              │
                         Chọn tuyến
                              │
                         CustomerListScreen
                         (Danh sách khách hàng)
                              │
                         Chọn khách hàng
                              │
                         MeterReadingScreen
                         (Nhập & gửi chỉ số)
                              │
                         Submit thành công
                              │
                         Back → CustomerListScreen
                         (đánh dấu đã ghi ✅)
```

---

## 🌐 API Thực Tế (Liferay JSON-WS)

**Base URL:** `http://qlkh.toctienltd.vn`

**Authentication:** `Basic Auth` — Header `Authorization: Basic <base64(email:password)>`
> Ví dụ: `vananh@toctienltd.vn:123456` → `dmFuYW5oQHRvY3RpZW5sdGQudm46MTIzNDU2`
> Tất cả API đều cần gửi header này.

---

### GET /api/jsonws/cm-portlet.api/get-roads
Trả về danh sách tuyến đường được giao.
```json
{
  "data": [
    { "code": "DN1-MINH", "name": "DOANH NGHIỆP 1- MINH" }
  ],
  "status": { "code": "success", "message": "Success" }
}
```

### GET /api/jsonws/cm-portlet.api/get-customers-by-road/road-code/{roadCode}/year/{year}/month/{month}
Trả về danh sách khách hàng theo tuyến, năm, tháng.
```json
{
  "data": [{
    "contractCode": "22021611084400", "contractSerial": "",
    "currentIndex": 0, "customerAddress": "Ấp 6, Xã Châu Pha, TP HCM",
    "customerCode": "03700001", "customerName": "KHÁCH HÀNG KHÔNG GHI TÊN",
    "customerPhone": "", "month": 3, "previousIndex": 0,
    "priceSchemaName": "Nước sinh hoạt đô thị",
    "roadCode": "KH", "roadName": "KH K GHI TÊN", "roadOrder": 1, "year": 2026
  }],
  "status": { "code": "success", "message": "Success" }
}
```
> `currentIndex = 0` → chưa ghi chỉ số tháng này.

### GET /api/jsonws/cm-portlet.api/update-index/customer-code/{customerCode}/contract-code/{contractCode}/year/{year}/month/{month}/new-index/{newIndex}
Ghi chỉ số nước mới. **Bắt buộc có Authorization header.**
```json
{ "status": { "code": "success", "message": "Success" } }
```

---

## ⚠️ Further Considerations

1. **Login không có API riêng** — Xác thực bằng cách gọi `get-roads` với credentials. Nếu trả về `status.code == "success"` → hợp lệ. Nếu 401/lỗi → sai thông tin.

2. **HTTP (không phải HTTPS)** — Cần `android:usesCleartextTraffic="true"` trong AndroidManifest và `NSAllowsArbitraryLoads = true` trong iOS Info.plist.

3. **Offline-first hay Online-only?** — MVP: online-only. Tương lai: cache bằng SQLDelight + WorkManager sync queue.

4. **`currentIndex > 0`** — Dùng làm dấu hiệu đã ghi chỉ số tháng này. Session-local tracking bằng `AppStateHolder.recordedCustomerCodes`.

5. **Tháng/Năm ghi chỉ số** — Lấy từ ngày hệ thống (`kotlinx-datetime`). Có thể thêm UI chọn tháng sau.


