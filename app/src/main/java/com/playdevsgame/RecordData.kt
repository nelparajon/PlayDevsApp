package com.playdevsgame


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecordData(
    @Json(name = "name")
    val name: String,
    @Json(name = "coins")
    val coins: Int,
    @Json(name = "record")
    val record: String
)
