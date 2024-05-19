package com.playdevsgame

import DatabaseHandler
import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity() {
    // Definir constantes para los códigos de solicitud de permisos
    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
        private const val PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 101
    }


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseHandler: DatabaseHandler
    private val PERMISSIONS_REQUEST_CODE = 1001 // Código de solicitud de permisos
    private lateinit var btnRanking : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Start the audio service if it's not already running
        startAudioServiceIfNeeded()

        // Verificar y solicitar permisos en tiempo de ejecución si es necesario
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        requestLocationPermissions()

        // Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar DatabaseHandler
        databaseHandler = DatabaseHandler(this)

        // Solicitar permisos de ubicación
        requestLocationPermissions()

        // Ejemplo de cómo utilizar DatabaseHandler para obtener datos
        databaseHandler.getRecordScoreData()
            .subscribe({ highScore ->
                Log.d("MainActivity", "High Score: $highScore")
            }, { error ->
                Log.e("MainActivity", "Error al obtener el récord de puntuación", error)
            })

        // Configurar SharedPreferences
        setupPlayerName()

        // Configurar botones
        setupButtons()
    }

    private fun requestLocationPermissions() {
        // Verifica si los permisos de ubicación ya están concedidos
        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocationPermission || !hasCoarseLocationPermission) {
            // Si no, solicita los permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            // Permiso ya concedido, puedes proceder con la funcionalidad que requiere ubicación
            getLastLocationAndSave()
        }
    }

    private fun setupPlayerName() {
        val playerName = PreferenceManager.getPlayerName(this)
        if (playerName.isNullOrEmpty()) {
            PreferenceManager.savePlayerName(this, "player")
        }
    }

    private fun setupButtons() {
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

        btnRanking = findViewById(R.id.rankingButton)
        btnRanking.setOnClickListener {
            val intent = Intent(this@MainActivity, RankingActivity::class.java)
            startActivity(intent)
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
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
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
                Log.d(
                    "Calendar",
                    "ID: $calID Account: $accountName Display Name: $displayName Owner: $ownerName"
                )

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
            // Handle specific logic for a general request code (if applicable)
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setupDatabase()
                setupUI()
            } else {
                Log.e("MainActivity", "Permission Denied")
            }
        }

        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Foreground location permission granted")
                    getLastLocationAndSave()
                } else {
                    Log.e("MainActivity", "Foreground location permission denied")
                }
            }

            PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Background location permission granted")
                } else {
                    Log.e("MainActivity", "Background location permission denied")
                }
            }
        }
    }

    /*override fun onRequestPermissionsResult(
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
=======



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Permiso de ubicación en primer plano concedido")
                    getLastLocationAndSave()
                } else {
                    Log.e("MainActivity", "Permiso de ubicación en primer plano denegado")
                }
            }
            PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Permiso de ubicación en segundo plano concedido")
                } else {
                    Log.e("MainActivity", "Permiso de ubicación en segundo plano denegado")
                }
>>>>>>> fabbfe2338c56c7ab67
            }
        }
    }

<<<<<<< HEAD
}
=======
*/
    private fun getLastLocationAndSave() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                databaseHandler.insertLocationData(location.latitude, location.longitude)
                    .subscribe({
                        Log.d(
                            "MainActivity",
                            "Ubicación guardada correctamente: Lat ${location.latitude}, Lon ${location.longitude}"
                        )
                    }, { error ->
                        Log.e("MainActivity", "Error al guardar la ubicación", error)
                    })
            } else {
                Log.e("MainActivity", "Error: La ubicación recibida es nula")
            }
        }
    }
}
