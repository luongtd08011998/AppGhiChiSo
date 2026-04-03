package com.example.appghichiso

import androidx.compose.runtime.Composable
import com.example.appghichiso.navigation.AppNavGraph
import com.example.appghichiso.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        AppNavGraph()
    }
}