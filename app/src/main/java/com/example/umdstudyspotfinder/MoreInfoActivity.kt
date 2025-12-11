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
                Log.w("MoreInfoActivity", "Failed to load study spot: ${error.message}")
            }
        })
    }

    // Load user rating from SharedPreferences
    private fun loadUserRating() {
        val prefs = getSharedPreferences(this.packageName + "_preferences", Context.MODE_PRIVATE)
        val saved = prefs.getFloat("rating_$spotId", -1f)

        if (saved >= 0f) {
            // Set the stars before locking the bar
            userRatingBar.rating = saved
            userRatingBar.setIsIndicator(true) // lock bar
        }
    }

    // Handle user rating submission (only once)
    private fun handleUserRating(userRating: Float) {
        val prefs = getSharedPreferences(this.packageName + "_preferences", Context.MODE_PRIVATE)
        val saved = prefs.getFloat("rating_$spotId", -1f)

        if (saved >= 0f) {
            // User already rated so this will prevent them from submitting another rating
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
        dbRef.get().addOnSuccessListener { snapshot ->
            val spot = snapshot.getValue(StudySpot::class.java)
            if (spot == null) {
                Log.w("MoreInfoActivity", "Spot not found when submitting rating!")
                return@addOnSuccessListener
            }

            val newTotal = spot.totalRating + userRating
            val newCount = spot.ratingCount + 1
            val newAverage = newTotal / newCount

            // Write each field using setValue()
            dbRef.child("totalRating").setValue(newTotal)
            dbRef.child("ratingCount").setValue(newCount)
            dbRef.child("averageRating").setValue(newAverage)

            Log.w("MoreInfoActivity", "Rating updated using setValue()")
        }.addOnFailureListener {
            Log.w("MoreInfoActivity", "Failed to get snapshot: ${it.message}")
        }
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