
package com.playdevsgame


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LogInActivity : AppCompatActivity() {

    private lateinit var btnAuthGoogle: Button
    private lateinit var googleLogo: ImageView
    private lateinit var auth: FirebaseAuth
    private val authListener: FirebaseAuth.AuthStateListener = TODO()
    private var database: FirebaseDatabase
    private var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseManager: FirebaseManager
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        firebaseManager = FirebaseManager()

        // Inicialización de la UI
        btnAuthGoogle = findViewById(com.playdevsgame.R.id.btnAuthGoogle)
        googleLogo = findViewById(com.playdevsgame.R.id.googleLogo)

        // Inicialización de FirebaseAuth
        auth = Firebase.auth

        // Agregar un listener de estado de autenticación
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Usuario está autenticado
                Log.d("AuthState", "Usuario autenticado: ${user.email}")
            } else {
                // Usuario no está autenticado
                Log.d("AuthState", "Usuario no autenticado")
            }
        }

        auth.addAuthStateListener(authListener)

        // Inicialización de la base de datos de Firebase
        database = Firebase.database

        // Configuración de GoogleSignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.playdevsgame.R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Evento del botón para iniciar sesión
        btnAuthGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {
                // Mejor manejo del error, por ejemplo, mostrar un Snackbar con el error
                Toast.makeText(this, "Authentication Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    val user = FirebaseAuth.getInstance().currentUser
                    // Verificar y agregar el usuario a la base de datos
                   checkAndAddUserToDatabase(user, this)

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    Toast.makeText(this, "AUTENTICADO CON ÉXITO", Toast.LENGTH_LONG).show()
                    startActivity(intent)
                } else {
                    // Si el inicio de sesión falla, mostrar mensaje al usuario.
                    Toast.makeText(this, "AUTENTICACIÓN FALLIDA", Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun checkAndAddUserToDatabase(user: FirebaseUser?, context: Context) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        user?.let {
            val playerName = PreferenceManager.getPlayerName(context)
            databaseReference.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        // El usuario no existe, así que lo agregamos a la base de datos
                        val userData = mapOf(
                            "name" to user.displayName,
                            "email" to user.email,
                            "userID" to playerName
                        )
                        databaseReference.child(user.uid).setValue(userData)
                            .addOnSuccessListener {
                                // Usuario agregado exitosamente a la base de datos
                                Log.d("FIREBASE-DATABASE", "USUARIO AÑADIDO CON ÉXITO")
                            }
                            .addOnFailureListener {
                                Log.d("FIREBASE-DATABASE", "ERROR AL AÑADIR EL USUARIO")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error de cancelación
                    Log.d("FIREBASE-DATABASE", "ERROR AL CONSULTAR LA BASE DE DATOS: ${error.message}")
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authListener)
    }



}
