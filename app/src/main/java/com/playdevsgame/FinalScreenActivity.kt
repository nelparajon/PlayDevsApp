package com.playdevsgame

import DatabaseHandler
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FinalScreenActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var contentResolver: ContentResolver

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_screen)

        databaseHandler = DatabaseHandler(this) // Inicializar databaseHandler aquí

        val settingsButton: ImageView = findViewById(R.id.botonSettings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // Recuperar la puntuación pasada desde otra Activity
        val score = intent.getIntExtra("EXTRA_SCORE", 0) // 0 es el valor por defecto

        // Configurar la Puntuación Actual
        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        // Establecer el texto con la puntuación actual
        scoreTextView.text = getString(R.string.puntuacion_actual, score)

        // Verificar si la puntuación supera al récord actual
        val highScoreTextView: TextView = findViewById(R.id.HighScoreTextView)
        // Inicializar contentResolver
        contentResolver = getContentResolver()

        databaseHandler = DatabaseHandler(this)
        databaseHandler.getRecordScoreData()
            .subscribeOn(Schedulers.io())
            .subscribe({ highScore ->
                if (score > highScore) {
                    highScoreTextView.text = getString(R.string.record, score)
                }
                else {
                    // Configurar el Récord
                    highScoreTextView.text = getString(R.string.record, highScore)
                }
            }, { error ->
                Log.e("FinalScreenActivity", "Error al obtener el récord: $error")
            })
        //databaseHandler.checkGameHistory()

        //val score = intent.getIntExtra("EXTRA_SCORE", 0)

        // Llama a la función para agregar la victoria al calendario directamente
        storeVictoryInCalendar(score)

    }

    private fun storeVictoryInCalendar(score: Int) {
        val calendarHelper = CalendarHelper(contentResolver)

        GlobalScope.launch(Dispatchers.Main) {
            val calendarId = calendarHelper.getCalendarId()
            if (calendarId != null) {
                try {
                    calendarHelper.insertGameResult(score.toString(), System.currentTimeMillis())
                } catch (e: Exception) {
                    Log.e(
                        "FinalScreenActivity",
                        "Error al agregar la victoria al calendario: ${e.message}"
                    )
                }
            } else {
                Log.e("FinalScreenActivity", "No se pudo obtener el ID del calendario")
            }
        }
    }
}