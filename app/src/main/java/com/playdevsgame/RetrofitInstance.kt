package com.playdevsgame

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }
    fun makeRetrofitService(): FirebaseApi {
        return Retrofit.Builder()
            .baseUrl("https://playdevsgame-default-rtdb.europe-west1.firebasedatabase.app/") //url de referencia de nuestra FirebaseDatabase
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build().create(FirebaseApi::class.java)
    }
}