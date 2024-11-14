package com.example.incidios2

import Noticia
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NoticiaAdapter(
    private var noticias: List<Noticia>,
    private val favoritosIds: MutableSet<String>,  // Cambiado a MutableSet
    private val onFavoriteClick: (Noticia, Boolean) -> Unit,
    private val onVerMasClick: (String) -> Unit // Funcion para "Ver más"
) : RecyclerView.Adapter<NoticiaAdapter.NoticiaViewHolder>() {

    class NoticiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleTextView)
       // val description: TextView = itemView.findViewById(R.id.descriptionTextView)
        val image: ImageView = itemView.findViewById(R.id.imageView)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        val verMasButton: MaterialButton = itemView.findViewById(R.id.verMasButton)
        val FechaPublicacion: TextView = itemView.findViewById(R.id.TextViewFechaPublicacion)

        val buttonCompartirNoticias : ImageButton = itemView.findViewById(R.id.buttonCompartirNoticias)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_noticia, parent, false)
        return NoticiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) {
        val noticia = noticias[position]
        holder.title.text = noticia.title
        holder.FechaPublicacion.text = formatFecha(noticia.publishedAt ?: "")
        Glide.with(holder.itemView.context).load(noticia.urlToImage).into(holder.image)

        // Botón de favoritos
        holder.favoriteButton.setImageResource(
            if (favoritosIds.contains(noticia.id)) R.drawable.icon_favorite_activo
            else R.drawable.icon_favorite_desctivo
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

        // Botón de compartir
        holder.buttonCompartirNoticias.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Mira esta noticia: ${noticia.title}")
                putExtra(Intent.EXTRA_TEXT, "Lee la noticia completa aquí: ${noticia.url}")
            }
            holder.itemView.context.startActivity(Intent.createChooser(shareIntent, "Compartir noticia"))
        }

    }

    override fun getItemCount(): Int = noticias.size

    private fun formatFecha(fechaOriginal: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            formatoEntrada.timeZone = TimeZone.getTimeZone("UTC")

            val fecha = formatoEntrada.parse(fechaOriginal)
            val formatoSalida = SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'horas' hh:mm:ss a", Locale("es", "ES"))

            formatoSalida.format(fecha ?: Date())
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
