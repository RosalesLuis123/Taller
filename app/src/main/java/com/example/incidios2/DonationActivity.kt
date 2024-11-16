package com.example.incidios2

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.incidios2.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.OutputStream

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class DonationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

        // Inicializar el fragmento del mapa
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)

        // Verificar permiso para acceder al almacenamiento
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        // Ejemplo con coordenadas para cada botón
        setupDownloadButton(R.id.btnFirefightersQuebracho, R.drawable.bomberos_quebracho, "bomberos_quebracho.png", -18.33111111, -59.75694444) // Coordenadas Robore
        setupDownloadButton(R.id.btnFirefightersRescateUrbano, R.drawable.bomberos_rescateurbano, "bomberos_rescateurbano.png", -17.801617520643106, -63.13382423295541) // Coordenadas Santa Cruz
        setupDownloadButton(R.id.btnFirefightersVoluntariosGuarayos, R.drawable.bomberos_voluntariosguarayos, "bomberos_voluntariosguarayos.png", -15.897006244102847, -63.186235782957574) // Coordenadas Ascencion de Guarayos

        // Configuración de la barra de navegación inferior
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_donation

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_fire -> {
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
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Establece una ubicación predeterminada
        val defaultLocation = LatLng(-17.801617520643106, -63.13382423295541) // Santa Cruz
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
    }

    private fun updateMapLocation(lat: Double, lon: Double) {
        val location = LatLng(lat, lon)
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(location))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }

    // Ajusta el método setupDownloadButton para incluir coordenadas
    private fun setupDownloadButton(buttonId: Int, drawableId: Int, fileName: String, lat: Double, lon: Double) {
        val button = findViewById<LinearLayout>(buttonId)
        button.setOnClickListener {
            val drawable = ContextCompat.getDrawable(this, drawableId)
            drawable?.let {
                downloadImage(it, fileName)
                updateMapLocation(lat, lon) // Actualizar el mapa con las coordenadas especificadas
            }
        }
    }

    /*private fun setupDownloadButton(buttonId: Int, drawableId: Int, fileName: String) {
        val button = findViewById<LinearLayout>(buttonId)
        button.setOnClickListener {
            val drawable = ContextCompat.getDrawable(this, drawableId)
            drawable?.let {
                downloadImage(it, fileName)
            }
        }
    }*/

    private fun downloadImage(drawable: Drawable, fileName: String) {
        try {
            // Configurar los detalles de la imagen para guardarla en MediaStore, usando la carpeta "Pictures"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/")
            }

            // Obtener el resolver de contenido y la URI para guardar la imagen
            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    val bitmap = (drawable as android.graphics.drawable.BitmapDrawable).bitmap
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    Toast.makeText(this, "Imagen descargada en la carpeta Pictures: $fileName", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, "No se pudo abrir el flujo de salida", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Error al crear el archivo", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al descargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

}
