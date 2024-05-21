package com.playdevsgame


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://playdevsgame-default-rtdb.europe-west1.firebasedatabase.app/") // URL base de Firebase
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: FirebaseApi = retrofit.create(FirebaseApi::class.java)
}
