package com.example.umdstudyspotfinder

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // RUN THIS ONCE to populate database, then comment it out
        // populateStudySpots()

        // testDatabase()

        // Get fragment for google map
        var mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.main_map) as SupportMapFragment

        Log.w("MainActivity", "Loading Google map...")
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap): Unit {
        map = googleMap
        Log.w("MainActivity", "Google map loaded!")

        var update: CameraUpdate = CameraUpdateFactory.newLatLngZoom(UMD_LAT_LNG, 18f)
        map.moveCamera(update)
    }

    private fun populateStudySpots() {
        val dbManager = DatabaseManager()

        val spots = listOf(
            StudySpot(
                id = "ASY",
                name = "Art-Sociology Building",
                description = "Home to the Art Library, offering quiet study tables, natural light, and good spots for small group work.",
                latitude = 38.98528145,
                longitude = -76.94789478,
                tags = listOf("group_study", "reservable", "outlets", "whiteboard", "windows"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "PAC",
                name = "Clarice Smith Performing Arts Center",
                description = "Large arts complex with the Performing Arts Library, calm lobby seating, and a café for study breaks.",
                latitude = 38.9906807,
                longitude = -76.95044341,
                tags = listOf("quiet_study", "group_study", "food_nearby", "windows"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "HBK",
                name = "Hornbake Library",
                description = "Academic library focused on special collections and study spaces, with both quiet areas and group-friendly rooms.",
                latitude = 38.9881767,
                longitude = -76.94157409,
                tags = listOf("quiet_study", "group_study", "outlets", "windows", "whiteboard"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "MTH",
                name = "Mathematics Building",
                description = "Home to the STEM Library, offering quiet carrels, group rooms, whiteboards, and bright spots for problem-solving.",
                latitude = 38.98862265,
                longitude = -76.93906881,
                tags = listOf("quiet_study", "group_study", "whiteboard", "outlets"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "MCK",
                name = "McKeldin Library",
                description = "Main campus library with seven floors, silent spaces on the upper levels and collaborative, tech-equipped areas on the lower ones.",
                latitude = 38.98598155,
                longitude = -76.94510047,
                tags = listOf("quiet_study", "group_study", "reservable", "outlets", "whiteboard", "windows", "food_nearby", "outside"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "SSU",
                name = "Adele H. Stamp Student Union",
                description = "Busy campus hub with lounges, meeting rooms, and lots of dining options, best for collaborative work.",
                latitude = 38.98816455,
                longitude = -76.94472182,
                tags = listOf("group_study", "reservable", "outlets", "food_nearby", "windows", "outside"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "ESJ",
                name = "Edward St. John Learning and Teaching Center",
                description = "Bright modern building with huddle rooms and plenty of open tables designed for group study.",
                latitude = 38.986699,
                longitude = -76.941914,
                tags = listOf("group_study", "reservable", "outlets", "whiteboard", "windows"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "HJP",
                name = "H.J. Patterson Hall",
                description = "Academic building with quieter hallways and cafe areas that work well for studying.",
                latitude = 38.98708535,
                longitude = -76.9432766,
                tags = listOf("quiet_study", "group_study", "whiteboard", "outlets", "outside"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "IRB",
                name = "Brendan Iribe Center",
                description = "Tech-focused CS building with study pods, glass meeting rooms, and lots of natural light.",
                latitude = 38.98912265,
                longitude = -76.93647137,
                tags = listOf("group_study", "reservable", "outlets", "whiteboard", "windows", "quiet_study"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "SPH",
                name = "School of Public Health",
                description = "Department building with classroom seating and sunny hallways suitable for quiet or small-group study.",
                latitude = 38.9934922,
                longitude = -76.94316338,
                tags = listOf("quiet_study", "group_study", "outlets", "windows"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "TWS",
                name = "Tawes Fine Arts Building",
                description = "English and arts building with quiet hallway seating and outdoor tables in the courtyard area.",
                latitude = 38.98598645,
                longitude = -76.94832385,
                tags = listOf("group_study", "quiet_study", "windows", "whiteboard", "outside"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "VMH",
                name = "Van Munching Hall",
                description = "Business school building with many tables, breakout spaces, and areas for team projects.",
                latitude = 38.9830467,
                longitude = -76.94703829,
                tags = listOf("quiet_study", "group_study", "outlets", "whiteboard"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "TMH",
                name = "Thurgood Marshall Hall",
                description = "Classroom and lecture building that stays fairly quiet and works well for individual or small group study.",
                latitude = 38.98516925,
                longitude = -76.93869834,
                tags = listOf("quiet_study", "group_study", "outlets", "whiteboard"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "KEB",
                name = "Jeong H. Kim Engineering Building",
                description = "Large engineering building with open collaborative areas",
                latitude = 38.9909099,
                longitude = -76.93802559,
                tags = listOf("group_study", "reservable", "outlets", "whiteboard", "windows"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            ),
            StudySpot(
                id = "PHY",
                name = "Physics Building",
                description = "Traditional science building with spaces for collaboration and quiet study in the lobby.",
                latitude = 38.9886972,
                longitude = -76.9400772,
                tags = listOf("quiet_study", "group_study", "whiteboard", "outlets"),
                favoriteCount = 0,
                totalRating = 0.0,
                ratingCount = 0,
                averageRating = 0.0
            )
        )

        Log.d("MainActivity", "Populating database with ${spots.size} study spots...")
        spots.forEach { spot ->
            dbManager.addStudySpot(spot)
        }
        Log.d("MainActivity", "Done populating database!")
    }

    companion object {
        val UMD_LAT_LNG: LatLng = LatLng(38.98465556431913, -76.94301522201258)
    }
}