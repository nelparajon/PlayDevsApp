package com.playdevsgame

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        databaseHandler = DatabaseHandler(this)
        databaseHandler.writableDatabase

        databaseHandler.insertData("Jugador1", 10).subscribe {
            Log.d("MainActivity", "Registro insertado correctamente")
        }


        // Verificar si el nombre de jugador predeterminado ya está configurado en SharedPreferences
        val playerName = PreferenceManager.getPlayerName(this)

        // Si el nombre de jugador predeterminado no está configurado, establecerlo como "player"
        if (playerName.isNullOrEmpty()) {
            PreferenceManager.savePlayerName(this, "player")
        }

        val btnPlay: Button = findViewById(R.id.myButton)
        btnPlay.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        val btnSettings: Button = findViewById(R.id.settingsButton)
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
