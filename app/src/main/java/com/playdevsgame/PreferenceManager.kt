package com.playdevsgame

import android.content.Context

object PreferenceManager {
    private const val PREFS_NAME = "MyPrefsFile"
    private const val PREF_PLAYER_NAME = "playerName"

    fun savePlayerName(context: Context, playerName: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(PREF_PLAYER_NAME, playerName)
        editor.apply()
    }

    fun getPlayerName(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(PREF_PLAYER_NAME, "player") ?: "player"
    }


}