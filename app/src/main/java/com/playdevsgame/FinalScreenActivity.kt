package com.playdevsgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class FinalScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_screen)

        // Configura el botón de Nueva Partida
        val newGameButton = findViewById<Button>(R.id.newGameButton)
        newGameButton.setOnClickListener {
            // Crear un Intent para iniciar GameActivity
            val intent = Intent(this@FinalScreenActivity, GameActivity::class.java)
            // Iniciar la actividad
            startActivity(intent)
        }

        // Configura el botón de Puntuación Actual
        val currentScoreButton = findViewById<Button>(R.id.currentScoreButton)
        currentScoreButton.setOnClickListener {
            // Aquí es donde colocarías el código para mostrar la puntuación actual
        }

        // Configura el botón de Récord
        val highScoreButton = findViewById<Button>(R.id.highScoreButton)
        highScoreButton.setOnClickListener {
            // Aquí es donde colocarías el código para mostrar el récord
        }
    }
}
