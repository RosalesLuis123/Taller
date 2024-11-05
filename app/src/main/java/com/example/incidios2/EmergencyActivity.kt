// EmergencyActivity.kt
package com.example.incidios2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.incidios2.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class EmergencyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        // Configuración de cada botón con su número de teléfono
        setupEmergencyButton(R.id.btnFirefighters, "109")
        setupEmergencyButton(R.id.btnPolice, "66-42222")
        setupEmergencyButton(R.id.btnPatrol, "66-43333")
        setupEmergencyButton(R.id.btnAmbulance, "114")
        setupEmergencyButton(R.id.btnSAR, "118")
        setupEmergencyButton(R.id.btnFelcc, "110")


        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_emergency // Seleccionar el ítem de configuración

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
                    true
                }
                R.id.navigation_settings -> {
                    // Ya estamos en settings
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupEmergencyButton(buttonId: Int, phoneNumber: String) {
        val button = findViewById<LinearLayout>(buttonId)
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }
    }

}
