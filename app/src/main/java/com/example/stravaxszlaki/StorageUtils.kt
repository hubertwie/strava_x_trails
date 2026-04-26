package com.example.stravaxszlaki

import android.content.Context
import com.example.stravaxszlaki.api.LocalRoute
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun saveRoutesLocally(context: Context, routes: List<LocalRoute>) {
    val prefs = context.getSharedPreferences("strava_prefs", Context.MODE_PRIVATE)
    val json = Gson().toJson(routes)
    prefs.edit().putString("saved_routes_json", json).apply()
}

fun loadRoutesLocally(context: Context): List<LocalRoute> {
    val prefs = context.getSharedPreferences("strava_prefs", Context.MODE_PRIVATE)
    val json = prefs.getString("saved_routes_json", null) ?: return emptyList()
    val type = object : TypeToken<List<LocalRoute>>() {}.type
    return Gson().fromJson(json, type)
}