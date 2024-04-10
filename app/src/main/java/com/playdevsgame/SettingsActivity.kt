package com.playdevsgame

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import androidx.core.text.HtmlCompat
import DatabaseHandler
import android.content.Intent
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageButton
import com.google.android.material.appbar.MaterialToolbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

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

        val buttonShowHistory = findViewById<Button>(R.id.historyButton)
        buttonShowHistory.setOnClickListener {
            getHistoryList()
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

    private fun showHistoryDialog(history: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Historial de Partidas")
        alertDialogBuilder.setMessage(history)
        alertDialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getHistoryList() {
        val db = DatabaseHandler(this)
        db.getAllScoreData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ scores ->
                // Aquí recibimos la lista de partidas y la mostramos en el cuadro de diálogo
                val historyList = StringBuilder()
                for ((playerName, score) in scores) {
                    historyList.append("Jugador: $playerName, Puntuación: $score\n")
                }
                showHistoryDialog(historyList.toString())
            }, { error ->
                // Manejar el error si ocurre
                Log.e("SettingsActivity", "Error al obtener datos de la base de datos: ${error.message}")
            })
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
