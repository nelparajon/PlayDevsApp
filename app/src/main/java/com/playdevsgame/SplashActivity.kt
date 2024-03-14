package com.playdevsgame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 3000 // Duración de la pantalla de inicio en milisegundos (3 segundos)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Usar un Handler para retrasar el inicio de MainActivity
        Handler().postDelayed({
            // Crear un Intent para iniciar MainActivity
            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(intent)

            // Cerrar esta actividad después de iniciar MainActivity
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}