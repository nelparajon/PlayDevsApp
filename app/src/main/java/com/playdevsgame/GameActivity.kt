package com.playdevsgame

import DatabaseHandler
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.appbar.MaterialToolbar


class GameActivity : AppCompatActivity() {

    private var clickCount = 0
    private val totalRolls = 10 //10 tiradas iniciales
    private val ANIMATIONS_DURATION: Long = 1000
    private val SHOW_TEXT: Long = 1100
    private lateinit var diceImageView: ImageView
    private lateinit var diceImageView2: ImageView
    private lateinit var diceImageView3: ImageView
    private lateinit var textViewScore: TextView
    private lateinit var onRollBtnPar: Button
    private lateinit var onRollBtnImpar: Button
    private lateinit var viewRollsText: TextView
    private lateinit var parText: TextView
    private lateinit var imparText: TextView
    private var score = 0
    private lateinit var playerTextView: TextView
    private lateinit var databaseHandler: DatabaseHandler // Agregar una instancia de DatabaseHandler
    private var initTime: Long = 0
    private var endTime: Long = 0
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "my_channel_id" // Necesario para versiones de Android Oreo y posteriores
    private val REQUEST_LOCATION_PERMISSION = 123 // Código de solicitud de permisos personalizado
    private lateinit var contentResolver: ContentResolver

    companion object {
        const val PERMISSION_REQUEST_CODE = 101 // Código para identificar la solicitud de permisos
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val homeButton: ImageButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        /*val helpButton: ImageButton = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }*/

      /*  val helpButton: ImageButton = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            val helpBottomSheet = HelpBottomSheetFragment.newInstance()
            helpBottomSheet.show(supportFragmentManager, helpBottomSheet.tag)
        }*/

        val helpButton: ImageButton = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            val helpBottomSheet = HelpBottomSheetFragment.newInstance() // Asegúrate de que el método newInstance() esté definido correctamente en tu fragmento.
            helpBottomSheet.show(supportFragmentManager, helpBottomSheet.tag)
        }



        initializateViews()

        databaseHandler = DatabaseHandler(this) // Inicializar la instancia de DatabaseHandler
        databaseHandler.writableDatabase

        playerTextView = findViewById(R.id.playerText)

        // Cargar el nombre del jugador guardado
        val playerName = PreferenceManager.getPlayerName(this)
        playerTextView.text = playerName

        //boton par con un listener donde se incluye como funciona el boton y que funciones desempeña
        onRollBtnPar.setOnClickListener {
            if (clickCount < 10) {

                onClicParBtn()

                clickCount++

                updateRollsRemaining(totalRolls, clickCount, viewRollsText)

                //bucle que pasa a la siguiente activity(pantalla puntuación) cuando se acabe el contador de clics
                /* if(clickCount == 10){
                    openScoreActivity(updateScore)
                    finish()
                }*/

            }
            if (clickCount == 10) {

                Log.d("NOTIFICACION", "Mostrando notificación de victoria")


                val (minutes, seconds) = elapsedTime(clickCount)
                sendNotification(minutes, seconds)

            }
        }

        //boton impar con un listener donde se incluye como funciona el boton y que funciones desempeña
        onRollBtnImpar.setOnClickListener {
            if (clickCount < 10) {

                onClicImparBtn()

                clickCount++

                updateRollsRemaining(totalRolls, clickCount, viewRollsText)

                //bucle que pasa a la siguiente activity(pantalla puntuación) cuando se acabe el contador de clics
                /* if(clickCount == 10){
                    openScoreActivity(updateScore)
                }*/

            }

            if (clickCount == 10) {

                Log.d("NOTIFICACION", "Mostrando notificación de victoria")


                val (minutes, seconds) = elapsedTime(clickCount)
                sendNotification(minutes, seconds)

            }
        }

    }

    //onClic del botón par
    private fun onClicParBtn() {
        //deshabilitamos los botones
        disabledBtns()
        val (randomDiceValue1, randomDiceValue2, randomDiceValue3) = rollDice()
        animateDiceRoll(diceImageView, randomDiceValue1)
        animateDiceRoll(diceImageView2, randomDiceValue2)
        animateDiceRoll(diceImageView3, randomDiceValue3)

        //resultado de la suma de los dados.
        val diceRollSum =
            diceRollResult(randomDiceValue1, randomDiceValue2, randomDiceValue3)
        showResultText(parText, imparText, diceRollSum)
        //textViewCode.text = "$diceRollSum"
        val success: Boolean = diceRollSum % 2 == 0
        updateUIScore(success)



        if ((totalRolls - clickCount) > 0) {
            activateBtns()
        }
    }

    //onClic del boton impar
    private fun onClicImparBtn() {
        disabledBtns()

        val (randomDiceValue1, randomDiceValue2, randomDiceValue3) = rollDice()

        animateDiceRoll(diceImageView, randomDiceValue1)
        animateDiceRoll(diceImageView2, randomDiceValue2)
        animateDiceRoll(diceImageView3, randomDiceValue3)

        //resultado de la suma de los dados.
        val diceRollSum =
            diceRollResult(randomDiceValue1, randomDiceValue2, randomDiceValue3)

        showResultText(parText, imparText, diceRollSum)
        //textViewCode.text = "$diceRollSum"
        var success: Boolean = diceRollSum % 2 != 0
        updateUIScore(success)

        if ((totalRolls - clickCount) > 0) {
            activateBtns()
        }

    }

    //función que se usará para desahabilitar los botones durante la animación
    //evitando colapso al pulsarlos demasiado rápido
    private fun disabledBtns() {
        onRollBtnPar.isEnabled = false
        onRollBtnImpar.isEnabled = false
    }

    //función que activa los botones de nuevo
    //retrasamos la activación durante 0,9 segundos para que coincida con la animacion
    private fun activateBtns() {
        Handler(Looper.getMainLooper()).postDelayed({
            onRollBtnPar.isEnabled = true
            onRollBtnImpar.isEnabled = true
        }, ANIMATIONS_DURATION)
    }

    private fun elapsedTime(clickCount: Int): Pair<Long, Long> {
        return when (clickCount) {
            1 -> {
                initTime = System.currentTimeMillis()
                Pair(0, 0) // No se devuelve ninguna diferencia porque solo se ha establecido initTime
            }
            10 -> {
                val endTime = System.currentTimeMillis()
                if (endTime > initTime) {
                    val elapsedTimeSeconds = (endTime - initTime) / 1000
                    val minutes = elapsedTimeSeconds / 60
                    val seconds = elapsedTimeSeconds % 60
                    Pair(minutes, seconds)
                } else {
                    Pair(0, 0) // Devuelve 0 si endTime es menor que initTime
                }
            }
            else -> {
                // Si clickCount no es 1 ni 10, devuelve 0
                Pair(0, 0)
            }
        }
    }

    //función donde inicializamos todas las vistas y sus valores iniciales(si existiesen)
    private fun initializateViews() {
        diceImageView = findViewById(R.id.diceImageView)
        diceImageView2 = findViewById(R.id.diceImageView2)
        diceImageView3 = findViewById(R.id.diceImageView3)
        textViewScore = findViewById(R.id.score)
        onRollBtnPar = findViewById(R.id.btn_par)
        onRollBtnImpar = findViewById(R.id.btn_impar)
        viewRollsText = findViewById(R.id.rollsNum)
        parText = findViewById(R.id.parText)
        imparText = findViewById(R.id.imparText)

        viewRollsText.text = "$totalRolls"
    }

    private fun animateDiceRoll(diceImageView: ImageView, randomDiceValue: Int) {
        val rotationAnimator = ObjectAnimator.ofFloat(diceImageView, "rotation", 0f, 360f)
        rotationAnimator.duration = 1000 // Duración de la animación en milisegundos
        rotationAnimator.interpolator = AccelerateDecelerateInterpolator()

        rotationAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            if (value % 90 == 0f) {
                // Cambia la imagen del dado cada 90 grados (cara nueva)

                val diceDrawableId = resources.getIdentifier(
                    "dice$randomDiceValue",
                    "drawable",
                    packageName
                )
                diceImageView.setImageResource(diceDrawableId)
            }
        }

        rotationAnimator.start()
    }

    // Función que realiza un lanzamiento de dados y devuelve el resultado
    private fun rollDice(): Triple<Int, Int, Int> {
        // Genera los valores aleatorios para los tres dados
        val randomDiceValue1 = (1..6).random()
        val randomDiceValue2 = (1..6).random()
        val randomDiceValue3 = (1..6).random()

        // Devuelve el resultado como una Tripleta
        return Triple(randomDiceValue1, randomDiceValue2, randomDiceValue3)
    }

    //función que suma el resultado de los dados
    private fun diceRollResult(vararg values: Int): Int {
        return values.sum()
    }

    //función que muestra si el resultado es par o impar al jugador
    //con un retraso para que dure mas o menos lo mismo que la animación
    //además, se oculta el texto justo después de hacer clic, antes de que vuelva a la función
    private fun showResultText(parText: TextView, imparText: TextView, diceRollSum: Int) {
        // Oculta ambos textos al principio
        parText.visibility = View.INVISIBLE
        imparText.visibility = View.INVISIBLE
        //retraso de 0,8 segundos para que coincida con el final de la animación de los dados
        Handler(Looper.getMainLooper()).postDelayed({
            if (diceRollSum % 2 == 0) {
                parText.visibility = View.VISIBLE
                imparText.visibility = View.INVISIBLE
            } else {
                imparText.visibility = View.VISIBLE
                parText.visibility = View.INVISIBLE
            }
        }, SHOW_TEXT)

    }

    /**/
    //función que calcula la puntuación del jugador en base al acierto
    private fun scoreSuccess(success: Boolean, textView: TextView): Int {
        Log.d("TAG", "Entrando en scoreSuccess")

        // Obtener el puntaje actual del TextView
        var score: Int = textView.text.toString().toIntOrNull() ?: 0
        Log.d("TAG", "Puntaje actual: $score")

        // Si el jugador acierta, el puntaje se suma en 10
        if (success) {
            score += 10
            this.score = score
            Log.d("TAG", "El jugador ha acertado, puntaje establecido en 10")
        }

        Log.d("TAG", "Puntaje final: $score")
        return score
    }

    //función que actualiza la vista de puntuación del jugador
    private fun updateUIScore(success: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            var updateScore: Int = scoreSuccess(success, textViewScore)
            textViewScore.text = "$updateScore"
        }, SHOW_TEXT)

    }

    //función que actualiza las tiradas de dados restantes
    private fun updateRollsRemaining(totalRolls: Int, clickCount: Int, textView: TextView) {
        var rollsRemaining = totalRolls - clickCount
        textView.text = "$rollsRemaining"
        if (rollsRemaining == 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                openFinalScreenActivity()
            }, 3000) // Espera 1 segundo (1000 milisegundos)
        }
    }

    private fun insertScoreInBD(finalScore: Int) {
        val playerName = PreferenceManager.getPlayerName(this)
        val db = databaseHandler.writableDatabase
        databaseHandler.insertData(playerName, finalScore).subscribe {
            Log.d("MainActivity", "Registro insertado correctamente")
        }
        db.close()
    }



    private fun sendNotification(minutes: Long, seconds: Long) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "Channel name",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Channel description"
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "ACTION_NOTIFICATION_CLICK"
            putExtra("RESOLUTION_TIME", minutes to seconds) // Envía los minutos y segundos como un par
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mBuilder = NotificationCompat.Builder(applicationContext, "default")
            .setSmallIcon(R.drawable.play_devs_logo_proto)
            .setContentTitle("VICTORIA!")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentText("Tiempo: ${formatTime(minutes, seconds)}") // Formatea los minutos y segundos
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(0, mBuilder.build())
    }

    private fun formatTime(minutes: Long, seconds: Long): String {
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun openFinalScreenActivity() {
        insertScoreInBD(score)
        val intent = Intent(this@GameActivity, FinalScreenActivity::class.java)
        intent.putExtra("EXTRA_SCORE", score) // Variable 'score'
        startActivity(intent)
        finish()
    }
}

//función para cambiar a la siguiente activity(pantalla puntuación)
//además se le pasa la variable de puntuación para poder usarla en la siguiente activity
//de momento solo lanza un texto


//private fun openScoreActivity(updateScore: Int){
// val intent = Intent(this, ScoreActivity::class.java)
//intent.putExtra("score", updateScore)
//startActivity(intent)
//finish()
//Toast.makeText(this, "Fin del juego", Toast.LENGTH_SHORT)