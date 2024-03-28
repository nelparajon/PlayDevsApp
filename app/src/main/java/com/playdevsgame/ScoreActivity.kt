package com.playdevsgame

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score) // Confirma que este es el nombre correcto de tu archivo de layout

        // Recuperar la puntuación pasada desde otra Activity
        val score = intent.getIntExtra("EXTRA_SCORE", 0)

        // Encontrar el TextView para mostrar la puntuación y establecer el texto
        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        scoreTextView.text = getString(R.string.puntuacion_actual, score)
    }
}
