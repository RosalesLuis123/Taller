package com.example.incidios2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.incidios2.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

class DonationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

        // Permiso de escritura para la descarga de imágenes
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        setupDownloadButton(R.id.btnFirefightersQuebracho, R.drawable.bomberos_quebracho, "bomberos_quebracho.png")
        setupDownloadButton(R.id.btnFirefightersRescateUrbano, R.drawable.bomberos_rescateurbano, "bomberos_rescateurbano.png")
        setupDownloadButton(R.id.btnFirefightersVoluntariosGuarayos, R.drawable.bomberos_voluntariosguarayos, "bomberos_voluntariosguarayos.png")

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_donation // Seleccionar el ítem de configuración

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_fire -> {
                    // Navegar a la actividad de fuego
                    true
                }
                R.id.navigation_news -> {
                    // Navegar a la actividad de documento/noticias
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
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDownloadButton(buttonId: Int, drawableId: Int, fileName: String) {
        val button = findViewById<LinearLayout>(buttonId)
        button.setOnClickListener {
            val drawable = ContextCompat.getDrawable(this, drawableId)
            drawable?.let {
                downloadImage(it, fileName)
            }
        }
    }

    private fun downloadImage(drawable: Drawable, fileName: String) {
        try {
            // Directorio de descargas
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            // Convertir Drawable a Bitmap y guardar en archivo
            val bitmap = (drawable as android.graphics.drawable.BitmapDrawable).bitmap
            val outputStream = FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            Toast.makeText(this, "Imagen descargada: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al descargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
}
