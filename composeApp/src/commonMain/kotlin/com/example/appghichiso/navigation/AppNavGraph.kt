package com.example.appghichiso.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.presentation.auth.AuthViewModel
import com.example.appghichiso.presentation.auth.LoginScreen
import com.example.appghichiso.presentation.customer.CustomerListScreen
import com.example.appghichiso.presentation.reading.MeterReadingScreen
import com.example.appghichiso.presentation.route.RouteListScreen
import com.example.appghichiso.session.SessionManager
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
    val sessionManager = koinInject<SessionManager>()
    val appStateHolder = koinInject<AppStateHolder>()

    // Warm-start: process still alive → session active → skip login
    // Cold-start / clear task: process killed → session inactive → show login
    val startDestination: Any =
        if (sessionManager.isActive) RouteListRoute else LoginRoute

    val navController = rememberNavController()

    // Listen for 401 Unauthorized mid-session → deactivate and force re-login
    LaunchedEffect(Unit) {
        sessionManager.unauthorizedEvent.collect {
            sessionManager.deactivate()
            appStateHolder.recordedCustomerCodes.clear()
            appStateHolder.selectedRoad = null
            appStateHolder.selectedCustomer = null
            navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

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


