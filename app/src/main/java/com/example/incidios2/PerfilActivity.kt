package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class PerfilActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        auth = FirebaseAuth.getInstance()

        setupViews()
        setupUserInfo()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun setupViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupUserInfo() {
        val user = auth.currentUser
        user?.let { firebaseUser ->
            findViewById<TextView>(R.id.userName)?.text = firebaseUser.displayName ?: "Usuario"
            findViewById<TextView>(R.id.userEmail)?.text = firebaseUser.email
            findViewById<TextView>(R.id.profileInitial)?.text =
                (firebaseUser.displayName?.firstOrNull() ?: "U").toString()
        }
    }

    private fun setupClickListeners() {
        findViewById<LinearLayout>(R.id.editProfileOption)?.setOnClickListener {
            // Implementar edición de perfil
        }

        findViewById<LinearLayout>(R.id.changePasswordOption)?.setOnClickListener {
            // Implementar cambio de contraseña
        }

        findViewById<MaterialButton>(R.id.logoutButton)?.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        // Establecer el ítem seleccionado
        bottomNavigation.selectedItemId = R.id.navigation_settings

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_fire -> {
                    startActivity(Intent(this, FireActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_emergency -> {
                    startActivity(Intent(this, EmergencyActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_settings -> {
                    // Ya estamos en esta actividad
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation.selectedItemId = R.id.navigation_settings
    }
}