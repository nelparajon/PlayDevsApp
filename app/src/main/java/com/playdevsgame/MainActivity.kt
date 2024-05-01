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
import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler
    private val PERMISSIONS_REQUEST_CODE = 1001 // Código de solicitud de permisos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start the audio service if it's not already running
        startAudioServiceIfNeeded()

        // Verificar y solicitar permisos en tiempo de ejecución si es necesario
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            // Los permisos ya están concedidos, puedes realizar la lógica adicional aquí
            setupDatabase()
            setupUI()
        }
    }

    private fun setupDatabase() {
        // Configurar la base de datos
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

        // Listar los IDs de los calendarios disponibles
        listCalendarsId()
    }

    private fun setupUI() {
        // Configurar la interfaz de usuario
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
    private fun checkPermissions(): Boolean {
        // Verificar si los permisos están concedidos
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        // Solicitar permisos al usuario
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            ),
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun listCalendarsId() {
        // Consultar y mostrar los detalles de todos los calendarios disponibles en el dispositivo
        val contentResolver = getContentResolver()
        val uri = CalendarContract.Calendars.CONTENT_URI

        // Define las columnas que se desean obtener de la consulta
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT
        )

        // Filtrar los calendarios visibles
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1"

        // Realiza la consulta a través del ContentResolver y obtiene un Cursor con los resultados
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, null, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val calID = cursor.getLong(0)
                val accountName: String = cursor.getString(1)
                val displayName = cursor.getString(2)
                val ownerName = cursor.getString(3)

                // Registra los detalles de cada calendario obtenido
                Log.d("Calendar", "ID: $calID Account: $accountName Display Name: $displayName Owner: $ownerName")

                // Verifica si el nombre del calendario coincide con el calendario de registro de partidas
                if (displayName == "Resultado de partida") {
                    Log.d("Calendar", "Calendario de registro de partidas encontrado: ID: $calID")
                }
            }
            cursor.close() // Cierra el cursor después de su uso para liberar recursos
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Verificar si el usuario concedió los permisos solicitados
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Los permisos fueron concedidos, puedes realizar la lógica que requiere permisos aquí
                setupDatabase()
                setupUI()
            } else {
                // Los permisos fueron denegados, puedes manejar esta situación aquí (por ejemplo, mostrando un mensaje al usuario)
                Log.e("MainActivity", "Permission Denied")
            }
        }
    }

}