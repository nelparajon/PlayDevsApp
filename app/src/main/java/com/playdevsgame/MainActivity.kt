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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start the audio service if it's not already running
        startAudioServiceIfNeeded()

        databaseHandler = DatabaseHandler(this)
        databaseHandler.writableDatabase

        // Fetch high score and handle data
        databaseHandler.getRecordScoreData()
            .subscribe({ highScore ->
                Log.d("MainActivity", "High Score: $highScore")
            }, {
                // Handle error if occurs
                Log.e("MainActivity", "Error fetching high score", it)
            })

        setupDefaultPlayerName()

        val btnPlay: Button = findViewById(R.id.myButton)
        btnPlay.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        val btnSettings: Button = findViewById(R.id.settingsButton)
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }
    }

    private fun startAudioServiceIfNeeded() {
        if (!isMyServiceRunning(AudioPlaybackService::class.java)) {
            startService(Intent(this, AudioPlaybackService::class.java))
        }
    }

    private fun setupDefaultPlayerName() {
        val playerName = PreferenceManager.getPlayerName(this)
        if (playerName.isNullOrEmpty()) {
            PreferenceManager.savePlayerName(this, "player")
        }
    }
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
        stopService(Intent(this, AudioPlaybackService::class.java))
    }
}