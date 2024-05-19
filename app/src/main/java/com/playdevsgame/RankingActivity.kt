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

class RankingActivity: AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var scores: MutableList<Score>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_activity)

        tableLayout = findViewById(R.id.tableLayout)

        scores = mutableListOf()

        firebaseDatabase = FirebaseDatabase.getInstance()
        val scoresRef = firebaseDatabase.getReference("users")

        scoresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scores.clear()  // Limpiamos la lista antes de llenarla con nuevos datos
                for (userSnapshot in snapshot.children) {
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val scoreString = userSnapshot.child("score").getValue(String::class.java) ?: "0"
                    val score = scoreString.toIntOrNull() ?: 0
                    Log.d("Ranking_Activity", "User: $name, Score: $score") // Añadir este registro de depuración
                    scores.add(Score(name, score))  // Añadimos cada puntuación a la lista
                }
                // Ordenar la lista de puntuaciones por valores en orden descendente
                scores.sortByDescending { it.score }
                updateTable()  // Actualizamos la UI con los nuevos datos
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ScoreListActivity", "Error al leer los datos de Firebase", error.toException())
            }
        })
    }

    private fun updateTable() {
        // Clear existing rows except the header
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        for (score in scores) {
            val tableRow = TableRow(this)
            val nameTextView = TextView(this)
            val scoreTextView = TextView(this)

            nameTextView.text = score.name
            nameTextView.setPadding(8, 8, 8, 8)
            scoreTextView.text = score.score.toString()
            scoreTextView.setPadding(8, 8, 8, 8)

            tableRow.addView(nameTextView)
            tableRow.addView(scoreTextView)

            tableLayout.addView(tableRow)
        }
    }
}