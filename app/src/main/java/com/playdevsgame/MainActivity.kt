package com.playdevsgame

import DatabaseHandler
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var contentResolver: ContentResolver
    private lateinit var uri: Uri
    companion object {
        const val PERMISSION_REQUEST_CODE = 101 // Código para identificar la solicitud de permisos
    }
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
            startActivity(intent)
        }
        checkCalendarPermission()
    }

    private fun checkCalendarPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR), PERMISSION_REQUEST_CODE)
            } else {
                listCalendarsId()
            }
        } else {
            listCalendarsId()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listCalendarsId() // Permiso concedido, proceder con la operación
            } else {
                Log.e("MainActivity", "Permission Denied") // Permiso negado, manejar la situación
            }
        }
    }

    private fun listCalendarsId(){


        contentResolver = getContentResolver()

        uri = CalendarContract.Calendars.CONTENT_URI;

        val projection = arrayOf(
            CalendarContract.Calendars._ID,  // ID necesario para inserciones futuras
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT
        )
        // Cursor que almacena los resultados de la consulta
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val calID = cursor.getLong(0);

                val accountName: String = cursor.getString(1);
                val displayName = cursor.getString(2);
                val ownerName = cursor.getString(3);

                // Usar los datos según sea necesario
                Log.d("Calendar", "ID: " + calID + " Account: " + accountName +
                        " Display Name: " + displayName + " Owner: " + ownerName);
            }
            cursor.close();
        }
    }


}