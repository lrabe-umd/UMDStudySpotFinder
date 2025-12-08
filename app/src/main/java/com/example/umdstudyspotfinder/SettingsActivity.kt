package com.example.umdstudyspotfinder

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
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

    private val PREFS_NAME = "prefData"
    private val DARK_MODE_KEY = "dark_mode"

    private lateinit var adView : AdView
    private lateinit var groupStudyButton : Button
    private lateinit var outletsButton : Button
    private lateinit var foodButton : Button
    private lateinit var whiteboardsButton : Button
    private lateinit var windowsButton : Button
    private lateinit var quietStudyButton : Button
    private lateinit var outsideButton : Button
    private lateinit var reservableButton : Button

    private lateinit var nightToggle : Switch

    private lateinit var buttons : List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //apply day or night mode
        loadNightMode()

        setContentView(R.layout.activity_settings)

        nightToggle = findViewById<Switch>(R.id.switchTheme)

        nightToggle.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        nightToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveNightMode(true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveNightMode(false)
            }
        }

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

        //setup pref buttons
        groupStudyButton = findViewById<Button>(R.id.groupStudyButton)
        outletsButton = findViewById<Button>(R.id.outletsButton)
        foodButton = findViewById<Button>(R.id.foodButton)
        whiteboardsButton = findViewById<Button>(R.id.whiteboardsButton)
        windowsButton = findViewById<Button>(R.id.windowsButton)
        quietStudyButton = findViewById<Button>(R.id.quietStudyButton)
        outsideButton = findViewById<Button>(R.id.outsideButton)
        reservableButton = findViewById<Button>(R.id.reservableButton)

        buttons = listOf(groupStudyButton, outletsButton, foodButton, whiteboardsButton, windowsButton, quietStudyButton, outsideButton, reservableButton)

        val currPrefs = DatabaseManager.SavedPrefs.getAll(this)
        buttons.forEach { btn ->
            btn.setOnClickListener {
                val key = btn.tag as String
                onButtonPressed(btn, key)
                updateButtonVisuals(this);
            }

            if(currPrefs.contains(btn.tag as String)){
                btn.isSelected = true
            }
        }

        updateButtonVisuals(this);
    }

    fun onButtonPressed(button: Button, key: String) {

        button.isSelected = !button.isSelected

        if(button.isSelected){
            DatabaseManager.SavedPrefs.add(button.context, key)
        }else{
            DatabaseManager.SavedPrefs.remove(button.context, key)
        }

        Log.d("Settings Activity", "Current Prefs are: "+DatabaseManager.SavedPrefs.getAll(button.context).toString())
    }

    fun updateButtonVisuals(context: Context){
        if(DatabaseManager.SavedPrefs.getAll(context).isEmpty()){
            buttons.forEach { btn ->
                val color = Color.BLUE
                btn.backgroundTintList = ColorStateList.valueOf(color)
            }
        }else{
            buttons.forEach { btn ->
                val color = if (btn.isSelected) Color.RED else Color.GRAY
                btn.backgroundTintList = ColorStateList.valueOf(color)
            }
        }


    }
/*
    private fun toggleNightMode() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // Switch to light
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            saveNightMode(false)
        } else {
            // Switch to dark
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            saveNightMode(true)
        }
    }
*/
    private fun saveNightMode(isDarkMode: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(DARK_MODE_KEY, isDarkMode).apply()
    }

    private fun loadNightMode() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(DARK_MODE_KEY, false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Log.d("Settings Activity","Loading in nightmode")
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Log.d("Settings Activity","Loading in daymode")
        }
    }
}

