package com.playdevsgame
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler (context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "GameDB"
        private const val TABLE_NAME = "Scores"
        private const val KEY_ID = "id"
        private const val KEY_SCORE = "score"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME ($KEY_ID INTEGER PRIMARY KEY, $KEY_SCORE INTEGER)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addScore(score: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_SCORE, score)
        val result = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result
    }

    fun getHighScore(): Int {
        val db = this.readableDatabase
        val query = "SELECT MAX($KEY_SCORE) FROM $TABLE_NAME"
        val cursor: Cursor? = db.rawQuery(query, null)
        var highScore = 0
        cursor?.let {
            if (it.moveToFirst()) {
                highScore = it.getInt(0)
            }
            it.close()
        }
        db.close()
        return highScore
    }
}