package com.example.appghichiso.di

import com.example.appghichiso.data.api.AuthApiService
import com.example.appghichiso.data.api.CustomerApiService
import com.example.appghichiso.data.api.MeterReadingApiService
import com.example.appghichiso.data.api.RoadApiService
import com.example.appghichiso.data.api.SmsApiService
import com.example.appghichiso.data.api.createHttpClient
import com.example.appghichiso.data.local.CredentialsStorage
import com.example.appghichiso.data.repository.AuthRepositoryImpl
import com.example.appghichiso.data.repository.CustomerRepositoryImpl
import com.example.appghichiso.data.repository.MeterReadingRepositoryImpl
import com.example.appghichiso.data.repository.RoadRepositoryImpl
import com.example.appghichiso.data.repository.SmsRepositoryImpl
import com.example.appghichiso.domain.repository.AuthRepository
import com.example.appghichiso.domain.repository.CustomerRepository
import com.example.appghichiso.domain.repository.MeterReadingRepository
import com.example.appghichiso.domain.repository.RoadRepository
import com.example.appghichiso.domain.repository.SmsRepository
import com.example.appghichiso.domain.usecase.GetCustomersUseCase
import com.example.appghichiso.domain.usecase.GetCustomersByRoadUseCase
import com.example.appghichiso.domain.usecase.GetRoadsUseCase
import com.example.appghichiso.domain.usecase.LoginUseCase
import com.example.appghichiso.domain.usecase.SubmitMeterReadingUseCase
import com.example.appghichiso.domain.usecase.GetToPublishListUseCase
import com.example.appghichiso.domain.usecase.GetDebtListUseCase
import com.example.appghichiso.domain.usecase.GetPaidListUseCase
import com.example.appghichiso.domain.usecase.PublishTvanUseCase
import com.example.appghichiso.domain.usecase.PayCashUseCase
import com.example.appghichiso.domain.usecase.GetReceiptUseCase
import com.example.appghichiso.presentation.auth.AuthViewModel
import com.example.appghichiso.presentation.customer.CustomerViewModel
import com.example.appghichiso.presentation.reading.MeterReadingViewModel
import com.example.appghichiso.presentation.route.RouteViewModel
import com.example.appghichiso.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun appModule() = listOf(
    platformModule(),
    networkModule(),
    repositoryModule(),
    useCaseModule(),
    viewModelModule(),
    stateModule()
)

private fun networkModule() = module {
    single { CredentialsStorage(get()) }
    single { SessionManager() }

    single<HttpClient> {
        val sessionManager = get<SessionManager>()
        val client = createHttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.NONE
            }
            install(HttpTimeout) {
                // Tăng timeout để tránh "Socket timeout has expired" khi server chậm
                // connectTimeout: 30s — đủ để thiết lập kết nối
                // socket/requestTimeout: 300s (5 phút) — đủ cho phát hành nhiều hóa đơn TVAN cùng lúc
                connectTimeoutMillis = 30_000
                socketTimeoutMillis  = 300_000
                requestTimeoutMillis = 300_000
            }
            // Intercept 401 — deactivate session and notify UI
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        sessionManager.emitUnauthorized()
                    }
                }
            }
        }

        client.requestPipeline.intercept(io.ktor.client.request.HttpRequestPipeline.State) {
            if (sessionManager.isActive) {
                val authString = "${sessionManager.email}:${sessionManager.password}"
                val base64 = kotlin.io.encoding.Base64.Default.encode(authString.encodeToByteArray())
                context.headers.append(io.ktor.http.HttpHeaders.Authorization, "Basic $base64")
            }
        }

        client
    }

    single { AuthApiService(get()) }
    single { RoadApiService(get()) }
    single { CustomerApiService(get()) }
    single { MeterReadingApiService(get()) }
    single { SmsApiService(get(), get()) }
    single { com.example.appghichiso.data.api.TvanApiService(get(), get()) }
}

private fun repositoryModule() = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get<AuthApiService>(), get<SessionManager>()) }
    single<RoadRepository> { RoadRepositoryImpl(get()) }
    single<CustomerRepository> { CustomerRepositoryImpl(get()) }
    single<MeterReadingRepository> { MeterReadingRepositoryImpl(get()) }
    single<SmsRepository> { SmsRepositoryImpl(get()) }
    single<com.example.appghichiso.domain.repository.TvanRepository> { com.example.appghichiso.data.repository.TvanRepositoryImpl(get()) }
}

private fun useCaseModule() = module {
    factory { LoginUseCase(get()) }
    factory { GetRoadsUseCase(get()) }
    factory { GetCustomersUseCase(get()) }
    factory { GetCustomersByRoadUseCase(get()) }
    factory { SubmitMeterReadingUseCase(get(), get()) }
    factory { com.example.appghichiso.domain.usecase.GetInvoiceStatusUseCase(get()) }
    factory { GetToPublishListUseCase(get()) }
    factory { GetDebtListUseCase(get()) }
    factory { GetPaidListUseCase(get()) }
    factory { PublishTvanUseCase(get()) }
    factory { PayCashUseCase(get()) }
    factory { GetReceiptUseCase(get()) }
}

private fun viewModelModule() = module {
    viewModel { AuthViewModel(get(), get(), get(), get<SessionManager>()) }
    viewModel { RouteViewModel(get()) }
    viewModel { CustomerViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get<com.example.appghichiso.data.local.CredentialsStorage>()) }
    viewModel { MeterReadingViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

private fun stateModule() = module {
    single { AppStateHolder() }
}

