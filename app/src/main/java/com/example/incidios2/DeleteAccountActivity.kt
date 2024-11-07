package com.example.incidios2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var deleteAccountButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        deleteAccountButton = findViewById(R.id.delete_account_button)

        // Eliminar la cuenta del usuario
        deleteAccountButton.setOnClickListener {
            val mAuth = FirebaseAuth.getInstance()
            val currentUser = mAuth.currentUser
            currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Cuenta eliminada exitosamente
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish() // Cierra la actividad
                } else {
                    // Manejar error de eliminación de cuenta
                }
            }
        }
    }
}
