package com.example.gestprom

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class GestPromApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Inicializar WorkManager manualmente
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
        WorkManager.initialize(this, config)
    }
} 