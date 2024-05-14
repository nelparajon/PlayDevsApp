package com.playdevsgame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 3000 // Duración de la pantalla de inicio en milisegundos (3 segundos)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Usar un Handler para retrasar el inicio de MainActivity
        /*Cambio el anterior handler().postDelayed por la nueva convención de google.
        Handler(Looper.getMainLooper()).postDelayed({})
        Al utilizar Looper.getMainLooper(), estás obteniendo una referencia al Looper asociado con el hilo principal de la aplicación desde el propio handler.
        Cada Looper va asociado a un hilo de la aplicación y la mayoría de interacciones del usuario se asocian al Main Looper
        Esto elimina la necesidad de que el sistema tenga que buscar la activity para identificar en qué hilo se encuentra.
        */
        Handler(Looper.getMainLooper()).postDelayed({
            // Creo un Intent para iniciar MainActivity
            val intent = Intent(this@SplashScreenActivity, LogInActivity::class.java)
            startActivity(intent)

            // Cerrar la activity splash después de iniciar MainActivity. Evitas el consumo de recursos
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}