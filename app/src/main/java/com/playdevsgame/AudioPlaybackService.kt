package com.playdevsgame

import android.app.Service
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioPlaybackService : LifecycleService() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest
    private var audioFilePath: String = ""

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onStart(owner: LifecycleOwner) {
            // Ensure mediaPlayer is initialized and ready before attempting to play
            if (::mediaPlayer.isInitialized && !mediaPlayer.isPlaying && audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer.start()
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            // Safely check if mediaPlayer is initialized before attempting to pause
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        prepareAudioFile()
        setupAudioFocusRequest()

        // Initialize mediaPlayer before adding the observer
        prepareMediaPlayer()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAudioFocusRequest() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                handleAudioFocusChange(focusChange)
            }
            .build()
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        if (::mediaPlayer.isInitialized) { // Check initialization before accessing
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    mediaPlayer.pause()
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (!mediaPlayer.isPlaying) mediaPlayer.start()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    mediaPlayer.pause()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareAudioFile() {
        val afd: AssetFileDescriptor = resources.openRawResourceFd(R.raw.background_music01) ?: return
        val inputStream = afd.createInputStream()
        val outputFile = File(filesDir, "background_musicwav05.wav")

        FileOutputStream(outputFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        afd.close()

        audioFilePath = outputFile.absolutePath
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            isLooping = true
            setOnPreparedListener { mp ->
                if (audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mp.start()
                }
            }
            prepareAsync()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        audioManager.abandonAudioFocusRequest(focusRequest)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
