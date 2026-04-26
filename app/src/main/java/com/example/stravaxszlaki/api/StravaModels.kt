package com.example.stravaxszlaki.api

import org.osmdroid.util.GeoPoint

data class StravaTokenResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_at: Long
)

data class StravaActivity(
    val id: Long,
    val name: String,
    val distance: Float,
    val map: StravaMapSummary
)

data class StravaMapSummary(
    val summary_polyline: String?
)

data class StravaDetailedActivity(
    val id: Long,
    val map: StravaDetailedMap
)

data class StravaDetailedMap(
    val polyline: String?
)

data class LocalRoute(
    val id: Long,
    val name: String,
    val distance: Float,
    val points: List<GeoPoint>
)