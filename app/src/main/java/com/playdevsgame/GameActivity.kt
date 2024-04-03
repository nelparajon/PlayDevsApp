package com.playdevsgame

import DatabaseHandler
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import io.reactivex.rxjava3.schedulers.Schedulers


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

    private lateinit var databaseHandler: DatabaseHandler

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initializateViews()

        databaseHandler = DatabaseHandler(this)

        var textViewPar = findViewById<TextView>(R.id.parText)
        var textViewImpar = findViewById<TextView>(R.id.imparText)

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
        //retrasamos la activación de los dados durante 0,9 segundos
        activateBtns()
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

        activateBtns()

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

            Log.d("TAG", "El jugador ha acertado, puntaje establecido en 10")
        }

        // Guardar el puntaje actualizado
        this.score = score

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
            openFinalScreenActivity()
        }


    }

    private fun openFinalScreenActivity() {
        Toast.makeText(this, "Fin del juego", Toast.LENGTH_SHORT).show()

        // Guardar la puntuación y actualizar el récord
        val playerName = PreferenceManager.getPlayerName(this)
        val score = textViewScore.text.toString().toInt()
        databaseHandler.saveScoreAndCheckRecord(playerName, score)
            .subscribe({
                Log.d("GameActivity", "Puntuación guardada en la base de datos y récord actualizado")
                val intent = Intent(this@GameActivity, FinalScreenActivity::class.java)
                intent.putExtra("EXTRA_SCORE", score)
                startActivity(intent)
            }, { error ->
                Log.e("GameActivity", "Error al guardar la puntuación y actualizar el récord: $error")
            })
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


