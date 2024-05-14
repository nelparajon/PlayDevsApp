package com.playdevsgame

import DatabaseHandler
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.core.text.HtmlCompat
import com.google.android.material.appbar.MaterialToolbar




class SettingsActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler // Agregar una instancia de DatabaseHandler
    private lateinit var musicCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Inicialización y configuración del CheckBox
        musicCheckBox = findViewById(R.id.musicCheckBox)
        // Establecer el estado inicial del CheckBox como marcado
        musicCheckBox.isChecked = true

        // Establecer el listener para manejar cambios en el estado del CheckBox
        musicCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Iniciar el servicio de reproducción de música si el CheckBox está marcado
                startMusicService()
            } else {
                // Detener el servicio de reproducción de música si el CheckBox no está marcado
                stopMusicService()
            }
        }

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val homeButton: ImageButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

      /*  val helpButton: ImageButton = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }*/

       /* val helpButton: ImageButton = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            val helpBottomSheet = HelpBottomSheetFragment.newInstance()
            helpBottomSheet.show(supportFragmentManager, helpBottomSheet.tag)
        }*/

        val helpButton: ImageButton = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            val helpBottomSheet = HelpBottomSheetFragment.newInstance() // Asegúrate de que el método newInstance() esté definido correctamente en tu fragmento.
            helpBottomSheet.show(supportFragmentManager, helpBottomSheet.tag)
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

    private fun startMusicService() {
        val intent = Intent(this, AudioPlaybackService::class.java)
        startService(intent)
    }

    private fun stopMusicService() {
        val intent = Intent(this, AudioPlaybackService::class.java)
        stopService(intent)
    }

    /*private fun showHistoryDialog(history: String) {*/
    private fun showInstructionsDialog() {
        val instructions = getString(R.string.instructions)
        val alertDialogBuilder = AlertDialog.Builder(this)
        val title = getString(R.string.dialog_title_instructions)
        val acceptText = getString(R.string.dialog_button_accept)

        alertDialogBuilder.setTitle(
            HtmlCompat.fromHtml(
                "<big>$title</big>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )
        alertDialogBuilder.setMessage(
            HtmlCompat.fromHtml(
                instructions,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )
        alertDialogBuilder.setPositiveButton(acceptText) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}