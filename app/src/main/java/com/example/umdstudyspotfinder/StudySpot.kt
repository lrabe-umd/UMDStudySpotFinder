package com.example.umdstudyspotfinder

data class StudySpot (
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var tags: List<String> = emptyList(),
    var favoriteCount: Int = 0,

    // rating stuff
    var totalRating: Double = 0.0,
    var ratingCount: Int = 0,
    var averageRating: Double = 0.0
)

