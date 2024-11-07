package com.example.incidios2

import ApiService
import NewsResponse
import Noticia
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NewsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var noticiaAdapter: NoticiaAdapter
    private var noticias: List<Noticia> = emptyList()
    private val favoritosIds = mutableSetOf<String>() // Para almacenar los IDs de favoritos
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        recyclerView = findViewById(R.id.recyclerView)
        searchBar = findViewById(R.id.searchBar)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/") // URL base de NewsAPI
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getFireNews().enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    noticias = response.body()?.articles?.map { article ->
                        Noticia(
                            id = article.url, // Usando la URL como ID único
                            title = article.title,
                            description = article.description,
                            url = article.url,
                            urlToImage = article.urlToImage
                        )
                    } ?: emptyList()

                    // Obtener los favoritos desde Firestore
                    getUserFavorites()

                    // Asigna el adaptador después de obtener las noticias
                    noticiaAdapter = NoticiaAdapter(noticias, favoritosIds) { noticia, isFavorite ->
                        if (isFavorite) {
                            favoritosIds.add(noticia.id)
                            saveFavorite(noticia.id)
                        } else {
                            favoritosIds.remove(noticia.id)
                            removeFavorite(noticia.id)
                        }
                        // Actualizar el adaptador para mostrar los cambios
                        noticiaAdapter.notifyDataSetChanged()
                    }
                    recyclerView.adapter = noticiaAdapter
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Toast.makeText(this@NewsActivity, "Error al cargar noticias", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                val filteredNoticias = noticias.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            (it.description?.contains(query, ignoreCase = true) == true)
                }
                noticiaAdapter =
                    NoticiaAdapter(filteredNoticias, favoritosIds) { noticia, isFavorite ->
                        if (isFavorite) {
                            favoritosIds.add(noticia.id)
                            saveFavorite(noticia.id)
                        } else {
                            favoritosIds.remove(noticia.id)
                            removeFavorite(noticia.id)
                        }
                        noticiaAdapter.notifyDataSetChanged()
                    }
                recyclerView.adapter = noticiaAdapter
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_news // Seleccionar el ítem de configuración

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_fire -> {
                    startActivity(Intent(this, FireActivity::class.java))
                    true
                }
                R.id.navigation_news -> {
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

    // Función para obtener los favoritos del usuario desde Firestore
    private fun getUserFavorites() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                document?.get("favorites")?.let { favorites ->
                    favoritosIds.clear()
                    favoritosIds.addAll((favorites as List<String>))
                    noticiaAdapter.notifyDataSetChanged()
                }
            }
    }

    // Función para guardar una noticia en los favoritos del usuario
    private fun saveFavorite(newsId: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)
        userRef.update("favorites", FieldValue.arrayUnion(newsId))
    }

    // Función para eliminar una noticia de los favoritos del usuario
    private fun removeFavorite(newsId: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)
        userRef.update("favorites", FieldValue.arrayRemove(newsId))
    }
}
