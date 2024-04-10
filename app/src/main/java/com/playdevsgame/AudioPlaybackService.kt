package com.playdevsgame

import android.app.Service
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioPlaybackService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private var audioFilePath: String = ""

    override fun onCreate() {
        super.onCreate()
        prepareAudioFile()
    }

    private fun prepareAudioFile() {
        val afd: AssetFileDescriptor = resources.openRawResourceFd(R.raw.background_music01) ?: return
        val inputStream = afd.createInputStream()
        val outputFile = File(filesDir, "background_musicwav05.wav")

        try {
            val outputStream = FileOutputStream(outputFile)
            inputStream.copyTo(outputStream)
            outputStream.close()
            afd.close()
            audioFilePath = outputFile.absolutePath
            prepareMediaPlayer()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun prepareMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            isLooping = true
            setOnPreparedListener { mp ->
                mp.start()
            }
            prepareAsync()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Comprobamos si mediaPlayer fue inicializado y si no se está reproduciendo actualmente.
        if (::mediaPlayer.isInitialized && !mediaPlayer.isPlaying) {
            // Agregamos un try-catch para manejar cualquier excepción que podría ocurrir al iniciar el mediaPlayer.
            try {
                mediaPlayer.start()
            } catch (e: IllegalStateException) {
                Log.e("AudioPlaybackService", "Error al iniciar el mediaPlayer", e)
            }
        }
        return START_STICKY
    }


    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
