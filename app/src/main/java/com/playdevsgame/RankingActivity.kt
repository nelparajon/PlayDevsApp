package com.playdevsgame

import android.os.Bundle
import android.util.Log
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RankingActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var records: MutableList<Record>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_activity)

        tableLayout = findViewById(R.id.tableLayout)

        records = mutableListOf()

        firebaseDatabase = FirebaseDatabase.getInstance()
        val recordsRef = firebaseDatabase.getReference("records")

        recordsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                records.clear()  // Limpiamos la lista antes de llenarla con nuevos datos
                for (userSnapshot in snapshot.children) {
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val premio = userSnapshot.child("premio").getValue(Int::class.java) ?: 0
                    val recordValue = userSnapshot.child("record").getValue(String::class.java) ?: "0"
                    Log.d("RankingActivity", "User: $name, Premio: $premio, Record: $recordValue")
                    records.add(Record(name, premio, recordValue))  // Añadimos cada puntuación a la lista
                }
                // Ordenar la lista de puntuaciones por valores en orden descendente
                records.sortByDescending { it.record?.toIntOrNull() }
                val topScores = records.take(10) //funciona como un iterable de 10 elementos
                updateTable(topScores)  // Actualizamos la UI con los nuevos datos
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RankingActivity", "Error al leer los datos de Firebase", error.toException())
            }
        })
    }

    private fun updateTable(topScores: List<Record>) {
        // Clear existing rows except the header
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        for (record in topScores) {
            val tableRow = TableRow(this)
            val nameTextView = TextView(this)
            val scoreTextView = TextView(this)

            nameTextView.text = record.name
            nameTextView.setPadding(8, 8, 8, 8)
            scoreTextView.text = record.record.toString()
            scoreTextView.setPadding(8, 8, 8, 8)

            tableRow.addView(nameTextView)
            tableRow.addView(scoreTextView)

            tableLayout.addView(tableRow)
        }
    }
}
