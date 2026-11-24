package com.example.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    const val BASE_URL = "http://10.0.2.2:8080/"

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val usuarios: UsuariosApi by lazy { retrofit.create(UsuariosApi::class.java) }
    val cobranza: CobranzaApi by lazy { retrofit.create(CobranzaApi::class.java) }
    val habito: HabitoApi by lazy { retrofit.create(HabitoApi::class.java) }
    val reportes: ReportesApi by lazy { retrofit.create(ReportesApi::class.java) }
}
