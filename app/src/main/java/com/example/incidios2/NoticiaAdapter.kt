package com.example.incidios2

import Noticia
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NoticiaAdapter(
    private var noticias: List<Noticia>,
    private val favoritosIds: Set<String>,
    private val onFavoriteClick: (Noticia, Boolean) -> Unit
) : RecyclerView.Adapter<NoticiaAdapter.NoticiaViewHolder>() {

    class NoticiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleTextView)
        val description: TextView = itemView.findViewById(R.id.descriptionTextView)
        val image: ImageView = itemView.findViewById(R.id.imageView)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
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

        // Configura el botón de favoritos
        holder.favoriteButton.setImageResource(
            if (favoritosIds.contains(noticia.id)) R.drawable.ic_star_filled
            else R.drawable.ic_star_outline
        )

        holder.favoriteButton.setOnClickListener {
            val isFavorite = !favoritosIds.contains(noticia.id)
            onFavoriteClick(noticia, isFavorite)
            notifyItemChanged(position) // Notifica el cambio para actualizar el icono
        }
    }
    override fun getItemCount(): Int = noticias.size
}
