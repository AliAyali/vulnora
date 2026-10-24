package com.aliayali.vulnora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aliayali.designsystem.theme.VulnoraTheme
import com.aliayali.vulnora.ui.VulnoraApp
import com.aliayali.vulnora.ui.rememberVulnoraAppState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VulnoraTheme {
                val appState = rememberVulnoraAppState()
                VulnoraApp(appState = appState)
            }
        }
    }
}