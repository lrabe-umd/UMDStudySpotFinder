package com.example.umdstudyspotfinder

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MoreInfoActivity : AppCompatActivity() {

    private lateinit var tvSpotName: TextView
    private lateinit var tvSpotDescription: TextView
    private lateinit var tvSpotRating: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var backButton: ImageButton

    private lateinit var dbRef: DatabaseReference
    private lateinit var spotId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_info)

        tvSpotName = findViewById(R.id.info_name)
        tvSpotDescription = findViewById(R.id.info_description)
        tvSpotRating = findViewById(R.id.info_rating_value)
        ratingBar = findViewById(R.id.info_rating_bar)
        backButton = findViewById(R.id.info_back_button)

        // Get the study spot ID passed from previous activity
        spotId = intent.getStringExtra("spot_id") ?: ""
        if (spotId.isEmpty()) {
            Log.e("MoreInfoActivity", "No spotId passed in Intent!")
            finish()
            return
        }

        // Firebase reference for this specific study spot
        dbRef = FirebaseDatabase.getInstance()
            .getReference("study_spots")
            .child(spotId)

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Load study spot details and rating in real time
        loadSpotDetailsRealtime()

        // Auto-submit rating when user touches the
        ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
            if (fromUser) {
                submitRatingRealtime(newRating)
            }
        }
    }

    // Load study spot name, description, and rating
    private fun loadSpotDetailsRealtime() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spot = snapshot.getValue(StudySpot::class.java)
                if (spot != null) {
                    tvSpotName.text = spot.name.ifEmpty { "Unknown Location" }
                    tvSpotDescription.text = spot.description.ifEmpty { "No description available" }
                    tvSpotRating.text = "Rating: %.1f".format(spot.averageRating)
                    ratingBar.rating = spot.averageRating.toFloat()
                } else {
                    tvSpotName.text = "Unknown Location"
                    tvSpotDescription.text = "No description available"
                    tvSpotRating.text = "Rating: 0.0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MoreInfoActivity", "Failed to load study spot: ${error.message}")
            }
        })
    }

    // Submit rating for this study spot only
    private fun submitRatingRealtime(userRating: Float) {
        dbRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val spot = mutableData.getValue(StudySpot::class.java) ?: return Transaction.success(mutableData)

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
                    Log.d("MoreInfoActivity", "Rating updated successfully for spot $spotId")
                }
            }
        })
    }
}
