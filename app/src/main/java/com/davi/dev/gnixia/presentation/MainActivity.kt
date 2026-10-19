package com.davi.dev.gnixia.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material3.AppScaffold
import com.davi.dev.gnixia.presentation.navigation.AppNavigation
import com.davi.dev.gnixia.presentation.theme.GnixiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    GnixiaTheme {
        AppScaffold {
            AppNavigation()
        }
    }
}
