import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v2/everything")
    fun getFireNews(
        @Query("q") query: String = "incendios bolivia",
        @Query("language") language: String = "es",
        @Query("apiKey") apiKey: String = "8f4d6ca80fba47848a503c0e692016ab"
    ): Call<NewsResponse>
}
