package com.playdevsgame
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecordResponse(
    val name: String = "",
    val premio: Int = 0,
    val record: Int = 0
)
