package com.sv.elveloz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sv.elveloz.ui.AppViewModelFactory
import com.sv.elveloz.ui.NavegacionApp
import com.sv.elveloz.ui.theme.ElVelozTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElVelozTheme {
                val factory = AppViewModelFactory(applicationContext)
                NavegacionApp(factory = factory)
            }
        }
    }
}
