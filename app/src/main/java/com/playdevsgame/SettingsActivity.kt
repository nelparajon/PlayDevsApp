package com.playdevsgame

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

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

        backButton.setOnClickListener {
            onBackPressed()
        }
    }
}
