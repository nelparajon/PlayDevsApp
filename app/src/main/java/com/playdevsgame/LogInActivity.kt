
package com.playdevsgame


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException


class LogInActivity : AppCompatActivity() {

    private lateinit var btnAuthGoogle: Button
    private lateinit var googleLogo: ImageView
    private lateinit var firebaseManager: FirebaseManager

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        firebaseManager = FirebaseManager(this)

        // Inicialización de la UI
        btnAuthGoogle = findViewById(com.playdevsgame.R.id.btnAuthGoogle)
        googleLogo = findViewById(com.playdevsgame.R.id.googleLogo)

        // Agregar un listener de estado de autenticación
        firebaseManager.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user != null) {
                Log.d("AuthState", "Usuario autenticado: ${user.email}")
            } else {
                Log.d("AuthState", "Usuario no autenticado")
            }
        }

        // Evento del botón para iniciar sesión
        btnAuthGoogle.setOnClickListener {
            signInWithGoogle()

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseManager.signInWithGoogle(account) { isSuccess, user ->
                    if (isSuccess) {
                        firebaseManager.checkAndAddUserToDatabase(user)
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        Toast.makeText(this, "AUTENTICADO CON ÉXITO", Toast.LENGTH_LONG).show()
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "AUTENTICACIÓN FALLIDA", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Authentication Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent: Intent = firebaseManager.getGoogleSignInClient().signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseManager.removeAuthStateListener()

    }
}
