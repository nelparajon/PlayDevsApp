package com.playdevsgame

import retrofit2.http.GET

interface FirebaseApi {

    @GET("records.json")
    suspend fun getRecords(){

    }
}