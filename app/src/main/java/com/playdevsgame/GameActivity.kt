package com.playdevsgame

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val diceImageView = findViewById<ImageView>(R.id.diceImageView)
        val diceImageView2 = findViewById<ImageView>(R.id.diceImageView2)
        val onRollBtn = findViewById<Button>(R.id.btn_tirada)
        onRollBtn.setOnClickListener{
            animateDiceRoll(diceImageView)
            animateDiceRoll(diceImageView2)
        }

    }

    private fun animateDiceRoll(diceImageView: ImageView) {
        val randomDiceValue = (1..6).random() // Genera un valor aleatorio entre 1 y 6

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
}
