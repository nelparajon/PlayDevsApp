package com.playdevsgame

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseManager(private val context: Context) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance() //instancia de la BD
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() //instancia FirebaseAuth para manejar la autenticación
    private lateinit var authListener: FirebaseAuth.AuthStateListener //Listener para manejar los cambios de autenticación
    private lateinit var mGoogleSignInClient: GoogleSignInClient //cliente de inicio de sesión de google

    companion object {
        private const val TAG = "FirebaseManager"
    }

    // Bloque de inicialización
    init {
        setupGoogleSignIn()

    }

    //configuramos las opciones de Google Sign-In y creamos una instancia de GoogleSignInClient.
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.playdevsgame.R.string.default_web_client_id))
            .requestEmail()
            //.setAccountName(null) // Agregar esta línea para mostrar el selector de cuentas
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    //devolvemos la instancia dek GoogleSignInClient para usarla en la Login Activity
    fun getGoogleSignInClient(): GoogleSignInClient {
        return mGoogleSignInClient
    }

    //método que agrega un listener que monitorea los cambios en el estado de autenticación
    //usamos una función lambda como argumento que acepta un parámetro lsitener: (-> Unit es = que Void en java)
    fun addAuthStateListener(listener: (FirebaseAuth) -> Unit) {
        //agrega el authListener al FirebaseAuth instance. Esto asegura que se reciba la notificación
        authListener = FirebaseAuth.AuthStateListener(listener)
        auth.addAuthStateListener(authListener)
    }

    //eliminamos el listener
    fun removeAuthStateListener() {
        auth.removeAuthStateListener(authListener)
    }

    fun signInWithGoogle(account: GoogleSignInAccount?, onComplete: (Boolean, FirebaseUser?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, auth.currentUser)
                } else {
                    onComplete(false, null)
                }
            }
    }

    fun checkAndAddUserToDatabase(user: FirebaseUser?) {
        val databaseReference = database.getReference("users")
        user?.let {
            val playerName = PreferenceManager.getPlayerName(context)
            databaseReference.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val userData = mapOf(
                            "name" to user.displayName,
                            "email" to user.email,
                            "userID" to playerName

                        )
                        databaseReference.child(user.uid).setValue(userData)
                            .addOnSuccessListener {
                                Log.d(TAG, "USUARIO AÑADIDO CON ÉXITO")
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "ERROR AL AÑADIR EL USUARIO")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "ERROR AL CONSULTAR LA BASE DE DATOS: ${error.message}")
                }
            })
        }
    }

    fun updateHighScoreInFirebase(userId: String, updateScore: String) {
        // Codificar el correo electrónico para usarlo como clave en Firebase


        // Crear la referencia a la ubicación del score en la base de datos
        val databaseReference = database.getReference("records").child(userId).child("record")

        // Actualizar el valor del score en la base de datos
        databaseReference.setValue(updateScore)
            .addOnSuccessListener {
                Log.d(TAG, "Puntuación actualizada con éxito")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error al actualizar la puntuación", exception)
            }
    }





    // Método para leer y actualizar el valor de score
     fun updateScoreToFirebase(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("scores").child(userId)

        // Leer el valor actual de score
        databaseReference.child("score").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val score = dataSnapshot.getValue(Int::class.java)
                if (score != null) {
                    // Convertir el score a cadena
                    val scoreString = score.toString()

                    // Actualizar el valor de score en la base de datos
                    val updates = hashMapOf<String, Any>(
                        "score" to scoreString
                    )
                    databaseReference.updateChildren(updates)
                        .addOnSuccessListener {
                            // Actualización exitosa
                            Log.d("Firebase", "Score actualizado a cadena exitosamente")
                        }
                        .addOnFailureListener {
                            // Manejar error de actualización
                            Log.d("Firebase", "Error al actualizar el score", it)
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar error de lectura
                Log.d("Firebase", "Error al leer el score", databaseError.toException())
            }
        })
    }

    //si fuese necesario usarlo.
    fun signOut(){
        auth.signOut()
    }
}
