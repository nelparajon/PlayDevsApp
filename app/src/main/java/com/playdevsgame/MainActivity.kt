package com.playdevsgame

import DatabaseHandler
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler
    /*private var mediaPlayer: MediaPlayer? = null*/

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // En MainActivity o GameActivity donde inicies el servicio
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isMyServiceRunning(AudioPlaybackService::class.java)) {
                val serviceIntent = Intent(this, AudioPlaybackService::class.java)
                startService(serviceIntent)
            }
        }, 1000) // Retrasa el inicio del servicio 1 segundo.

        databaseHandler = DatabaseHandler(this)
        databaseHandler.writableDatabase

        /*databaseHandler.insertData(PreferenceManager.getPlayerName(this), 10).subscribe {
            Log.d("MainActivity", "Registro insertado correctamente")
        }*/
        databaseHandler.getRecordScoreData()
            .subscribe({ highScore ->
                // Aquí puedes mostrar el récord de puntuación en la actividad principal si lo deseas
                Log.d("MainActivity", "High Score: $highScore")
            }, { error ->
                // Manejar el error si ocurre
            })

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
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    // Función auxiliar para verificar si el servicio está en ejecución
    private fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    override fun onDestroy() {
        super.onDestroy()

        // Detener el servicio de reproducción de audio
        val intent = Intent(this, AudioPlaybackService::class.java)
        stopService(intent)
    }
}