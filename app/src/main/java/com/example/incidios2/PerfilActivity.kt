package com.example.incidios2

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class PerfilActivity : AppCompatActivity() {

    private lateinit var userName: TextView
    private lateinit var deleteAccountButton: MaterialButton
    private lateinit var userEmail: TextView
    private lateinit var userProfileImage: ImageView
    private lateinit var editProfileButton: MaterialButton
    private lateinit var logoutButton: MaterialButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        userName = findViewById(R.id.user_name)
        userEmail = findViewById(R.id.user_email)
        deleteAccountButton = findViewById(R.id.delete_account_button)
        userProfileImage = findViewById(R.id.user_profile_image)
        editProfileButton = findViewById(R.id.edit_profile_button)
        logoutButton = findViewById(R.id.logout_button)

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            userName.text = it.displayName
            userEmail.text = it.email
            // Cargar la imagen de perfil si está disponible
            loadProfileImage(it.photoUrl)
        }

        // Botón Editar perfil
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        // Botón para borrar cuenta (redirige a la nueva actividad)
        deleteAccountButton.setOnClickListener {
            val intent = Intent(this, DeleteAccountActivity::class.java)
            startActivity(intent)
        }
        // Botón Cerrar sesión
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()  // Cierra la actividad actual
        }
    }

    // Función para cargar la imagen de perfil
    private fun loadProfileImage(photoUrl: Uri?) {
        photoUrl?.let {
            Glide.with(this).load(it).into(userProfileImage)
        } ?: run {
            // Si no hay foto, puedes poner una imagen predeterminada
            userProfileImage.setImageResource(R.drawable.ic_user)
        }
    }

    // Llamar a esta función cuando el perfil se haya actualizado (por ejemplo, al cambiar la foto)
    private fun updateProfilePhoto(newPhotoUrl: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(newPhotoUrl)
            .build()

        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Actualizar la UI con la nueva foto de perfil
                loadProfileImage(newPhotoUrl)
            }
        }
    }
}
