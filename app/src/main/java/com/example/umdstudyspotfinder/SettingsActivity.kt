package com.example.umdstudyspotfinder

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.umdstudyspotfinder.ui.theme.UMDStudySpotFinderTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class SettingsActivity : ComponentActivity() {

    private lateinit var adView : AdView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //setup ad
        adView = findViewById(R.id.adView)
        //adView.adSize = getFullWidthAdSize()

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        //setup back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

