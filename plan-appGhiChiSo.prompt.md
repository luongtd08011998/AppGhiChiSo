# Plan: Ứng dụng Ghi Chỉ Số Nước — KMP Architecture & WBS

Xây dựng app đa nền tảng (Android/iOS) theo **Clean Architecture + MVVM/MVI** với **Ktor** gọi API, **Koin** DI, `multiplatform-settings` lưu pre-fill form, và **Jetbrains Navigation Compose**. Session xác thực được giữ **in-memory** (`SessionManager`) — tồn tại khi process còn sống, mất khi clear task.

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
│  LoginUC  GetRoadsUC  GetCustomersUC  SubmitReadUC  │
├────────────────────────┬────────────────────────────┤
│   Data / Remote        │   Session (In-Memory)       │
│   Ktor HTTP Client     │   SessionManager            │
│   API Services         │   isActive / email / pw     │
│                        ├────────────────────────────┤
│                        │   Data / Local (Pre-fill)   │
│                        │   CredentialsStorage        │
│                        │   (multiplatform-settings)  │
└────────────────────────┴────────────────────────────┘
```

---

## 🔐 Chiến Lược Authentication (đã triển khai)

| Tình huống | Hành vi |
|---|---|
| App cold-start (clear task / process kill) | `SessionManager.isActive = false` → **LoginScreen** |
| App warm-start (background → foreground) | `SessionManager.isActive = true` → **RouteListScreen** (bỏ qua login) |
| Đăng nhập thành công | `sessionManager.activate(email, pw, month, year)` |
| Đăng xuất | `sessionManager.deactivate()` → LoginScreen |
| 401 mid-session | `unauthorizedEvent.tryEmit()` → NavGraph bắt → deactivate → LoginScreen |
| "Ghi nhớ mật khẩu" | Lưu vào `CredentialsStorage` để **pre-fill form** — không tự động đăng nhập |

---

## 📦 Tech Stack

| Thành phần | Thư viện | Lý do |
|---|---|---|
| UI | Compose Multiplatform 1.10.x | ✅ Đã có |
| Navigation | `jetbrains.navigation.compose` | KMP native |
| HTTP Client | `ktor-client` (OkHttp/Darwin) | KMP standard |
| Serialization | `kotlinx-serialization-json` | Ktor plugin |
| DI | `koin-compose-multiplatform` | KMP, lightweight |
| Pre-fill storage | `multiplatform-settings` | Lưu form pre-fill |
| Async | `kotlinx-coroutines` | ✅ Đã có transitively |

---

## 📁 Cấu Trúc Thư Mục (thực tế)

```
composeApp/src/commonMain/kotlin/com/example/appghichiso/
├── data/
│   ├── api/
│   │   ├── ApiConfig.kt               # BASE_URL
│   │   ├── AuthApiService.kt          # validate-user
│   │   ├── RoadApiService.kt          # get-roads
│   │   ├── CustomerApiService.kt      # get-customers-by-road
│   │   ├── MeterReadingApiService.kt  # update-index
│   │   ├── HttpClientFactory.kt       # expect fun createHttpClient()
│   │   └── dto/                       # ApiStatus, DTOs
│   ├── local/
│   │   └── CredentialsStorage.kt      # Settings – pre-fill only
│   └── repository/
│       ├── AuthRepositoryImpl.kt
│       ├── RoadRepositoryImpl.kt
│       ├── CustomerRepositoryImpl.kt
│       └── MeterReadingRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── Road.kt
│   │   └── Customer.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── RoadRepository.kt
│   │   ├── CustomerRepository.kt
│   │   └── MeterReadingRepository.kt
│   └── usecase/
│       ├── LoginUseCase.kt
│       ├── GetRoadsUseCase.kt
│       ├── GetCustomersUseCase.kt
│       └── SubmitMeterReadingUseCase.kt
├── session/
│   └── SessionManager.kt              # ✅ In-memory auth singleton
├── presentation/
│   ├── auth/
│   │   ├── LoginScreen.kt             # pre-fill email + password
│   │   └── AuthViewModel.kt
│   ├── route/
│   │   ├── RouteListScreen.kt         # search + pull-to-refresh
│   │   └── RouteViewModel.kt
│   ├── customer/
│   │   ├── CustomerListScreen.kt      # search + recorded badge
│   │   └── CustomerViewModel.kt
│   ├── reading/
│   │   ├── MeterReadingScreen.kt      # confirm dialog + submit
│   │   └── MeterReadingViewModel.kt
│   └── common/
│       ├── LoadingIndicator.kt
│       ├── ErrorView.kt
│       └── UiState.kt
├── navigation/
│   └── AppNavGraph.kt                 # startDest = sessionManager.isActive
├── di/
│   ├── AppModule.kt
│   ├── AppStateHolder.kt
│   └── PlatformModule.kt
└── util/
    └── PlatformDate.kt                # expect currentYear/currentMonth
```

---

## ✅ WBS — Trạng Thái Triển Khai

### 1. Project Setup & Dependencies ✅
- Ktor, Koin, Navigation, Serialization, `multiplatform-settings` trong `libs.versions.toml`
- `composeApp/build.gradle.kts` cấu hình đầy đủ
- `android:usesCleartextTraffic="true"` trong AndroidManifest

### 2. SessionManager (In-Memory Auth) ✅
- `session/SessionManager.kt` — Koin `single`
- `activate(email, pw, month, year)` / `deactivate()`
- `isActive: Boolean` — nguồn sự thật cho auth state
- `unauthorizedEvent: SharedFlow<Unit>` — bắt 401 mid-session

### 3. Lớp Data — Remote ✅
- `HttpClientFactory` — expect/actual (OkHttp / Darwin)
- `HttpClient` đọc credentials từ `SessionManager` động tại mỗi request
- `HttpResponseValidator` bắt 401 → `sessionManager.emitUnauthorized()`
- `AuthApiService.validateUser()` — không cần Auth header (credentials trong URL)
- `RoadApiService`, `CustomerApiService`, `MeterReadingApiService` — Basic Auth

### 4. Lớp Data — Local (Pre-fill) ✅
- `CredentialsStorage` — `multiplatform-settings`
- `save()` lưu email + password (chỉ khi rememberMe=true) cho pre-fill
- `getSavedUsername()`, `getSavedPassword()`, `getSavedMonthYear()`
- **Không có `isLoggedIn()`** — auth state do `SessionManager` quản lý

### 5. Lớp Domain ✅
- Models: `Road`, `Customer`
- Interfaces: `AuthRepository`, `RoadRepository`, `CustomerRepository`, `MeterReadingRepository`
- Use Cases: `LoginUseCase`, `GetRoadsUseCase`, `GetCustomersUseCase`, `SubmitMeterReadingUseCase`

### 6. Lớp Presentation ✅

#### LoginScreen
- Logo + tên công ty Tóc Tiên
- Pre-fill email từ `savedUsername`, password từ `savedPassword` (nếu rememberMe)
- Dropdown chọn Tháng / Năm ghi chỉ số
- Checkbox "Ghi nhớ mật khẩu"
- Button Đăng nhập với loading state

#### RouteListScreen
- Search theo tên/mã tuyến
- Pull-to-refresh
- Icon Đăng xuất trên TopAppBar

#### CustomerListScreen
- Search theo tên/mã khách hàng
- Badge ✅ đã ghi chỉ số (currentIndex > 0 hoặc trong `recordedCustomerCodes`)
- Hiển thị tháng/năm kỳ ghi

#### MeterReadingScreen
- Thông tin khách hàng, chỉ số cũ
- Ô nhập chỉ số mới (numeric keyboard)
- Tính tiêu thụ tự động
- Confirmation dialog trước khi submit

### 7. Navigation ✅
- `startDestination = if (sessionManager.isActive) RouteListRoute else LoginRoute`
- `LaunchedEffect` lắng nghe `unauthorizedEvent` → deactivate + navigate LoginRoute
- Back stack: CustomerList ↔ RouteList; Login bị xóa khỏi stack sau đăng nhập

### 8. Dependency Injection ✅
- `AppModule`: networkModule, repositoryModule, useCaseModule, viewModelModule, stateModule
- `SessionManager` inject vào `HttpClient`, `AuthRepositoryImpl`, `AuthViewModel`
- `platformModule()` — expect/actual: `Settings` (Android SharedPreferences / iOS NSUserDefaults)

### 9. Platform-Specific ✅
- `HttpClientFactory` — `AndroidSqliteDriver` không cần; OkHttp (Android) / Darwin (iOS)
- `CredentialsStorage` — `multiplatform-settings` (không cần expect/actual)
- `PlatformDate` — `expect currentYear/currentMonth` (kotlinx-datetime)

---

## 🔄 Luồng Người Dùng (User Flow)

```
App Start
   │
   ├─ SessionManager.isActive = true (warm-start) ──→ RouteListScreen
   │
   └─ SessionManager.isActive = false (cold-start / clear task)
         │
      LoginScreen
      (pre-fill email/pw nếu rememberMe)
         │
      Nhấn Đăng nhập
         │
      validate-user API
         │
      ├─ Thành công → sessionManager.activate()
      │                    │
      │               RouteListScreen (search tuyến)
      │                    │
      │               Chọn tuyến
      │                    │
      │               CustomerListScreen (search khách hàng)
      │                    │
      │               Chọn khách hàng
      │                    │
      │               MeterReadingScreen (nhập + submit)
      │                    │
      │               Submit → Back → CustomerList ✅
      │
      ├─ Thất bại → Lỗi, ở lại LoginScreen
      │
      └─ 401 mid-session → unauthorizedEvent
                              → sessionManager.deactivate()
                              → LoginScreen (popUpTo 0)
```

---

## 🌐 API Thực Tế (Liferay JSON-WS)

**Base URL:** `http://qlkh.toctienltd.vn`

**Authentication:** `Basic Auth` — `Authorization: Basic <base64(email:password)>`
> Tất cả API (trừ `validate-user`) cần header này — được gửi tự động từ `SessionManager`.

### GET /api/jsonws/cm-portlet.api/validate-user/user-name/{email}/password/{password}
```json
{ "status": { "code": "success", "message": "Success" } }
```

### GET /api/jsonws/cm-portlet.api/get-roads
```json
{ "data": [{ "code": "DN1-MINH", "name": "DOANH NGHIỆP 1- MINH" }], "status": {...} }
```

### GET /api/jsonws/cm-portlet.api/get-customers-by-road/road-code/{roadCode}/year/{year}/month/{month}
```json
{ "data": [{ "customerCode": "...", "currentIndex": 0, "previousIndex": 0, ... }], "status": {...} }
```
> `currentIndex = 0` → chưa ghi chỉ số tháng này.

### GET /api/jsonws/cm-portlet.api/update-index/customer-code/{cc}/contract-code/{ctc}/year/{y}/month/{m}/new-index/{n}
```json
{ "status": { "code": "success", "message": "Success" } }
```

---

## ⚠️ Notes

1. **Session = Process lifetime** — `SessionManager` là Koin `single`. Mất khi process bị kill (clear task). Đây là hành vi mong muốn.
2. **"Ghi nhớ mật khẩu" ≠ auto-login** — chỉ pre-fill form; user phải nhấn nút Đăng nhập thủ công.
3. **HTTP** — `android:usesCleartextTraffic="true"` ✅ và `NSAllowsArbitraryLoads = true` (iOS).
4. **`currentIndex > 0`** — server đã ghi; `AppStateHolder.recordedCustomerCodes` track phiên hiện tại.
5. **Online-only MVP** — không có offline cache (SQLDelight/WorkManager có thể thêm sau).
