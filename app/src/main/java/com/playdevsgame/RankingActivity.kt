package com.playdevsgame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class RankingActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var records: MutableList<RecordData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_activity)

        tableLayout = findViewById(R.id.tableLayout)
        records = mutableListOf()

        val btnBackToMain = findViewById<Button>(R.id.btnBackToMain)
        btnBackToMain.setOnClickListener {
            val intent = Intent(this@RankingActivity, MainActivity::class.java)
            startActivity(intent)
        }

        fetchRecords()
    }

    private fun fetchRecords() {
        lifecycleScope.launch {
            try {
                val response: Response<Map<String, RecordData>> = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.getRecords()
                }
                if (response.isSuccessful) {
                    response.body()?.let { recordMap ->
                        records.clear()
                        records.addAll(recordMap.values)
                        records.sortByDescending { it.record.toIntOrNull() }
                        val topScores = records.take(10)
                        updateTable(topScores)
                    } ?: run {
                        Log.e("RankingActivity", "La respuesta es nula.")
                    }
                } else {
                    Log.e("RankingActivity", "Error en la respuesta: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("RankingActivity", "Error al hacer la solicitud", e)
            }
        }
    }

    private fun updateTable(topScores: List<RecordData>) {
        // Clear existing rows except the header
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        for (record in topScores) {
            val tableRow = TableRow(this)
            val nameTextView = TextView(this)
            val recordTextView = TextView(this)
            val coinsTextView = TextView(this)

            nameTextView.text = record.name
            nameTextView.setPadding(8, 8, 8, 8)
            recordTextView.text = record.record
            recordTextView.setPadding(8, 8, 8, 8)
            coinsTextView.text = record.coins.toString()
            coinsTextView.setPadding(8, 8, 8, 8)

            tableRow.addView(nameTextView)
            tableRow.addView(recordTextView)
            tableRow.addView(coinsTextView)

            tableLayout.addView(tableRow)
        }
    }
}


