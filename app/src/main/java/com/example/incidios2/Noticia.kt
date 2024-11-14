data class NewsResponse(
    val articles: List<Noticia>
)

data class Noticia(
    val id: String, // o Int, según tus necesidades
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?, // URL de la imagen
    val publishedAt:String?,
    val content: String?
)