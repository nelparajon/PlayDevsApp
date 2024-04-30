package com.playdevsgame

import DatabaseHandler
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.util.TimeZone
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.schedulers.Schedulers


class FinalScreenActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var currentBitmap: Bitmap
    private lateinit var contentResolver: ContentResolver


    companion object {
        private const val REQUEST_CREATE_FILE = 1
    }

    @SuppressLint("StringFormatInvalid", "CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_screen)



        val settingsButton: ImageView = findViewById(R.id.botonSettings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        databaseHandler = DatabaseHandler(this) // Inicializar la instancia de DatabaseHandler
        databaseHandler.checkGameHistory()
        //borrar tabla
        databaseHandler.clearTable()
        databaseHandler.checkGameHistory()
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

        // Verificar si la puntuación supera al récord actual
        val highScoreTextView: TextView = findViewById(R.id.HighScoreTextView)

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
        databaseHandler.checkGameHistory()
        storeVictoryInCalendar(score, highScoreTextView.toString())
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

    private fun getScreenBitmap(): Bitmap {

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

    private fun storeVictoryInCalendar(score: Int, highScore: String){
        contentResolver = getContentResolver()
        val values = ContentValues()
        val victoryTime = System.currentTimeMillis()



        values.put(CalendarContract.Events.CALENDAR_ID, 1) // ID del dispositivo
        values.put(CalendarContract.Events.DTSTART, victoryTime)
        values.put(CalendarContract.Events.TITLE, "Victoría en PlayDevs Get Success!!")
        values.put(CalendarContract.Events.DESCRIPTION, "Puntuación Obtenida: $score")
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID())

// Usar el URI correcto para los eventos
        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

        if(uri != null){
            Log.d("Calendar", "Evento añadido con URI: ${uri.toString()}")
        } else {
            Log.e("Calendar", "Error al añadir evento")
        }

    }

}


