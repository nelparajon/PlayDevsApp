package com.playdevsgame

import DatabaseHandler
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.util.Log
import androidx.core.text.HtmlCompat
import io.reactivex.rxjava3.schedulers.Schedulers

class SettingsActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler // Agregar una instancia de DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val playerNameEditText: EditText = findViewById(R.id.playerNameEditText)
        val backButton: Button = findViewById(R.id.backButton)

        databaseHandler = DatabaseHandler(this) // Inicializar la instancia de DatabaseHandler
        // Cargar el nombre del jugador guardado
        val playerName = PreferenceManager.getPlayerName(this)
        playerNameEditText.setText(playerName)

        playerNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newName = playerNameEditText.text.toString()
                // Guardar el nuevo nombre del jugador en las preferencias
                PreferenceManager.savePlayerName(this, newName)
                // Guardar el nuevo nombre del jugador en la base de datos
                databaseHandler.insertData(newName, 0) // 0 o cualquier otro valor para la puntuación inicial
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        Log.d("SettingsActivity", "Nombre de jugador guardado en la base de datos")
                    }, { error ->
                        Log.e("SettingsActivity", "Error al guardar el nombre de jugador en la base de datos: $error")
                    })
                playerNameEditText.clearFocus() // Ocultar el teclado virtual
                true // Indica que se ha manejado el evento de acción del editor
            } else {
                false // Indica que no se ha manejado el evento de acción del editor
            }
        }

        val buttonShowInstructions = findViewById<Button>(R.id.instructionsButton)
        buttonShowInstructions.setOnClickListener {
            showInstructionsDialog()
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }
    private fun showInstructionsDialog() {
        val instructions = "<font color='#000000'><big>1. El jugador cuenta con 10 tiradas de dados para conseguir obtener la puntuación más alta posible.</big></font><br/>" +
                "<font color='#000000'><big>2. Se empieza el juego con 0 puntos y en cada tirada se debe adivinar si el resultado de los dados será par o impar.</big></font><br/>" +
                "<font color='#000000'><big>3. Si se acierta el resultado, se otorgan 10 puntos. Fallar no conlleva pérdida de puntos.</big></font>"

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(HtmlCompat.fromHtml("<big>Instrucciones del Juego</big>", HtmlCompat.FROM_HTML_MODE_LEGACY))
        alertDialogBuilder.setMessage(HtmlCompat.fromHtml(instructions, HtmlCompat.FROM_HTML_MODE_LEGACY))
        alertDialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}
