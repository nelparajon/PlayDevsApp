package com.playdevsgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class FinalScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_screen)

        // Configura el botón de Nueva Partida
        val newGameButton: Button = findViewById(R.id.newGameButton)
        newGameButton.setOnClickListener {
            // Crear un Intent para iniciar GameActivity
            val intent = Intent(this@FinalScreenActivity, GameActivity::class.java)
            startActivity(intent)
        }

        // Recuperar la puntuación pasada desde otra Activity
        val score = intent.getIntExtra("EXTRA_SCORE", 0) // 0 es el valor por defecto

        // Configurar la Puntuación Actual
        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        // Establecer el texto con la puntuación actual
        scoreTextView.text = getString(R.string.puntuacion_actual, score)

        // Configurar el Récord
        val highScoreTextView: TextView = findViewById(R.id.highScoreTextView)
        val highScore = getHighScore() // Este método recupera el récord actual
        highScoreTextView.text = getString(R.string.record, highScore)
    }

    private fun getHighScore(): Int {
        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        return sharedPreferences.getInt("HIGH_SCORE", 0) // 0 es el valor por defecto
    }
}


