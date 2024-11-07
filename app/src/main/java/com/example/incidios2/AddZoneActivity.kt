package com.example.incidios2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class AddZoneActivity : AppCompatActivity() {

    private lateinit var zoneNameEditText: EditText
    private lateinit var saveZoneButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_zone)

        zoneNameEditText = findViewById(R.id.zone_name_edit_text)
        saveZoneButton = findViewById(R.id.save_zone_button)

        saveZoneButton.setOnClickListener {
            val zoneName = zoneNameEditText.text.toString()
            if (zoneName.isNotEmpty()) {
                // Aquí podrías agregar la lógica para guardar la nueva zona en la base de datos
                Toast.makeText(this, "Zona '$zoneName' agregada", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad después de agregar la zona
            } else {
                Toast.makeText(this, "Por favor, ingresa un nombre para la zona", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
