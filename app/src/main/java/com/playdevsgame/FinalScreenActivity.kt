package com.playdevsgame

import DatabaseHandler
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.schedulers.Schedulers


class FinalScreenActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler // Agregar una instancia de DatabaseHandler

    @SuppressLint("StringFormatInvalid", "CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_screen)

        databaseHandler = DatabaseHandler(this) // Inicializar la instancia de DatabaseHandler

        // Configura el botón de Nueva Partida
        val newGameButton: Button = findViewById(R.id.NewGameButton)
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
        val highScoreTextView: TextView = findViewById(R.id.HighScoreTextView)
        databaseHandler.getRecordScoreData()
            .subscribe({ highScore ->
                highScoreTextView.text = getString(R.string.record, highScore)
            }, { error ->
                // Manejar el error si ocurre
            })

        // Verificar si la puntuación supera al récord actual
        databaseHandler.getRecordScoreData()
            .subscribeOn(Schedulers.io())
            .subscribe({ highScore ->
                if (score > highScore) {
                    // Actualizar el récord en la base de datos
                    databaseHandler.updateRecordScore(score)
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            Log.d("FinalScreenActivity", "Récord actualizado en la base de datos")
                        }, { error ->
                            Log.e("FinalScreenActivity", "Error al actualizar el récord: $error")
                        })
                }
            }, { error ->
                Log.e("FinalScreenActivity", "Error al obtener el récord: $error")
            })

}
}


