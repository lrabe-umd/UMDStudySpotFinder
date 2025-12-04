package com.example.umdstudyspotfinder

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase

class DatabaseManager {
    private val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val studySpotsRef: DatabaseReference = firebase.getReference("study_spots")

    // adds a study spot
    fun addStudySpot(spot: StudySpot) {
        studySpotsRef.child(spot.id).setValue(spot)
        Log.d("DatabaseManager", "added spot ${spot.name}")
    }

    // gets all study spots (one time)
    fun getAllStudySpots(callback: (List<StudySpot>) -> Unit) {
        studySpotsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spots = mutableListOf<StudySpot>()
                for (childSnapshot in snapshot.children) {
                    val spot = childSnapshot.getValue(StudySpot::class.java)
                    if (spot != null) {
                        spots.add(spot)
                    }
                }
                callback(spots)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error: ${error.message}")
            }
        })
    }

    // listen to real-time changes in spots (very similar to above)
    fun listenToStudySpots(callback: (List<StudySpot>) -> Unit) {
        studySpotsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spots = mutableListOf<StudySpot>()
                for (childSnapshot in snapshot.children) {
                    val spot = childSnapshot.getValue(StudySpot::class.java)
                    if (spot != null) {
                        spots.add(spot)
                    }
                }
                callback(spots)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error: ${error.message}")
            }
        })
    }

    // get single study spot by ID
    fun getStudySpot(spotId: String, callback: (StudySpot?) -> Unit) {
        studySpotsRef.child(spotId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spot = snapshot.getValue(StudySpot::class.java)
                callback(spot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error: ${error.message}")
                callback(null)
            }
        })
    }

    //  ------ FAVORITE STUFF:  ------

    // increase favorite count
    fun incrementFavorites(spotId: String) {
        val spotRef = studySpotsRef.child(spotId).child("favoriteCount")
        spotRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // get count
                val currentCount = currentData.getValue(Int::class.java) ?: 0
                // increase count by 1
                currentData.value = currentCount + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean,
                                    currentData: DataSnapshot?) {
                if (error != null) {
                    Log.e("DatabaseManager", "error incrementing favorites: ${error.message}")
                } else {
                    Log.d("DatabaseManager", "favorite count incremented")
                }
            }
        })
    }
    // decrease favorite count (very similar to increase)
    fun decrementFavorites(spotId: String) {
        val spotRef = studySpotsRef.child(spotId).child("favoriteCount")
        spotRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // get count
                val currentCount = currentData.getValue(Int::class.java) ?: 0
                // decrease by 1
                if (currentCount > 0) {
                    currentData.value = currentCount - 1
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean,
                                    currentData: DataSnapshot?) {
                if (error != null) {
                    Log.e("DatabaseManager", "Error decrementing favorites: ${error.message}")
                } else {
                    Log.d("DatabaseManager", "Favorite count decremented")
                }
            }
        })
    }

    //  ------ RATING STUFF:  ------

    // add rating (1 star - 5 stars)
    fun addRating(spotId: String, rating: Float, callback: ((Boolean) -> Unit)? = null) {
        val spotRef = studySpotsRef.child(spotId)
        // transactions are added in order to control when multiple
        // users enter data (such as a rating) at the same time
        // both are added and no rating is lost
        spotRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val spot = currentData.getValue(StudySpot::class.java)

                if (spot != null) {
                    // update rating data
                    spot.totalRating += rating.toDouble()
                    spot.ratingCount += 1
                    spot.averageRating = spot.totalRating / spot.ratingCount

                    currentData.value = spot
                    return Transaction.success(currentData)
                }

                return Transaction.abort()
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("DatabaseManager", "Error adding rating: ${error.message}")
                    callback?.invoke(false)
                } else {
                    Log.d("DatabaseManager", "Rating added successfully")
                    callback?.invoke(true)
                }
            }
        })
    }
    // get average rating (nothing fancy)
    fun getAverageRating(spotId: String, callback: (Double) -> Unit) {
        studySpotsRef.child(spotId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spot = snapshot.getValue(StudySpot::class.java)
                callback(spot?.averageRating ?: 0.0)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error getting rating: ${error.message}")
                callback(0.0)
            }
        })
    }
}