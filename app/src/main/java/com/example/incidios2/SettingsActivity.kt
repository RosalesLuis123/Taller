package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SettingsActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userEmailTextView: TextView
    private lateinit var logoutButton: RelativeLayout
    private lateinit var manageZonesButton: RelativeLayout
    private lateinit var userProfileImage: ImageView
    private lateinit var PERFIL_button: RelativeLayout
    private lateinit var userNameTextView: TextView // Nueva variable para el nombre del usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Inicializa FirebaseAuth y componentes de la interfaz
        mAuth = FirebaseAuth.getInstance()
        userEmailTextView = findViewById(R.id.user_email)
        userNameTextView = findViewById(R.id.user_name) // Inicializa el TextView para el nombre
        logoutButton = findViewById(R.id.logout_button)
        manageZonesButton = findViewById(R.id.manage_zones_button)
        PERFIL_button = findViewById(R.id.PERFIL_button)
        userProfileImage = findViewById(R.id.user_profile_image)


        // Recupera el correo del usuario desde Firebase y mostrarlo
        val currentUser: FirebaseUser? = mAuth.currentUser
        currentUser?.let {
            userEmailTextView.text = it.email
            userNameTextView.text = it.displayName // Establece el nombre del usuario en el TextView
            // Cargar la imagen de perfil si está disponible
            Glide.with(this).load(it.photoUrl).into(userProfileImage)
        }

        // Botón de Cerrar sesión
        logoutButton.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Cierra la actividad actual
        }


        // Configurar el botón "Gestionar zonas" para que navegue a la actividad correspondiente
        manageZonesButton.setOnClickListener {
            val intent = Intent(this, GestionarZonasActivity::class.java) // Nueva actividad que gestionará las zonas
            startActivity(intent)
        }

        // Redirigir a la actividad de perfil cuando se haga clic en la imagen de perfil
        PERFIL_button.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        // Configuración de la barra de navegación inferior
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
