package com.example.trabajo1.ui.inicio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

class InicioViewModel : ViewModel() {

    //Dolar
    data class Dolar(
        val moneda: String,
        val casa: String,
        val nombre: String,
        val compra: Double,
        val venta: Double,
        val fechaActualizacion: String
    )
    private val _dolar = MutableLiveData<Dolar>()
    val dolar: LiveData<Dolar> get() = _dolar

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.dolarapi.com/v1/dolares/")//Esto aconsejo el profe ("https://www.dolarsi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    interface DolarApiService{
        @GET("oficial")
        fun getDolarOficial(): Call<Dolar>
    }

    private val api = retrofit.create(DolarApiService::class.java)

    fun cargarDolarOficial(){
        api.getDolarOficial().enqueue(object : Callback<Dolar> {
            override fun onResponse(call: Call<Dolar>, response: Response<Dolar>) {
                if (response.isSuccessful) {
                    _dolar.value = response.body()
                }
            }
            override fun onFailure(call: Call<Dolar>, t: Throwable) {
                //Agregar algo en error, para tener un buen manejo de errores
            }
        })
    }

    //Noticias
    data class Noticia(
        val id: String, //id string? mejor int
        val title: String,
        val description: String,
        val content: String,
        val url: String,
        val image: String,
        val publishedAt: String,
        val source: Source
    )
    data class Source(
        val id: String,
        val name: String,
        val url: String?
    )

    data class GNewsResponse(
        val totalArticles: Int,
        val articles: List<Noticia>
    )

    private val retrofitGNews = Retrofit.Builder()
        .baseUrl("https://gnews.io/api/v4/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    interface GnewsApiService{
        @GET("top-headlines")
        fun getTopHeadlines(
            @Query("topic") topic: String = "technology",
            @Query("country") country: String = "ar",
            @Query("lang") lang: String = "es",
            @Query("token") apiKey: String = "231029e077f84902fc438c5a2613b89c"
        ): Call<GNewsResponse>
    }

    private val gNewsApi = retrofitGNews.create(GnewsApiService::class.java)

    private val _noticias = MutableLiveData<List<Noticia>>()
    val noticias: LiveData<List<Noticia>> get() = _noticias

    fun cargarNoticias(force: Boolean = false) {
        //Solo cargar su no hay noticias, o si el usurio forzo con swipe-to-refresh
        if(!force && !_noticias.value.isNullOrEmpty()) return

        gNewsApi.getTopHeadlines().enqueue(object : Callback<GNewsResponse> {
            override fun onResponse(call: Call<GNewsResponse>, response: Response<GNewsResponse>) {
                if(response.isSuccessful){
                    _noticias.value = response.body()?.articles ?: emptyList()
                }
            }

            override fun onFailure(call: Call<GNewsResponse>, t: Throwable) {
                _noticias.value = emptyList() //o manejar errores
            }
        })
    }
}
