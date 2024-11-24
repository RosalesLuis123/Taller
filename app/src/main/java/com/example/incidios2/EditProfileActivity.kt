package com.example.incidios2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton
import android.widget.EditText
import com.bumptech.glide.Glide
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private val PICK_IMAGE_REQUEST = 71
    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        profileImageView = findViewById(R.id.profileImageView)
        nameEditText = findViewById(R.id.nameEditText)
        saveButton = findViewById(R.id.saveButton)

        // Mostrar la imagen y nombre del usuario actual
        val currentUser = auth.currentUser
        currentUser?.let {
            nameEditText.setText(it.displayName)

            if (it.photoUrl != null) {
                Glide.with(this)
                    .load(it.photoUrl)
                    .into(profileImageView)
            }
        }

        // Solicitar permisos de almacenamiento si no se tienen
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        // Seleccionar una nueva foto
        profileImageView.setOnClickListener {
            openImagePicker()
        }

        // Guardar los cambios
        saveButton.setOnClickListener {
            val newName = nameEditText.text.toString()
            if (newName.isNotEmpty()) {
                updateUserProfile(newName)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data

            if (imageUri != null) {
                // Subir la imagen seleccionada a Firebase Storage
                val filePath = storageRef.child("profile_pictures/${auth.currentUser?.uid}.jpg")
                filePath.putFile(imageUri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        filePath.downloadUrl.addOnCompleteListener { urlTask ->
                            if (urlTask.isSuccessful) {
                                val downloadUri = urlTask.result
                                updateUserProfilePicture(downloadUri)
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error al subir la imagen: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserProfile(newName: String) {
        val currentUser = auth.currentUser
        currentUser?.let {
            // Actualizar nombre
            it.updateProfile(
                userProfileChangeRequest {
                    displayName = newName
                }
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al actualizar el perfil: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUserProfilePicture(uri: Uri?) {
        val currentUser = auth.currentUser
        currentUser?.let {
            it.updateProfile(
                userProfileChangeRequest {
                    photoUri = uri
                }
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()

                    // Actualizar la imagen de perfil en el ImageView
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_user) // Imagen predeterminada mientras carga
                        .error(R.drawable.ic_error) // Imagen en caso de error
                        .into(profileImageView)
                } else {
                    Toast.makeText(this, "Error al actualizar la imagen: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Manejo de permisos en caso de que el usuario no los haya concedido
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                openImagePicker()
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de almacenamiento necesario", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
