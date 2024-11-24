package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        // Configurar botón de crear cuenta
        findViewById<MaterialButton>(R.id.createAccountButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        // Configurar botón de iniciar sesión
        findViewById<MaterialButton>(R.id.loginButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}