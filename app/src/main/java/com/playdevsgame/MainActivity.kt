package com.playdevsgame

import DatabaseHandler
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

    private fun setupPlayerName() {
        val playerName = PreferenceManager.getPlayerName(this)
        if (playerName.isNullOrEmpty()) {
            PreferenceManager.savePlayerName(this, "player")
        }
    }

    private fun setupButtons() {
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

    private fun requestLocationPermissions() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        val shouldRequestBackgroundPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED

        val permissionsToRequest = mutableListOf<String>()
        if (!hasFineLocationPermission) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!hasCoarseLocationPermission) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (shouldRequestBackgroundPermission) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toTypedArray(), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            Log.d("MainActivity", "All necessary permissions are already granted.")
        }
    }


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
            }
        }
    }


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
                databaseHandler.insertLocation(location.latitude, location.longitude).subscribe({
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


