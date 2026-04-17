package com.example.appghichiso.di

import com.example.appghichiso.data.api.AuthApiService
import com.example.appghichiso.data.api.CustomerApiService
import com.example.appghichiso.data.api.MeterReadingApiService
import com.example.appghichiso.data.api.RoadApiService
import com.example.appghichiso.data.api.createHttpClient
import com.example.appghichiso.data.local.CredentialsStorage
import com.example.appghichiso.data.repository.AuthRepositoryImpl
import com.example.appghichiso.data.repository.CustomerRepositoryImpl
import com.example.appghichiso.data.repository.MeterReadingRepositoryImpl
import com.example.appghichiso.data.repository.RoadRepositoryImpl
import com.example.appghichiso.domain.repository.AuthRepository
import com.example.appghichiso.domain.repository.CustomerRepository
import com.example.appghichiso.domain.repository.MeterReadingRepository
import com.example.appghichiso.domain.repository.RoadRepository
import com.example.appghichiso.domain.usecase.GetCustomersUseCase
import com.example.appghichiso.domain.usecase.GetRoadsUseCase
import com.example.appghichiso.domain.usecase.LoginUseCase
import com.example.appghichiso.domain.usecase.SubmitMeterReadingUseCase
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
        createHttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Auth) {
                basic {
                    credentials {
                        // Only send credentials when session is active (after login)
                        if (sessionManager.isActive)
                            BasicAuthCredentials(sessionManager.email, sessionManager.password)
                        else null
                    }
                    // Pre-emptively send auth header only when session is active
                    sendWithoutRequest { sessionManager.isActive }
                }
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.BODY
            }
            install(HttpTimeout) {
                // Tăng timeout để tránh "Socket timeout has expired" khi server chậm
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 120_000
                requestTimeoutMillis = 120_000
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
    }

    single { AuthApiService(get()) }
    single { RoadApiService(get()) }
    single { CustomerApiService(get()) }
    single { MeterReadingApiService(get()) }
}

private fun repositoryModule() = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get<AuthApiService>(), get<SessionManager>()) }
    single<RoadRepository> { RoadRepositoryImpl(get()) }
    single<CustomerRepository> { CustomerRepositoryImpl(get()) }
    single<MeterReadingRepository> { MeterReadingRepositoryImpl(get()) }
}

private fun useCaseModule() = module {
    factory { LoginUseCase(get()) }
    factory { GetRoadsUseCase(get()) }
    factory { GetCustomersUseCase(get()) }
    factory { SubmitMeterReadingUseCase(get(), get()) }
}

private fun viewModelModule() = module {
    viewModel { AuthViewModel(get(), get(), get(), get<SessionManager>()) }
    viewModel { RouteViewModel(get()) }
    viewModel { CustomerViewModel(get(), get()) }
    viewModel { MeterReadingViewModel(get(), get()) }
}

private fun stateModule() = module {
    single { AppStateHolder() }
}

