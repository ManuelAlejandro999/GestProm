package com.example.gestprom

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.gestprom.navigation.AppNavigation
import com.example.gestprom.ui.theme.GestPromTheme
import com.example.gestprom.viewmodels.AuthViewModel
import com.example.gestprom.viewmodels.DataViewModel

class MainActivity : ComponentActivity() {
    private var hasNotificationPermission by mutableStateOf(false)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Solicitar permisos de notificación
        requestNotificationPermission()
        
        setContent {
            GestPromTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val dataViewModel: DataViewModel = viewModel()
                    
                    // Inicializar notificaciones de manera segura
                    LaunchedEffect(Unit) {
                        try {
                            println("DEBUG: MainActivity - Iniciando inicialización de notificaciones...")
                            dataViewModel.initializeNotifications(this@MainActivity)
                            println("DEBUG: MainActivity - Inicialización de notificaciones completada")
                        } catch (e: Exception) {
                            // Manejar error de inicialización
                            println("DEBUG: MainActivity - ERROR al inicializar notificaciones: ${e.message}")
                        }
                    }
                    
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        dataViewModel = dataViewModel
                    )
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    hasNotificationPermission = true
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Mostrar explicación al usuario si es necesario
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Para versiones anteriores a Android 13, no se necesita permiso explícito
            hasNotificationPermission = true
        }
    }
}
