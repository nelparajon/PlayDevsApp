package com.playdevsgame

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import androidx.core.text.HtmlCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val playerNameEditText: EditText = findViewById(R.id.playerNameEditText)
        val backButton: Button = findViewById(R.id.backButton)

        // Cargar el nombre del jugador guardado
        val playerName = PreferenceManager.getPlayerName(this)
        playerNameEditText.setText(playerName)

        playerNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                val newName = playerNameEditText.text.toString()
                // Guardar el nuevo nombre del jugador
                PreferenceManager.savePlayerName(this, newName)
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