package com.playdevsgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import  android.widget.Toast



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

        val score = intent.getIntExtra("EXTRA_SCORE", 0) // 0 es el valor por defecto


        // Configura el botón de Puntuación Actual
        val currentScoreButton = findViewById<Button>(R.id.currentScoreButton)
        currentScoreButton.setOnClickListener {
            // Mostrar un Toast con la puntuación actual
            Toast.makeText(this, "Puntuación actual: $score", Toast.LENGTH_SHORT).show()






        }


        // Configura el botón de Récord
        val highScoreButton = findViewById<Button>(R.id.highScoreButton)
        highScoreButton.setOnClickListener {
            // Aquí es donde colocarías el código para mostrar el récord
        }
    }
}
