package com.example.stravaxszlaki

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stravaxszlaki.api.StravaApi
import com.example.stravaxszlaki.ui.LoginScreen
import com.example.stravaxszlaki.ui.MapScreen
import kotlinx.coroutines.launch

const val CLIENT_ID = Secrets.CLIENT_ID
const val CLIENT_SECRET = Secrets.CLIENT_SECRET
const val REDIRECT_URI = "stravaxszlaki://localhost"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && data.toString().startsWith(REDIRECT_URI)) {
            val code = data.getQueryParameter("code")
            if (code != null) {
                Log.d("STRAVA_AUTH", "Złapaliśmy kod: $code. Wymieniam na Token...")
                lifecycleScope.launch {
                    try {
                        val retrofit = retrofit2.Retrofit.Builder()
                            .baseUrl("https://www.strava.com/")
                            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                            .build()
                        val api = retrofit.create(StravaApi::class.java)
                        val response = api.exchangeToken(CLIENT_ID, CLIENT_SECRET, code)
                        Log.d("STRAVA_AUTH", "SUKCES! MAMY ACCESS TOKEN: ${response.access_token}")

                        val prefs = getSharedPreferences("strava_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("access_token", response.access_token).apply()

                        val intentMap = Intent(this@MainActivity, MainActivity::class.java)
                        intentMap.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intentMap)
                    } catch (e: Exception) {
                        Log.e("STRAVA_AUTH", "Błąd wymiany tokena: ${e.message}")
                    }
                }
            } else {
                val error = data.getQueryParameter("error")
                Log.e("STRAVA_AUTH", "Błąd od Stravy: $error")
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("strava_prefs", Context.MODE_PRIVATE)
    val hasToken = prefs.getString("access_token", null) != null
    val startScreen = if (hasToken) "map_screen" else "login_screen"

    NavHost(navController = navController, startDestination = startScreen) {
        composable("login_screen") { LoginScreen(navController) }
        composable("map_screen") { MapScreen(navController) }
    }
}