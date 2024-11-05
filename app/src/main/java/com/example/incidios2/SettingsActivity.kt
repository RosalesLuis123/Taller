package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Configurar la barra de navegación
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_settings // Seleccionar el ítem de configuración

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_fire -> {
                    // Navegar a la actividad de fuego
                    startActivity(Intent(this, FireActivity::class.java))
                    true
                }
                R.id.navigation_news -> {
                    // Navegar a la actividad de documento/noticias
                    startActivity(Intent(this, NewsActivity::class.java))
                    true
                }
                R.id.navigation_emergency -> {
                    // Navegar a la actividad de emergencia
                    startActivity(Intent(this, EmergencyActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    // Ya estamos en settings
                    true
                }
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}