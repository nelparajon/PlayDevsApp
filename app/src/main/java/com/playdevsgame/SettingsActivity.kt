package com.playdevsgame

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.Intent
import android.widget.ImageButton
import androidx.core.text.HtmlCompat
import com.google.android.material.appbar.MaterialToolbar




class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val homeButton: ImageButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

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
        val instructions = getString(R.string.instructions)

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
