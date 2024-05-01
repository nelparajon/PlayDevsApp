package com.playdevsgame

import DatabaseHandler
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
    private lateinit var currentBitmap: Bitmap

    companion object {
        private const val REQUEST_CREATE_FILE = 1
    }

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

        databaseHandler.checkGameHistory()

        val captureButton: Button = findViewById(R.id.captureButton)
        captureButton.setOnClickListener {
            val bitmap = getScreenBitmap()
            if (bitmap != null) {
                saveImageWithUserInput(bitmap)
            } else {
                // Manejar el caso donde el bitmap es null
                Log.e("FinalScreenActivity", "La captura de pantalla falló o el bitmap es null")
            }
        }
    }

    private fun getScreenBitmap(): Bitmap? {

        val buttonToHide: Button = findViewById(R.id.NewGameButton)
        val settingsButtonToHide: Button = findViewById(R.id.settingsButton)
        settingsButtonToHide.visibility = View.INVISIBLE
        buttonToHide.visibility = View.INVISIBLE
        val rootView: View = window.decorView.findViewById(android.R.id.content)
        rootView.isDrawingCacheEnabled = true
        val result = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        rootView.draw(canvas)
        rootView.isDrawingCacheEnabled = false

        buttonToHide.visibility = View.VISIBLE
        settingsButtonToHide.visibility = View.VISIBLE
        return result
    }
    private fun saveImageWithUserInput(bitmap: Bitmap) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/jpeg"
            putExtra(Intent.EXTRA_TITLE, "Captura_${System.currentTimeMillis()}.jpg")
        }
        currentBitmap = bitmap
        startActivityForResult(intent, REQUEST_CREATE_FILE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                } ?: Log.e("FinalScreenActivity", "No se pudo abrir el OutputStream.")
            } ?: Log.e("FinalScreenActivity", "Data URI is null.")
        }
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