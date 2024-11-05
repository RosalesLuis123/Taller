package com.example.incidios2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException

class FireActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var searchBar: EditText
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire)

        // Configuración de OSMDroid
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        mapView = findViewById(R.id.mapView)
        searchBar = findViewById(R.id.searchBar)
        searchButton = findViewById(R.id.searchButton)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(5.0)
        mapView.controller.setCenter(GeoPoint(-16.2902, -63.5887)) // Centrar en Bolivia

        fetchFireData()

        searchButton.setOnClickListener { searchLocation() }
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_fire // Seleccionar el ítem de configuración

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
                R.id.navigation_settings -> {
                    // Ya estamos en settings
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchFireData() {
        val url = "https://firms.modaps.eosdis.nasa.gov/api/area/csv/38cd0617b2cf0dce3e5e92461671c67f/VIIRS_SNPP_NRT/world/1/2024-10-27"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string() ?: return
                runOnUiThread {
                    parseFireData(data)
                }
            }
        })
    }

    private fun parseFireData(data: String) {
        try {
            val lines = data.split("\n")
            for (i in 1 until lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty()) continue

                val parts = line.split(",")
                if (parts.size < 3) continue

                val lat = parts[0].toDoubleOrNull() ?: continue
                val lon = parts[1].toDoubleOrNull() ?: continue
                val intensity = parts[2].toDoubleOrNull() ?: continue

                val color = when {
                    intensity < 150 -> Color.GREEN
                    intensity in 150.0..250.0 -> Color.YELLOW
                    intensity in 250.0..300.0 -> Color.parseColor("#FFA500")
                    else -> Color.RED
                }

                addFireMarker(GeoPoint(lat, lon), color)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addFireMarker(location: GeoPoint, color: Int) {
        val marker = Marker(mapView)
        marker.position = location
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = createColoredMarkerIcon(color)
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun createColoredMarkerIcon(color: Int): BitmapDrawable? {
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.FILL

        val size = 40
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        return BitmapDrawable(resources, bitmap)
    }


    private fun searchLocation() {
        val locationName = searchBar.text.toString().trim()
        if (locationName.isNotEmpty()) {
            // Aquí puedes usar un Geocoder o la API de Google para buscar la ubicación
            val geocoder = android.location.Geocoder(this)
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val geoPoint = GeoPoint(address.latitude, address.longitude)

                    // Centrar el mapa en la ubicación encontrada
                    mapView.controller.animateTo(geoPoint)
                    mapView.controller.setZoom(10.0) // Ajusta el zoom según sea necesario

                    // Opcional: Puedes agregar un marcador en la ubicación buscada
                    addFireMarker(
                        geoPoint,
                        Color.BLUE
                    ) // Usar un color diferente para el marcador de búsqueda
                } else {
                    // Manejo de caso cuando no se encuentra la ubicación
                    // Puedes mostrar un Toast o algún mensaje
                }
            }
        }
    }}
