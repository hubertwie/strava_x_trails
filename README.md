# Strava Heatmap Mobile

Strava Heatmap Mobile is an Android application designed to visualize personal sports activities on interactive maps. It fetches data from the Strava API and renders a custom heatmap, focusing on privacy, performance, and offline capabilities.

## Key Features

**Strava Integration:** Securely connect your Strava account using OAuth2 to access your personal activities.

**Detailed Polylines:** Fetches full-resolution polylines instead of summaries to ensure activity lines are visible right from your doorstep, bypassing default privacy zone limitations.

**Offline-First Architecture:** Activities are cached locally in the device storage (Gson/SharedPreferences) for instant loading and to respect Strava API rate limits.

**Interactive Map Engine:** Uses OSMDroid with Mapy.cz outdoor tiles to provide a high-quality topographic view ideal for hiking and cycling.

**Advanced Localization:** Real-time GPS tracking with a smooth, custom-rendered compass arrow using Android's rotation vector sensors for 60fps performance.

**Map Customization:** Toggle activity visibility and switch between Outdoor, Satellite, and Standard map layers via an interactive UI.

## How it Works

The project is structured into modular packages for maintainability (Clean Architecture approach):

**api:** Handles network communication with Strava using Retrofit and manages data models.

**map:** Contains custom map overlays, sensor listeners for the smooth compass, and polyline decoding logic.

**ui:** Built entirely with Jetpack Compose, featuring a responsive BottomSheet navigation and floating map controls.

## Requirements

- Android SDK 26+
- Kotlin
- Jetpack Compose
- OSMDroid
- Retrofit & Gson
- Strava API Developer Credentials
- Mapy.cz API Key

## Setup

To run this project locally, you need to configure your own API keys. 

1. Obtain your Client ID and Client Secret from the [Strava API Settings](https://www.strava.com/settings/api).
2. Obtain a free API Key from [Mapy.cz Developer Portal](https://developer.mapy.cz/).
3. In the root package (`com.example.stravaxszlaki`), create a file named `Secrets.kt` (this file is gitignored for security).
4. Add your credentials to the file as follows:

```kotlin
package com.example.stravaxszlaki

object Secrets {
    const val CLIENT_ID = "YOUR_STRAVA_CLIENT_ID"
    const val CLIENT_SECRET = "YOUR_STRAVA_CLIENT_SECRET"
    const val MAPY_CZ_KEY = "YOUR_MAPY_CZ_API_KEY"
}
```

5. Ensure the Strava Authorization Callback Domain is set to localhost in your Strava API settings.
6. Build and run the project using Android Studio.