package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Switch
import android.widget.Toast
import com.example.incidios2.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Inicializa los componentes
        switchDarkMode = findViewById(R.id.switch_dark_mode)

        // Configurar el modo oscuro
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Activar modo oscuro
                Toast.makeText(this, "Modo oscuro activado", Toast.LENGTH_SHORT).show()
                // Aquí puedes añadir lógica para activar el modo oscuro
            } else {
                // Desactivar modo oscuro
                Toast.makeText(this, "Modo oscuro desactivado", Toast.LENGTH_SHORT).show()
                // Aquí puedes añadir lógica para desactivar el modo oscuro
            }
        }
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
                    startActivity(Intent(this, NewsActivity::class.java))
                    true
                }
                R.id.navigation_emergency -> {
                    startActivity(Intent(this, EmergencyActivity::class.java))
                    true
                }
                R.id.navigation_donation -> {
                    startActivity(Intent(this, DonationActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    // Ya estamos en settings
                    true
                }
                else -> false
            }
        }
    }
}
