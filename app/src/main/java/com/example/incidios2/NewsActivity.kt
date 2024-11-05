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

class NewsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var noticiaAdapter: NoticiaAdapter
    private var noticias: List<Noticia> = emptyList()
    private val favoritosIds = mutableSetOf<String>() // Para almacenar los IDs de favoritos

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

                    // Asigna el adaptador después de obtener las noticias
                    noticiaAdapter = NoticiaAdapter(noticias, favoritosIds) { noticia, isFavorite ->
                        if (isFavorite) {
                            favoritosIds.add(noticia.id)
                        } else {
                            favoritosIds.remove(noticia.id)
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
                        } else {
                            favoritosIds.remove(noticia.id)
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
                    // Navegar a la actividad de fuego
                    startActivity(Intent(this, FireActivity::class.java))
                    true
                }
                R.id.navigation_news -> {
                    // Navegar a la actividad de documento/noticias
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
}
