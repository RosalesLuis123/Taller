package com.example.incidios2

import ApiService
import NewsResponse
import Noticia
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class NewsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var filterButtonFavorite: MaterialButton // Agregar la referencia del botón de favoritos
    private lateinit var noticiaAdapter: NoticiaAdapter
    private var noticias: List<Noticia> = emptyList()
    private val favoritosIds = mutableSetOf<String>() // Para almacenar los IDs de favoritos
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var isFilteringFavorites = false // Flag para saber si estamos filtrando
    private lateinit var noFavoritesMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        recyclerView = findViewById(R.id.recyclerView)
        searchBar = findViewById(R.id.searchBar)
        filterButtonFavorite = findViewById(R.id.filterButtonFavorite) // Asignación del botón de favoritos
        recyclerView.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getFireNews().enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    noticias = response.body()?.articles?.map { article ->
                        Noticia(
                            id = article.url,
                            title = article.title,
                            description = article.description,
                            url = article.url,
                            urlToImage = article.urlToImage,
                            publishedAt = article.publishedAt,
                            content = article.content
                        )
                    } ?: emptyList()

                    // Obtener los favoritos desde Firestore
                    getUserFavorites()

                    // Esperar que los favoritos estén listos antes de asignar el adaptador
                    noticiaAdapter = NoticiaAdapter(noticias, favoritosIds, { noticia, isFavorite ->
                        // Lógica de favoritos
                        if (isFavorite) {
                            favoritosIds.add(noticia.id)
                            saveFavorite(noticia.id)
                        } else {
                            favoritosIds.remove(noticia.id)
                            removeFavorite(noticia.id)
                        }
                        noticiaAdapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
                    }, { url ->
                        // Lógica de "Ver más"
                        val intent = Intent(this@NewsActivity, NewsDetailActivity::class.java)
                        intent.putExtra("url", url) // Pasa la URL de la noticia
                        startActivity(intent)
                    })

                    recyclerView.adapter = noticiaAdapter
                } else {
                    Toast.makeText(this@NewsActivity, "Error al cargar noticias", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@NewsActivity, "Error al cargar noticias", Toast.LENGTH_SHORT).show()
                }
            }
        })

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()

                // Filtrar las noticias basadas en el texto de búsqueda
                val filteredNoticias = noticias.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            (it.description?.contains(query, ignoreCase = true) == true)
                }

                // Crear el adaptador con los datos filtrados
                noticiaAdapter = NoticiaAdapter(filteredNoticias, favoritosIds, { noticia, isFavorite ->
                    // Lógica de favoritos
                    if (isFavorite) {
                        favoritosIds.add(noticia.id)
                        saveFavorite(noticia.id)
                    } else {
                        favoritosIds.remove(noticia.id)
                        removeFavorite(noticia.id)
                    }
                    noticiaAdapter.notifyDataSetChanged() // Notifica que los datos han cambiado
                }, { url ->
                    // Lógica de "Ver más"
                    val intent = Intent(this@NewsActivity, NewsDetailActivity::class.java)
                    intent.putExtra("url", url) // Pasa la URL de la noticia
                    startActivity(intent)
                })

                // Establecer el adaptador en el RecyclerView
                recyclerView.adapter = noticiaAdapter
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        filterButtonFavorite.setOnClickListener {
            isFilteringFavorites = !isFilteringFavorites // Cambia el estado de filtrado

            // Filtra las noticias según el estado de los favoritos
            val filteredNoticias = if (isFilteringFavorites) {
                noticias.filter { favoritosIds.contains(it.id) }
            } else {
                noticias
            }
            // Mostrar u ocultar el mensaje de "No hay favoritos"
            noFavoritesMessage= findViewById(R.id.noFavoritesMessage)
            if (isFilteringFavorites && filteredNoticias.isEmpty()) {
                noFavoritesMessage.visibility = View.VISIBLE
            } else {
                noFavoritesMessage.visibility = View.GONE
            }

            // Crea una nueva instancia del adaptador con las noticias filtradas
            noticiaAdapter = NoticiaAdapter(filteredNoticias, favoritosIds, { noticia, isFavorite ->
                if (isFavorite) {
                    favoritosIds.add(noticia.id)
                    saveFavorite(noticia.id)
                } else {
                    favoritosIds.remove(noticia.id)
                    removeFavorite(noticia.id)
                }
                noticiaAdapter.notifyDataSetChanged()
            }, { url ->
                val intent = Intent(this@NewsActivity, NewsDetailActivity::class.java)
                intent.putExtra("url", url) // Pasa la URL de la noticia
                startActivity(intent)
            })

            recyclerView.adapter = noticiaAdapter

            // Cambia el texto del botón según el estado del filtro
            filterButtonFavorite.text = if (isFilteringFavorites) "Ver todas" else "Favoritos"
            val iconRes = if (isFilteringFavorites) R.drawable.icon_ver_todo else R.drawable.icon_favorite_activo
            filterButtonFavorite.setIconResource(iconRes)
        }


        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_news

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

    private fun getUserFavorites() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    favoritosIds.clear()
                    favoritosIds.addAll(favorites)
                    noticiaAdapter.notifyDataSetChanged()
                } else {
                    userRef.set(mapOf("favorites" to emptyList<String>()))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar favoritos", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveFavorite(newsId: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.update("favorites", FieldValue.arrayUnion(newsId))
            .addOnFailureListener {
                // Si falla, intenta crear el campo favorites con el ID de la noticia
                userRef.set(mapOf("favorites" to listOf(newsId)), SetOptions.merge())
            }
    }


    private fun removeFavorite(newsId: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.update("favorites", FieldValue.arrayRemove(newsId))
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar favorito", Toast.LENGTH_SHORT).show()
            }
    }

}