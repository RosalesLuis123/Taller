package com.example.incidios2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.app.Zona
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FireActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var searchBar: AutoCompleteTextView
    private lateinit var searchButton: ImageButton
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val zoneNames = mutableListOf<String>()
    private val zonesMap = mutableMapOf<String, Zona>()
    private lateinit var buttonBueno: Button
    private lateinit var buttonMedia: Button
    private lateinit var buttonMalo: Button
    private lateinit var buttonGrave: Button

    private val buenoMarkers = mutableListOf<Marker>()
    private val mediaMarkers = mutableListOf<Marker>()
    private val maloMarkers = mutableListOf<Marker>()
    private val graveMarkers = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        mapView = findViewById(R.id.mapView)
        searchBar = findViewById(R.id.searchBar)
        searchButton = findViewById(R.id.searchButton)
        buttonBueno = findViewById(R.id.buttonBueno)
        buttonMedia = findViewById(R.id.buttonMedia)
        buttonMalo = findViewById(R.id.buttonMalo)
        buttonGrave = findViewById(R.id.buttonGrave)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(5.0)
        mapView.controller.setCenter(GeoPoint(-16.2902, -63.5887)) // Centrar en Bolivia

        fetchFireData()

        searchButton.setOnClickListener { searchLocation() }
        setupBottomNavigation()

        buttonBueno.setOnClickListener { mostrarMarcadores(buenoMarkers) }
        buttonMedia.setOnClickListener { mostrarMarcadores(mediaMarkers) }
        buttonMalo.setOnClickListener { mostrarMarcadores(maloMarkers) }
        buttonGrave.setOnClickListener { mostrarMarcadores(graveMarkers) }
        loadZones()

        searchButton.setOnClickListener {
            val query = searchBar.text.toString()
            val zona = zonesMap[query]
            openZonaDetails(zona)
        }
    }

    private fun loadZones() {
        currentUser?.let { user ->
            firestore.collection("users").document(user.uid).collection("zonas")
                .get()
                .addOnSuccessListener { documents ->
                    zoneNames.clear()
                    zonesMap.clear()
                    for (document in documents) {
                        val nombre = document.getString("nombre") ?: ""
                        val geoPoint = document.getGeoPoint("coordenadas") ?: com.google.firebase.firestore.GeoPoint(0.0, 0.0)
                        val rango = document.getLong("rango")?.toInt() ?: 0
                        val nombreUbicacion = document.getString("nombreUbicacion") ?: ""

                        val coordenadas = LatLng(geoPoint.latitude, geoPoint.longitude)
                        val zona = Zona(
                            nombre = nombre,
                            coordenadas = coordenadas,
                            rango = rango,
                            nombreUbicacion = nombreUbicacion
                        )

                        zoneNames.add(nombre)
                        if (nombreUbicacion.isNotEmpty()) {
                            zoneNames.add(nombreUbicacion)
                        }

                        zonesMap[nombre] = zona
                        if (nombreUbicacion.isNotEmpty()) {
                            zonesMap[nombreUbicacion] = zona
                        }
                    }
                    setupSearchAutoComplete()
                }
                .addOnFailureListener { e ->
                    Log.w("FireActivity", "Error loading zones", e)
                }
        }
    }

    private fun setupSearchAutoComplete() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, zoneNames)
        searchBar.setAdapter(adapter)
        searchBar.threshold = 1
    }

    private fun openZonaDetails(zona: Zona?) {
        if (zona != null) {
            Log.d("FireActivity", "Selected Zone: ${zona.nombre}")
            mostrarMarcadoresEnRango(zona)
        }
    }

    private fun mostrarMarcadoresEnRango(zona: Zona) {
        mapView.overlays.clear()

        val radiusMeters = zona.rango * 1000.0
        val zonaCenter = GeoPoint(zona.coordenadas.latitude, zona.coordenadas.longitude)

        val circle = Polygon(mapView).apply {
            points = Polygon.pointsAsCircle(zonaCenter, radiusMeters)
            fillColor = Color.argb(50, 0, 0, 255)
            strokeColor = Color.BLUE
            strokeWidth = 2f
        }
        mapView.overlays.add(circle)

        val markersInRange = mutableListOf<Marker>()
        for (markerList in listOf(buenoMarkers, mediaMarkers, maloMarkers, graveMarkers)) {
            for (marker in markerList) {
                val distance = marker.position.distanceToAsDouble(zonaCenter)
                if (distance <= radiusMeters) {
                    markersInRange.add(marker)
                }
            }
        }

        mapView.overlays.addAll(markersInRange)
        mapView.controller.setCenter(zonaCenter)
        mapView.controller.setZoom(10.0)
        mapView.invalidate()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_fire

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_fire -> true
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
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }



    private fun fetchFireData() {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val url = "https://firms.modaps.eosdis.nasa.gov/api/area/csv/38cd0617b2cf0dce3e5e92461671c67f/VIIRS_SNPP_NRT/world/1/$currentDate"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val data = response.body?.string() ?: return@launch

                    withContext(Dispatchers.Main) {
                        parseFireData(data)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun parseFireData(data: String) {
        try {
            val lines = data.lineSequence()
                .drop(1)
                .filter { it.isNotBlank() }
                .map { it.trim().split(",") }
                .filter { it.size >= 3 }
                .mapNotNull { parts ->
                    val lat = parts[0].toDoubleOrNull() ?: return@mapNotNull null
                    val lon = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                    val intensity = parts[2].toDoubleOrNull() ?: return@mapNotNull null
                    Triple(lat, lon, intensity)
                }
                .filter { (lat, lon, _) -> lat in -22.0..-10.0 && lon in -69.5..-57.5 }

            lines.forEach { (lat, lon, intensity) ->
                val (color, markerList) = when {
                    intensity < 150 -> Color.GREEN to buenoMarkers
                    intensity in 150.0..250.0 -> Color.YELLOW to mediaMarkers
                    intensity in 250.0..300.0 -> Color.parseColor("#FFA500") to maloMarkers
                    else -> Color.RED to graveMarkers
                }

                val marker = addFireMarker(GeoPoint(lat, lon), color)
                markerList.add(marker)
            }

            mapView.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addFireMarker(location: GeoPoint, color: Int): Marker {
        val marker = Marker(mapView)
        marker.position = location
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = createColoredMarkerIcon(color)
        mapView.overlays.add(marker)
        return marker
    }

    private fun createColoredMarkerIcon(color: Int): BitmapDrawable? {
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        }

        val size = 40
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        return BitmapDrawable(resources, bitmap)
    }

    private fun mostrarMarcadores(markersToShow: List<Marker>) {
        mapView.overlays.clear()
        mapView.overlays.addAll(markersToShow)
        mapView.invalidate()
    }

    private fun searchLocation() {
        val locationName = searchBar.text.toString().trim()
        if (locationName.isNotEmpty()) {
            val geocoder = android.location.Geocoder(this)
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val geoPoint = GeoPoint(address.latitude, address.longitude)

                mapView.controller.animateTo(geoPoint)
                mapView.controller.setZoom(10.0)

                val searchMarker = addFireMarker(geoPoint, Color.BLUE)
                mapView.overlays.add(searchMarker)
            }
        }
    }
}
