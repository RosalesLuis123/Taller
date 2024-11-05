@file:Suppress("DEPRECATION")

package com.example.incidios2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        passwordEditText = findViewById(R.id.passwordEditText)
        passwordLayout = findViewById(R.id.passwordLayout)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // Reemplaza con tu Web Client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = passwordEditText.text.toString()
            signInWithEmailPassword(email, password)
        }

        // Create Account text
        findViewById<TextView>(R.id.createAccountText).setOnClickListener {
            showCreateAccountDialog()
        }

        // Forgot Password text
        findViewById<TextView>(R.id.forgotPasswordText).setOnClickListener {
            showForgotPasswordDialog()
        }

        // Social login buttons
        findViewById<ImageView>(R.id.googleIcon).setOnClickListener {
            startGoogleSignIn()
        }
    }

    private fun showCreateAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_account, null)
        val emailEdit = dialogView.findViewById<EditText>(R.id.emailEditText)
        val passwordEdit = dialogView.findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEdit = dialogView.findViewById<EditText>(R.id.confirmPasswordEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Crear cuenta")
            .setView(dialogView)
            .setPositiveButton("Crear") { dialog, _ ->
                val email = emailEdit.text.toString()
                val password = passwordEdit.text.toString()
                val confirmPassword = confirmPasswordEdit.text.toString()

                if (password == confirmPassword) {
                    createAccount(email, password)
                } else {
                    showToast("Las contraseñas no coinciden")
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        // Aplicar fondo blanco
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        // Cambiar el color del texto del título y de los botones
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)

            // Cambiar el color del título
            dialog.window?.decorView?.findViewById<TextView>(android.R.id.title)?.setTextColor(Color.BLACK)

            // Cambiar el color de los EditText
            emailEdit.setTextColor(Color.BLACK)
            passwordEdit.setTextColor(Color.BLACK)
            confirmPasswordEdit.setTextColor(Color.BLACK)
        }

        dialog.show()
    }

    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val emailEdit = dialogView.findViewById<EditText>(R.id.emailEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Recuperar contraseña")
            .setView(dialogView)
            .setPositiveButton("Enviar") { dialog, _ ->
                val email = emailEdit.text.toString()
                resetPassword(email)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        // Aplicar fondo blanco
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        // Cambiar el color del texto del título y de los botones
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)

            // Cambiar el color del título
            dialog.window?.decorView?.findViewById<TextView>(android.R.id.title)?.setTextColor(Color.BLACK)

            // Cambiar el color de los EditText
            emailEdit.setTextColor(Color.BLACK)
        }

        dialog.show()
    }

    private fun createAccount(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor completa todos los campos")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Cuenta creada exitosamente")
                    navigateToMain()
                } else {
                    showToast("Error al crear cuenta: ${task.exception?.message}")
                }
            }
    }

    private fun resetPassword(email: String) {
        if (email.isEmpty()) {
            showToast("Por favor ingresa tu email")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Se ha enviado un correo para restablecer tu contraseña")
                } else {
                    showToast("Error al enviar el correo: ${task.exception?.message}")
                }
            }
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showToast("Error en inicio de sesión con Google: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    showToast("Error en autenticación con Google")
                }
            }
    }

    private fun signInWithEmailPassword(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor completa todos los campos")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    showToast("Error en inicio de sesión: ${task.exception?.message}")
                }
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, FireActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}