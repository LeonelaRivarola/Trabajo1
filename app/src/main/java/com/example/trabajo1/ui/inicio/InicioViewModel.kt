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

class InicioViewModel : ViewModel() {

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
        .baseUrl("https://dolarapi.com/v1/dolares/") // baseUrl termina en /
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    interface DolarApiService {
        @GET("oficial")
        fun getDolarOficial(): Call<Dolar>
    }

    private val api = retrofit.create(DolarApiService::class.java)

    fun cargarDolarOficial() {
        api.getDolarOficial().enqueue(object : Callback<Dolar> {
            override fun onResponse(call: Call<Dolar>, response: Response<Dolar>) {
                if (response.isSuccessful) {
                    _dolar.value = response.body()
                }
            }
            override fun onFailure(call: Call<Dolar>, t: Throwable) {
                // Manejar error (puedes loguear o mostrar valor default)
            }
        })
    }
}