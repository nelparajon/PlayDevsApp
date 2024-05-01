package com.playdevsgame

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_activity)  // Establece el layout que contiene el TextView

        // Extraer el tiempo de resolución del Intent que inició esta Activity
        val resolutionTime = intent.getLongExtra("RESOLUTION_TIME", 0)

        // Encontrar el TextView y mostrar el tiempo de resolución
        val timeTextView = findViewById<TextView>(R.id.timeTextView)
        timeTextView.text = "Tiempo: $resolutionTime"
    }
}