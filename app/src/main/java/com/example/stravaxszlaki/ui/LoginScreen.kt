package com.example.stravaxszlaki.ui

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stravaxszlaki.CLIENT_ID
import com.example.stravaxszlaki.REDIRECT_URI

@Composable
fun LoginScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Połącz z kontem Strava", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                    .buildUpon()
                    .appendQueryParameter("client_id", CLIENT_ID)
                    .appendQueryParameter("redirect_uri", REDIRECT_URI)
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("approval_prompt", "auto")
                    .appendQueryParameter("scope", "activity:read_all")
                    .build()

                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(context, intentUri)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC4C02)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(50.dp)
        ) {
            Text("Autoryzuj", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("map_screen") }) {
            Text("Pomiń logowanie", color = Color.Gray)
        }
    }
}