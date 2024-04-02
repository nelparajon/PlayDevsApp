import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor
import android.util.Log
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers


//clase que utiliza SQLite y OpenHelper para crear la Bd interna dentro de la aplicación.
//en el constructor se especifican los parámetros, nombre, null para los cursores del factory (no es necesario en nuestro caso) y la versión de la BD

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    //definimos las constantes nombre de la base de datos y su version
    companion object{
        private const val DATABASE_NAME = "play_devs.db"
        private const val DATABASE_VERSION = 1
    }

    //con este método, cada vez que se inicia la aplicación se crearán las tablas de la BD si no existen.
    override fun onCreate(db: SQLiteDatabase?) {
        //utilizamos ? en el contexto de kotlin para indicarle que acceda al método execSQL() si no es nulo.

        db?.execSQL("CREATE TABLE IF NOT EXISTS game_history(id_history INTEGER PRIMARY KEY AUTOINCREMENT, player_name TEXT NOT NULL, score INTEGER NOT NULL)")
        Log.d("DatabaseHelper", "Tabla creada")
        // Verificar la creación de la tabla
        /*if (tableExists("game_history", db)) {
            Log.d("DatabaseHelper", "Base de datos creada")
        } else {
            Log.e("DatabaseHelper", "Error creating table 'game_history'")
        }*/
    }

    //este método se utiliza para actuazliar la BD cuando sea necesario.
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    //método para insertar un nuevo registro en la BD en la tabla game_history usando el Completable de RXJava
    //Completable no devuelve un valor de la BD sino que devuelve un error o un "acierto" si se han insertado bien o no los datos
    fun insertData(playerName: String, score: Int): Completable {
        return Completable.fromAction {
            val db = writableDatabase
            val contentValues = ContentValues()
            contentValues.put("player_name", playerName)
            contentValues.put("score", score)
            val insertedRowId = db.insert("game_history", null, contentValues)
            db.insert("game_history", null, contentValues)
            db.close()

            Log.d("DatabaseHelper", "Registro insertado en la base de datos. ID: $insertedRowId, Nombre: $playerName, Puntuación: $score")
        }.subscribeOn(Schedulers.io())


    }

    //funcion que devuelve la puntuaciñon mas alta entre todos los registros de la tabla game_history
    // Single representa un observable que emite exactamente un elemento
    fun getRecordScoreData(): Single<Int> {
        return Single.fromCallable { //se llama al método fromCallable para poder usar código dentro para devolver el valor
            val db = readableDatabase //se lee la base de datos
            val cursor: Cursor? = db.rawQuery("SELECT Max(score) AS record FROM game_history", null) //El resultado se almacena en un objeto Cursor.
            var score = 0
            cursor?.let {
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex("record")
                    if (columnIndex != -1) {
                        score = cursor.getInt(columnIndex)
                    } else {
                        // Si no se encuentra la columna "record"
                        // podrías manejar la situación de alguna manera
                    }
                }
                cursor.close()
            }

            //cerramos la base de datos y devolvemos la puntuación
            db.close()
            score
            //se especifica que se ejecutará en el subproceso shedule de entrada y salida (in-out)
            //esto se hace para que la operación se realice en un hilo a parte del hilo principal de la app
            //así se realiza la operación de manera asícrona al hilo principal
        }.subscribeOn(Schedulers.io())
    }
    //lo mismo que antes pero para todos los registros de la base de datos.
    //esta función es de pruebas de acceso y extracción de datos de la BD. Se podría usar si se fuese necesario
    fun getAllScoreData(): Single<MutableList<Int>> {
        return Single.fromCallable {
            val db = readableDatabase
            val scores = mutableListOf<Int>()
            val cursor: Cursor? = db.rawQuery("SELECT score FROM game_history", null)

            cursor?.use {
                while (cursor.moveToNext()) {
                    val scoreIndex = cursor.getColumnIndex("score")
                    if (scoreIndex != -1) {
                        val score = cursor.getInt(scoreIndex)
                        scores.add(score)
                    } else {
                        // Si no se encuentra la columna "score", podrías manejar la situación de alguna manera
                    }
                }
            }

            cursor?.close()
            db.close()

            scores
        }.subscribeOn(Schedulers.io())
    }

    //
    private fun tableExists(tableName: String, db: SQLiteDatabase?): Boolean {
        val cursor = db?.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=game_history", arrayOf(tableName))
        cursor?.use {
            Log.d("Database Helper", "Tabla existe")
            return it.count > 0
        }
        return false
    }

    fun updateRecordScore(newScore: Int): Completable {
        return Completable.fromAction {
            val db = writableDatabase
            val contentValues = ContentValues()
            contentValues.put("score", newScore)
            val result = db.update("game_history", contentValues, null, null)
            db.close()
            if (result != -1) {
                Log.d("DatabaseHandler", "Récord actualizado correctamente")
            } else {
                Log.e("DatabaseHandler", "Error al actualizar el récord")
            }
        }.subscribeOn(Schedulers.io())
    }

}
