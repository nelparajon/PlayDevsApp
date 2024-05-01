package com.playdevsgame

import DatabaseHandler
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.schedulers.Schedulers


class FinalScreenActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var currentBitmap: Bitmap

    companion object {
        private const val REQUEST_CREATE_FILE = 1
        private const val WRITE_REQUEST_CODE = 2  // Código de solicitud para escritura
    }

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

        val newGameButton: Button = findViewById(R.id.NewGameButton)
        newGameButton.setOnClickListener {
            val intent = Intent(this@FinalScreenActivity, GameActivity::class.java)
            startActivity(intent)
        }

        val score = intent.getIntExtra("EXTRA_SCORE", 0) // 0 es el valor por defecto
        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        scoreTextView.text = getString(R.string.puntuacion_actual, score)

        val highScoreTextView: TextView = findViewById(R.id.HighScoreTextView)
        databaseHandler.getRecordScoreData()
            .subscribeOn(Schedulers.io())
            .subscribe({ highScore ->
                if (score > highScore) {
                    highScoreTextView.text = getString(R.string.record, score)
                } else {
                    highScoreTextView.text = getString(R.string.record, highScore)
                }
            }, { error ->
                Log.e("FinalScreenActivity", "Error al obtener el récord: $error")
            })

        val captureButton: Button = findViewById(R.id.captureButton)
        captureButton.setOnClickListener {
            val bitmap = getScreenBitmap()
            if (bitmap != null) {
                saveImageWithMediaStore(bitmap)
            } else {
                Log.e("FinalScreenActivity", "La captura de pantalla falló o el bitmap es null")
            }
        }
    }

    private fun getScreenBitmap(): Bitmap {
        val buttonToHide: Button = findViewById(R.id.NewGameButton)
        val settingsButtonToHide: ImageView = findViewById(R.id.botonSettings)
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

    private fun saveImageWithMediaStore(bitmap: Bitmap) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Captura_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PlayDevsCaptures")
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            Log.e("FinalScreenActivity", "Uri de MediaStore es null, no se pudo guardar la imagen.")
            return
        }

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                    Log.e("FinalScreenActivity", "Fallo al comprimir el bitmap en el outputStream.")
                }
                outputStream.flush()
                // Mostrar un mensaje Toast
                runOnUiThread {
                    Toast.makeText(this, "Captura de pantalla guardada correctamente", Toast.LENGTH_SHORT).show()
                }
            } ?: Log.e("FinalScreenActivity", "No se pudo abrir el outputStream.")
        } catch (e: Exception) {
            Log.e("FinalScreenActivity", "Excepción al guardar la imagen: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // El usuario concedió el permiso, procede a guardar el bitmap
            val bitmap = getScreenBitmap()
            saveImageWithMediaStore(bitmap)
        } else {
            Log.e("FinalScreenActivity", "Permiso de escritura no concedido")
        }
    }
}