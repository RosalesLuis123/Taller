package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        setupViews()
    }

    private fun setupViews() {
        // Botón de registro
        findViewById<MaterialButton>(R.id.registerButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.confirmPasswordEditText).text.toString()

            if (validateInputs(email, password, confirmPassword)) {
                createAccount(email, password)
            }
        }

        // Link para ir al login
        findViewById<TextView>(R.id.loginLinkText).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Por favor completa todos los campos")
            return false
        }

        if (password != confirmPassword) {
            showToast("Las contraseñas no coinciden")
            return false
        }

        if (password.length < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres")
            return false
        }

        return true
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Cuenta creada exitosamente")
                    startActivity(Intent(this, FireActivity::class.java))
                    finish()
                } else {
                    showToast("Error al crear cuenta: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}