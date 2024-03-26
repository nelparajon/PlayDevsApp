package com.playdevsgame

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private var playerName = "Player" // Nombre por defecto del jugador
    private var score = 0 // Puntuación del jugador
    private lateinit var diceImageView: ImageView
    private lateinit var diceImageView2: ImageView
    private lateinit var diceImageView3: ImageView
    private lateinit var txtScore: TextView
    private var isParButtonPressed = true
    private var isImparButtonPressed = true
    private var remainingRolls = 10 // Inicialmente 10 tiradas restantes


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        txtScore = findViewById(R.id.txtScore)
        updateScoreText() // Actualiza el texto de la puntuación al inicio

        diceImageView = findViewById(R.id.diceImageView)
        diceImageView2 = findViewById(R.id.diceImageView2)
        diceImageView3 = findViewById(R.id.diceImageView3)




        updateRollsText() // Actualiza el contador de tiradas al inicio
    }

    private fun updateScoreText() {
        txtScore.text = "Score: $score"
    }

    fun onParButtonClick(view: View) {
        Log.d("GameActivity", "onParButtonClick called")
        animateDiceRoll(diceImageView)
        animateDiceRoll(diceImageView2)
        animateDiceRoll(diceImageView3)
        Handler(Looper.getMainLooper()).postDelayed({
            val result = rollDice()
            showResult(result)
            if (result % 2 == 0) {
                updateScore(result)
            }
            decreaseRolls() // Disminuye el contador de tiradas
            updateScoreText() // Actualiza la puntuación después de mostrar el resultado
        }, 1000)
    }

    fun onImparButtonClick(view: View) {
        Log.d("GameActivity", "onImparButtonClick called")
        animateDiceRoll(diceImageView)
        animateDiceRoll(diceImageView2)
        animateDiceRoll(diceImageView3)
        Handler(Looper.getMainLooper()).postDelayed({
            val result = rollDice()
            showResult(result)
            if (result % 2 != 0) {
                updateScore(result)
            }
            decreaseRolls() // Disminuye el contador de tiradas
            updateScoreText() // Actualiza la puntuación después de mostrar el resultado
        }, 1000)
    }

    private fun animateDiceRoll(diceImageView: ImageView) {
        val randomDiceValue = (1..6).random() // Genera un valor aleatorio entre 1 y 6

        // Cambia la imagen del dado antes de iniciar la animación de rotación
        val diceDrawableId = resources.getIdentifier(
            "dice$randomDiceValue",
            "drawable",
            packageName
        )
        diceImageView.setImageResource(diceDrawableId)

        // Inicia la animación de rotación
        val rotationAnimator = ObjectAnimator.ofFloat(diceImageView, "rotation", 0f, 360f)
        rotationAnimator.duration = 1000 // Aumenta la duración de la animación a 2000 milisegundos
        rotationAnimator.interpolator = AccelerateDecelerateInterpolator()
        rotationAnimator.start()
    }

    private fun rollDice(): Int {
        // Simulación de tirada de dados
        val dice1 = (1..6).random()
        val dice2 = (1..6).random()
        val dice3 = (1..6).random()

        // Mostrar los resultados en los ImageViews correspondientes
        diceImageView.setImageResource(getDiceImageResource(dice1))
        diceImageView2.setImageResource(getDiceImageResource(dice2))
        diceImageView3.setImageResource(getDiceImageResource(dice3))

        // Calcular y retornar la suma de los resultados de los dados
        return dice1 + dice2 + dice3
    }

    private fun getDiceImageResource(value: Int): Int {
        // Retorna el recurso de imagen correspondiente al valor del dado
        return when (value) {
            1 -> R.drawable.dice1
            2 -> R.drawable.dice2
            3 -> R.drawable.dice3
            4 -> R.drawable.dice4
            5 -> R.drawable.dice5
            else -> R.drawable.dice6
        }
    }

    private fun showResult(totalDiceValue: Int) {
        // Determinar si la suma de los dados es par o impar
        val isEvenResult = totalDiceValue % 2 == 0

        // Mostrar el resultado en el Toast
        val finalResultText = if (isEvenResult) {
            "PAR!!"
        } else {
            "IMPAR!!"
        }

        Toast.makeText(this, finalResultText, Toast.LENGTH_SHORT).show()
    }

    /*private fun updateScore(totalDiceValue: Int) {
        if ((isParButtonPressed && totalDiceValue % 2 == 0) || (isImparButtonPressed && totalDiceValue % 2 != 0)) {
            score += 10
        }
        // Verificar si se presionaron los botones Par e Impar después de actualizar la puntuación
        if (isParButtonPressed || isImparButtonPressed) {
            // Restablecer las variables de botones Par e Impar después de actualizar la puntuación
            isParButtonPressed = false
            isImparButtonPressed = false
        }
    }*/

    private fun updateScore(totalDiceValue: Int) {
        if (isParButtonPressed && totalDiceValue % 2 == 0) {
            score += 10
        }
        if (isImparButtonPressed && totalDiceValue % 2 != 0) {
            score += 10
        }
    }

    private fun updateRollsText() {
        val txtRolls = findViewById<TextView>(R.id.txtTiradasRestantes)
        txtRolls.text = "Tiradas restantes: $remainingRolls"
    }

    private fun decreaseRolls() {
        remainingRolls--
        updateRollsText() // Actualiza el contador de tiradas después de disminuir
        if (remainingRolls == 0) {
            endGame() // Si no quedan tiradas, finaliza la partida
        }
    }



    private fun endGame() {
        // Aquí se iniciará la actividad de final de partida
        // Por ahora, simplemente imprimimos un mensaje
        Toast.makeText(this, "Fin del juego", Toast.LENGTH_SHORT).show()


        val intent = Intent(this@GameActivity, FinalScreenActivity::class.java)
        intent.putExtra("EXTRA_SCORE", score) // score como variable
        startActivity(intent)
        finish()// Si se quiere finalizar GameActivity

    }

    }






