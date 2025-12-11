package com.example.umdstudyspotfinder

import android.content.Context
import android.util.Log
import androidx.compose.runtime.key
import com.example.umdstudyspotfinder.DatabaseManager.SavedPrefs.getAll

class FavoriteUtils {

    companion object {

        private const val PREF_NAME = "prefData"
        private const val KEY_LIST = "favsList"

        private fun prefs(context: Context) =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        fun isFavorited ( id : String, context : Context) : Boolean {
            return getFavoritesList(context).contains(id)
        }

        fun getFavoritesList(context : Context) : MutableSet<String>{
            return prefs(context)
                .getStringSet(KEY_LIST, emptySet())
                ?.toMutableSet() ?: mutableSetOf()
        }

        fun addFavorite( value: String, context: Context) {
            val set =getFavoritesList(context)
            set.add(value)
            prefs(context).edit().putStringSet(KEY_LIST, set).apply()

            Log.d("FavoritesUtils", "added "+value+" to favs")
        }

        fun removeFavorite(value: String, context: Context) {
            val set = getFavoritesList(context)
            set.remove(value)
            prefs(context).edit().putStringSet(KEY_LIST, set).apply()

            Log.d("FavoritesUtils", "removed "+value+" from favs")
        }
    }
}