package com.example.appghichiso.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.data.local.CredentialsStorage
import com.example.appghichiso.presentation.auth.AuthViewModel
import com.example.appghichiso.presentation.auth.LoginScreen
import com.example.appghichiso.presentation.customer.CustomerListScreen
import com.example.appghichiso.presentation.reading.MeterReadingScreen
import com.example.appghichiso.presentation.route.RouteListScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/* ---------- Type-safe route objects (KMP-compatible, no Bundle getString) ---------- */

@Serializable
object LoginRoute

@Serializable
object RouteListRoute

@Serializable
data class CustomerListRoute(val roadCode: String, val roadName: String)

@Serializable
object MeterReadingRoute

/* ---------- NavGraph ---------- */

@Composable
fun AppNavGraph() {
    val credentialsStorage = koinInject<CredentialsStorage>()
    val appStateHolder = koinInject<AppStateHolder>()
    val startDestination: Any =
        if (credentialsStorage.isLoggedIn()) RouteListRoute else LoginRoute

    // Khôi phục kỳ ghi chỉ số khi auto-login
    LaunchedEffect(Unit) {
        credentialsStorage.getSavedMonthYear()?.let { (m, y) ->
            appStateHolder.billingMonth = m
            appStateHolder.billingYear  = y
        }
    }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(RouteListRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<RouteListRoute> {
            val authViewModel = koinViewModel<AuthViewModel>()
            RouteListScreen(
                onRoadSelected = { road ->
                    navController.navigate(CustomerListRoute(road.code, road.name))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<CustomerListRoute> { backStackEntry ->
            val route: CustomerListRoute = backStackEntry.toRoute()
            CustomerListScreen(
                roadCode = route.roadCode,
                roadName = route.roadName,
                onCustomerSelected = {
                    navController.navigate(MeterReadingRoute)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<MeterReadingRoute> {
            MeterReadingScreen(
                onBack = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }
    }
}
