package com.example.incidios2

import Noticia
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class NoticiaAdapter(
    private var noticias: List<Noticia>,
    private val favoritosIds: MutableSet<String>,  // Cambiado a MutableSet
    private val onFavoriteClick: (Noticia, Boolean) -> Unit,
    private val onVerMasClick: (String) -> Unit // Funcion para "Ver más"
) : RecyclerView.Adapter<NoticiaAdapter.NoticiaViewHolder>() {

    class NoticiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleTextView)
        val description: TextView = itemView.findViewById(R.id.descriptionTextView)
        val image: ImageView = itemView.findViewById(R.id.imageView)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        val verMasButton: MaterialButton = itemView.findViewById(R.id.verMasButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_noticia, parent, false)
        return NoticiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) {
        val noticia = noticias[position]

        holder.title.text = noticia.title
        holder.description.text = noticia.description ?: ""
        Glide.with(holder.itemView.context).load(noticia.urlToImage).into(holder.image)

        // Botón de favoritos
        holder.favoriteButton.setImageResource(
            if (favoritosIds.contains(noticia.id)) R.drawable.ic_star_filled
            else R.drawable.ic_star_outline
        )

        holder.favoriteButton.setOnClickListener {
            val isFavorite = !favoritosIds.contains(noticia.id)
            onFavoriteClick(noticia, isFavorite)
            notifyItemChanged(position) // Actualiza el item
        }

        // Botón "Ver más"
        holder.verMasButton.setOnClickListener {
            onVerMasClick(noticia.url) // Pasa la URL de la noticia
        }

    }

    override fun getItemCount(): Int = noticias.size
}
