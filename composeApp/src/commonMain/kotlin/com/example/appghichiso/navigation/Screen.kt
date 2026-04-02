package com.example.appghichiso.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object RouteList : Screen("routes")
    data object CustomerList : Screen("customers/{roadCode}/{roadName}") {
        fun createRoute(roadCode: String, roadName: String) =
            "customers/$roadCode/${roadName.encodeForRoute()}"
    }
    data object MeterReading : Screen("reading")
}

/** Encode simple chars that can break route parsing */
private fun String.encodeForRoute(): String =
    this.replace("/", "%2F").replace("?", "%3F").replace("&", "%26").replace("#", "%23")

