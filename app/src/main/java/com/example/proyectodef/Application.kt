package com.example.proyectodef

import android.app.Application
import android.util.Log
import com.github.anrwatchdog.ANRWatchDog

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Crash handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("garcia-crash", "Excepción no capturada: ${throwable.message}", throwable)
            // Aquí podrías guardar en archivo, enviar a servidor, etc.
        }

        // ANR detector
        ANRWatchDog().setANRListener { error ->
            Log.e("garcia-anr", "ANR detectado: ${error.message}", error)
            // Igual, aquí podrías guardar o subir el log
        }.start()
    }
}
