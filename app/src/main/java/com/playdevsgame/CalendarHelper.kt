package com.playdevsgame

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.CalendarContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone

class CalendarHelper(private val contentResolver: ContentResolver) {

    suspend fun insertGameResult(result: String, gameDate: Long) {
        val calendarId = getCalendarId()

        if (calendarId != null) {
            try {
                withContext(Dispatchers.IO) {
                    val values = ContentValues().apply {
                        put(CalendarContract.Events.TITLE, "Victoria en PlayDevs Get Success!!")
                        put(CalendarContract.Events.DESCRIPTION, "Resultado de la partida: $result")
                        put(CalendarContract.Events.DTSTART, gameDate)
                        put(CalendarContract.Events.DTEND, gameDate + (60 * 60 * 1000)) // Event duration: 1 hour
                        put(CalendarContract.Events.CALENDAR_ID, calendarId)
                        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                    }

                    contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                }
            } catch (e: Exception) {
                Log.e("CalendarHelper", "Error al insertar evento en el calendario: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.e("CalendarHelper", "No se pudo obtener el ID del calendario")
        }
    }

    suspend fun getCalendarId(): String? {
        // Implementa la lógica para obtener el ID del calendario
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars._ID} = ?"
        val selectionArgs = arrayOf("6") // ID del calendario que deseas buscar

        val cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use { c ->
            if (c.moveToFirst()) {
                val idColumnIndex = c.getColumnIndex(CalendarContract.Calendars._ID)
                return c.getString(idColumnIndex)
            }
        }

        Log.e("CalendarHelper", "No se pudo obtener el ID del calendario")
        return null // Devuelve el ID del calendario o null si no se pudo obtener
    }
}