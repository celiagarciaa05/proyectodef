package com.example.proyectodef

import android.app.Application
import android.util.Log
import com.github.anrwatchdog.ANRWatchDog

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("crash", "Excepción no capturada: ${throwable.message}", throwable)
        }

        ANRWatchDog().setANRListener { error ->
            Log.e("anrr", "ANR detectado: ${error.message}", error)
        }.start()
    }
}
