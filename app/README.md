# Strava Heatmap Mobile

Strava Heatmap Mobile is an Android application designed to visualize personal sports activities on interactive maps. It fetches data from the Strava API and renders a custom heatmap, focusing on privacy and performance.

## Key Features

**Strava Integration:** Securely connect your Strava account using OAuth2 to access your personal activities.

**Detailed Polylines:** Fetches full-resolution polylines instead of summaries to ensure activity lines are visible right from your doorstep, bypassing default privacy zone limitations.

**Offline-First Architecture:** Activities are cached locally in the device storage (Gson/SharedPreferences) for instant loading and to respect Strava API rate limits.

**Interactive Map Engine:** Uses OSMDroid with Mapy.cz outdoor tiles to provide a high-quality topographic view ideal for hiking and cycling.

**Advanced Localization:** Real-time GPS tracking with a smooth, custom-rendered compass arrow using Android's rotation vector sensors for 60fps performance.

**Map Customization:** Toggle activity visibility and switch between Outdoor, Satellite, and Standard map layers via an interactive UI.

## How it Works

The project is structured into modular packages for maintainability:

**api:** Handles network communication with Strava using Retrofit and manages data models.

**map:** Contains custom map overlays, sensor listeners for the smooth compass, and polyline decoding logic.

**ui:** Built entirely with Jetpack Compose, featuring a responsive BottomSheet navigation and floating map controls.

**storage:** Manages local persistence to minimize API calls and enable offline viewing.

## Requirements

- Android SDK 26+
- Kotlin
- Jetpack Compose
- OSMDroid
- Retrofit & Gson
- Strava API Developer Credentials

## Setup

1. Obtain your Client ID and Client Secret from the Strava Settings.
2. Add your credentials to a local configuration file (excluded from version control).
3. Ensure the redirect URI is set to `stravaxszlaki://localhost`.
4. Build and run the project using Android Studio.