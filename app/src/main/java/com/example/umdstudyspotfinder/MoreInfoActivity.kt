//package com.example.umdstudyspotfinder
//
//import android.os.Bundle
//import android.util.Log
//import android.widget.ImageButton
//import android.widget.RatingBar
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.AdView
//import com.google.firebase.database.*
//
//class MoreInfoActivity : AppCompatActivity() {
//
//    private lateinit var tvSpotName: TextView
//    private lateinit var tvSpotDescription: TextView
//    private lateinit var tvSpotRating: TextView
//    private lateinit var averageRatingBar: RatingBar      // NEW
//    private lateinit var userRatingBar: RatingBar          // NEW
//    private lateinit var backButton: ImageButton
//    private lateinit var favButton: ImageButton
//
//    private lateinit var dbRef: DatabaseReference
//    private lateinit var spotId: String
//    private lateinit var adView: AdView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_more_info)
//
//        tvSpotName = findViewById(R.id.info_name)
//        tvSpotDescription = findViewById(R.id.info_description)
//        tvSpotRating = findViewById(R.id.info_rating_value)
//
//        averageRatingBar = findViewById(R.id.info_average_rating_bar)   // NEW
//        userRatingBar = findViewById(R.id.info_user_rating_bar)         // NEW
//
//        backButton = findViewById(R.id.info_back_button)
//        favButton = findViewById(R.id.info_fav_button)
//
//        spotId = intent.getStringExtra("spot_id") ?: ""
//        if (spotId.isEmpty()) {
//            Log.e("MoreInfoActivity", "No spotId passed in Intent!")
//            finish()
//            return
//        }
//
//        dbRef = FirebaseDatabase.getInstance()
//            .getReference("study_spots")
//            .child(spotId)
//
//        // Load ad
//        adView = findViewById(R.id.adView)
//        val adRequest = AdRequest.Builder().build()
//        adView.loadAd(adRequest)
//
//        // Back button
//        backButton.setOnClickListener { finish() }
//
//        // Favorite button
//        favButton.setOnClickListener {
//            if (FavoriteUtils.isFavorited(spotId, this)) {
//                FavoriteUtils.removeFavorite(spotId, this)
//            } else {
//                FavoriteUtils.addFavorite(spotId, this)
//            }
//            updateFavoriteButton()
//        }
//
//        updateFavoriteButton()
//
//        loadSpotDetailsRealtime()
//
//        // -----------------------
//        // USER RATING INPUT
//        // -----------------------
//
//        val prefs = getSharedPreferences("ratings", MODE_PRIVATE)
//        val hasRated = prefs.getBoolean("rated_$spotId", false)
//
//        if (hasRated) {
//            userRatingBar.isEnabled = false
//        }
//
//        userRatingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
//            if (fromUser && !hasRated) {
//                submitRatingRealtime(rating)
//
//                // lock rating input
//                userRatingBar.isEnabled = false
//                prefs.edit().putBoolean("rated_$spotId", true).apply()
//            }
//        }
//    }
//
//    private fun loadSpotDetailsRealtime() {
//        dbRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val spot = snapshot.getValue(StudySpot::class.java)
//                if (spot != null) {
//                    tvSpotName.text = spot.name.ifEmpty { "Unknown Location" }
//                    tvSpotDescription.text = spot.description.ifEmpty { "No description available" }
//                    tvSpotRating.text = "Rating: %.1f".format(spot.averageRating)
//
//                    averageRatingBar.rating = spot.averageRating.toFloat()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("MoreInfoActivity", "Failed to load study spot: ${error.message}")
//            }
//        })
//    }
//
//    // user submits personal rating
//    private fun submitRatingRealtime(userRating: Float) {
//        dbRef.runTransaction(object : Transaction.Handler {
//            override fun doTransaction(mutableData: MutableData): Transaction.Result {
//                val spot = mutableData.getValue(StudySpot::class.java)
//                    ?: return Transaction.success(mutableData)
//
//                val newTotal = spot.totalRating + userRating
//                val newCount = spot.ratingCount + 1
//                val newAverage = newTotal / newCount
//
//                mutableData.child("totalRating").value = newTotal
//                mutableData.child("ratingCount").value = newCount
//                mutableData.child("averageRating").value = newAverage
//
//                return Transaction.success(mutableData)
//            }
//
//            override fun onComplete(
//                error: DatabaseError?,
//                committed: Boolean,
//                currentData: DataSnapshot?
//            ) {
//                if (error != null) {
//                    Log.w("MoreInfoActivity", "Rating update failed: ${error.message}")
//                } else {
//                    Log.w("MoreInfoActivity", "User rating applied for $spotId")
//                }
//            }
//        })
//    }
//
//    private fun updateFavoriteButton() {
//        if (!FavoriteUtils.isFavorited(spotId, this)) {
//            favButton.setImageResource(R.drawable.heart_empty)
//        } else {
//            favButton.setImageResource(R.drawable.heart_filled)
//        }
//    }
//}


package com.example.umdstudyspotfinder

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.database.*

class MoreInfoActivity : AppCompatActivity() {

    private lateinit var tvSpotName: TextView
    private lateinit var tvSpotDescription: TextView
    private lateinit var tvAverageLabel: TextView
    private lateinit var tvUserLabel: TextView

    private lateinit var avgRatingBar: RatingBar
    private lateinit var userRatingBar: RatingBar

    private lateinit var favButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var adView: AdView

    private lateinit var dbRef: DatabaseReference
    private lateinit var spotId: String

    private val PREFS_NAME = "user_ratings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_info)

        tvSpotName = findViewById(R.id.info_name)
        tvSpotDescription = findViewById(R.id.info_description)
        tvAverageLabel = findViewById(R.id.info_average_rating_label)
        tvUserLabel = findViewById(R.id.info_user_rating_label)

        avgRatingBar = findViewById(R.id.info_average_rating_bar)
        userRatingBar = findViewById(R.id.info_user_rating_bar)

        favButton = findViewById(R.id.info_fav_button)
        backButton = findViewById(R.id.info_back_button)

        // Average rating must not be editable
        avgRatingBar.setIsIndicator(true)

        // Load spotId
        spotId = intent.getStringExtra("spot_id") ?: ""
        if (spotId.isEmpty()) {
            Log.w("MoreInfoActivity", "No spotId passed in Intent!")
            finish()
            return
        }

        // Firebase reference
        dbRef = FirebaseDatabase.getInstance()
            .getReference("study_spots")
            .child(spotId)

        // Load ads
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Back button
        backButton.setOnClickListener { finish() }

        // Favorite button
        favButton.setOnClickListener {
            if (FavoriteUtils.isFavorited(spotId, this)) {
                FavoriteUtils.removeFavorite(spotId, this)
            } else {
                FavoriteUtils.addFavorite(spotId, this)
            }
            updateFavoriteButton()
        }
        updateFavoriteButton()

        // Load spot data
        loadSpotDetailsRealtime()

        // Load user rating if it exists
        loadUserRating()

        // User rating submission (ONLY ONCE)
        userRatingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                handleUserRating(rating)
            }
        }
    }

    // Load spot name, description, and average rating
    private fun loadSpotDetailsRealtime() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spot = snapshot.getValue(StudySpot::class.java)
                if (spot != null) {
                    // Name and description
                    tvSpotName.text = spot.name
                    tvSpotDescription.text = spot.description

                    // Average rating bar: reflect actual Firebase value
                    avgRatingBar.rating = spot.averageRating.toFloat()
                    avgRatingBar.setIsIndicator(true) // ensure it's read-only

                    // Update Average Rating label dynamically
                    tvAverageLabel.text = "Average Rating: %.1f".format(spot.averageRating)
                } else {
                    tvSpotName.text = "Unknown Location"
                    tvSpotDescription.text = "No description available"
                    avgRatingBar.rating = 0f
                    tvAverageLabel.text = "Average Rating: 0.0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MoreInfoActivity", "Failed to load study spot: ${error.message}")
            }
        })
    }

    // Load user rating from SharedPreferences
    private fun loadUserRating() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getFloat("rating_$spotId", -1f)

        if (saved >= 0f) {
            // Set the stars before locking the bar
            userRatingBar.rating = saved
            userRatingBar.setIsIndicator(true) // lock bar
        }
    }

    // Handle user rating submission (only once)
    private fun handleUserRating(userRating: Float) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getFloat("rating_$spotId", -1f)

        if (saved >= 0f) {
            // User already rated --> prevent second rating
            userRatingBar.rating = saved
            userRatingBar.setIsIndicator(true)
            return
        }

        // Save rating locally
        prefs.edit().putFloat("rating_$spotId", userRating).apply()

        // Lock input
        userRatingBar.setIsIndicator(true)

        // Update the average in Firebase
        submitRatingToFirebase(userRating)
    }

    // Update Firebase rating (average calculation)
    private fun submitRatingToFirebase(userRating: Float) {
        dbRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val spot = mutableData.getValue(StudySpot::class.java)
                    ?: return Transaction.success(mutableData)

                val newTotal = spot.totalRating + userRating
                val newCount = spot.ratingCount + 1
                val newAverage = newTotal / newCount

                mutableData.child("totalRating").value = newTotal
                mutableData.child("ratingCount").value = newCount
                mutableData.child("averageRating").value = newAverage

                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("MoreInfoActivity", "Rating update failed: ${error.message}")
                } else {
                    Log.d("MoreInfoActivity", "Rating updated for spot $spotId")
                }
            }
        })
    }

    // Favorite button UI
    private fun updateFavoriteButton() {
        if (FavoriteUtils.isFavorited(spotId, this)) {
            favButton.setImageResource(R.drawable.heart_filled)
        } else {
            favButton.setImageResource(R.drawable.heart_empty)
        }
    }
}
