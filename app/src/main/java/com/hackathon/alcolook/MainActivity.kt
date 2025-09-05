package com.hackathon.alcolook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.hackathon.alcolook.ui.navigation.AlcoLookNavigation
import com.hackathon.alcolook.ui.theme.AlcoLookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Disable system ActionBar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        enableEdgeToEdge()
        setContent {
            AlcoLookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlcoLookNavigation()
                }
            }
        }
    }
}