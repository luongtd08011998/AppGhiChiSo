package com.example.appghichiso

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.appghichiso.navigation.AppNavGraph

@Composable
fun App() {
    MaterialTheme {
        AppNavGraph()
    }
}