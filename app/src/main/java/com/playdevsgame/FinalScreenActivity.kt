package com.playdevsgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class FinalScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_screen)

        // Configura el botón de Nueva Partida
        val newGameButton = findViewById<Button>(R.id.newGameButton)
        newGameButton.setOnClickListener {
            // Crear un Intent para iniciar GameActivity
            val intent = Intent(this@FinalScreenActivity, GameActivity::class.java)
            startActivity(intent)
        }

        val score = intent.getIntExtra("EXTRA_SCORE", 0) // 0 es el valor por defecto

        // Configura el botón de Puntuación Actual
        val currentScoreButton = findViewById<Button>(R.id.currentScoreButton)
        currentScoreButton.setOnClickListener {
            val intent = Intent(this@FinalScreenActivity, ScoreActivity::class.java)
            intent.putExtra("EXTRA_SCORE", score) // Pasar el dato de la puntuación actual
            startActivity(intent)
        }

        // Configura el botón de Récord
        val highScoreButton = findViewById<Button>(R.id.highScoreButton)
        highScoreButton.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de récord aún no implementada", Toast.LENGTH_SHORT).show()
        }
    }
}
