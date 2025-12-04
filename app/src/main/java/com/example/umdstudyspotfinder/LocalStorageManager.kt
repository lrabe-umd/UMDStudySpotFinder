package com.example.umdstudyspotfinder
import android.content.Context
import android.content.SharedPreferences

class LocalStorageManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("StudySpotPrefs", Context.MODE_PRIVATE)

    // ------ local FAVORITES ------
    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    fun addFavorite(spotId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(spotId)
        prefs.edit().putStringSet("favorites", favorites).apply()
    }

    fun removeFavorite(spotId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(spotId)
        prefs.edit().putStringSet("favorites", favorites).apply()
    }

    fun isFavorite(spotId: String): Boolean {
        return getFavorites().contains(spotId)
    }

    fun toggleFavorite(spotId: String): Boolean {
        return if (isFavorite(spotId)) {
            removeFavorite(spotId)
            false // returns false = unfavorited
        } else {
            addFavorite(spotId)
            true // returns true = favorited
        }
    }
    // ------ local SETTINGS ------

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    // ===== STUDY PREFERENCES =====
    fun isQuietStudyEnabled(): Boolean {
        return prefs.getBoolean("quiet_study", true)
    }

    fun setQuietStudy(enabled: Boolean) {
        prefs.edit().putBoolean("quiet_study", enabled).apply()
    }

    fun isGroupStudyEnabled(): Boolean {
        return prefs.getBoolean("group_study", true)
    }

    fun setGroupStudy(enabled: Boolean) {
        prefs.edit().putBoolean("group_study", enabled).apply()
    }
}