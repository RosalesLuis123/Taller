package com.example.incidios2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Switch
import android.widget.Toast
import com.example.incidios2.R

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
    }
}
