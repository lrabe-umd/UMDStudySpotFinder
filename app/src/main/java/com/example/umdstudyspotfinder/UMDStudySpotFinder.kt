package com.example.umdstudyspotfinder

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Collapsible BottomSheet
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudySpotAdapter
    private lateinit var bottomSheet : LinearLayout

    // Ads, Database, Google Maps
    private lateinit var adView : AdView
    val dbManager : DatabaseManager = DatabaseManager()
    private lateinit var map: GoogleMap
    private var mapMarkers: ArrayList<Marker> = ArrayList()

    // Filters
    // TODO: Update based on SeekBar
    private var seekBarMaxDist: Float = 10000f
    private lateinit var selectedTagList: MutableList<String>

    // GPS
    private var useGPS : Boolean = false
    private lateinit var locationClient: FusedLocationProviderClient
    private var curLatLng: LatLng? = null
    private var locationIndicator: Circle? = null

    private val gpsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->

            if(permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                // User granted fine location access
                startGPSRequests()
            } else if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                // User granted only coarse location access
            } else {
                // User did not grant any location access
            }
        }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            val location = p0.lastLocation
            if(location != null) {
                if(map != null) {
                    curLatLng = LatLng(location.latitude, location.longitude)
                    drawGPSLocationOnMap()
                    updateMapMarkers()
                    updateRecycler()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // RUN THIS ONCE to populate database, then comment it out
        // populateStudySpots()

        // testDatabase()

        // Test filter values
        selectedTagList = mutableListOf("outside")

        // Get study spots
        dbManager.getFilteredStudySpots(curLatLng, seekBarMaxDist, DatabaseManager.SavedPrefs.getAll(this).toList(), { spots ->
            setupRecycler(spots.toMutableList())
        })

        // Get fragment for google map
        var mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.main_map) as SupportMapFragment

        Log.w("MainActivity", "Loading Google map...")
        mapFragment.getMapAsync(this)

        Log.w("MainActivity", "Loading Ad from google services...")

        //advertising
        adView = findViewById(R.id.adView)
        //adView.adSize = getFullWidthAdSize()

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        Log.w("MainActivity", "Ad loaded!")

        // Get bottom sheet
        bottomSheet = findViewById<LinearLayout>(R.id.collapsible_menu)

        // Setup settings button
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Setup recenter button
        val recenterButton = findViewById<ImageButton>(R.id.recenterButton)
        recenterButton.setOnClickListener {
            moveMapToGPSLocation()
        }

        // Setup seekbar
        var seekbar = findViewById<SeekBar>(R.id.seekbar)
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                Log.w("MainActivity", progress.toString())

                // Square progress to get real value
                seekBarMaxDist = progress.toFloat() * progress.toFloat()
                val distInKm : Float = seekBarMaxDist / 1000.0f
                val kmString = String.format("%.1f", distInKm)
                findViewById<TextView>(R.id.seekbarDistDisplay).text = "${kmString}km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateMapMarkers()
            }

        })

        // Initialize GPS
        if(useGPS) {
            requestGPSPermissions()
        }
    }

    override fun onMapReady(googleMap: GoogleMap): Unit {
        map = googleMap
        Log.w("MainActivity", "Google map loaded!")

        var update: CameraUpdate = CameraUpdateFactory.newLatLngZoom(UMD_LAT_LNG, 15.0f)
        map.moveCamera(update)

        // Add markers
        dbManager.getFilteredStudySpots(curLatLng, seekBarMaxDist, DatabaseManager.SavedPrefs.getAll(this).toList(),{ spots ->
            for(spot in spots) {
                val pos = LatLng(spot.latitude, spot.longitude)
                var marker = map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(spot.name)
                        .snippet(spot.description)
                )
                mapMarkers.add(marker!!)
            }
        })

        // Subscribe to marker clicks
        map.setOnMarkerClickListener(this)
    }

    fun updateMapMarkers() {
        for(marker in mapMarkers) {
            marker.remove()
        }

        // Add markers
        dbManager.getFilteredStudySpots(curLatLng, seekBarMaxDist, DatabaseManager.SavedPrefs.getAll(this).toList(),{ spots ->
            for(spot in spots) {
                val pos = LatLng(spot.latitude, spot.longitude)
                var marker = map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(spot.name)
                        .snippet(spot.description)
                )
                mapMarkers.add(marker!!)
            }
        })
    }

    private fun startGPSRequests() {
        locationClient = LocationServices.getFusedLocationProviderClient(this)

        // GPS request every 10 seconds
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()

        // Make sure we actually have the perms
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        Log.w("MainActivity", "GPS requests started!")
    }

    private fun requestGPSPermissions() {
        gpsPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun moveMapToGPSLocation() {
        if(curLatLng == null) return
        val cameraUpdate = CameraUpdateFactory.newLatLng(curLatLng!!)
        map.animateCamera(cameraUpdate)
    }

    private fun drawGPSLocationOnMap() {
        if(curLatLng == null) return

        if(locationIndicator != null) {
            locationIndicator!!.remove()
        }

        locationIndicator = map.addCircle(
            CircleOptions()
                .center(curLatLng!!)
                .radius(50.0)
                .fillColor(Color.CYAN)
        )
    }

    private fun setupRecycler(spots: MutableList<StudySpot>) {
        // Get recycler
        recyclerView = findViewById(R.id.studySpotRecycler)

        // Create adapter
        adapter = StudySpotAdapter(spots)

        // Set up recycler stuff
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun updateRecycler() {
        // Add markers
        dbManager.getFilteredStudySpots(curLatLng, seekBarMaxDist, DatabaseManager.SavedPrefs.getAll(this).toList(),{ spots ->
            setupRecycler(spots.toMutableList())
        })
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.w("MainActivity", "Marker clicked: " + marker.title)

        for(spot in adapter.spots) {
            val index = adapter.spots.indexOf(spot)

            if(spot.name == marker.title) {
                // Expand BottomSheet
                val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                // Update Highlight
                adapter.highlightedIndex = index
                adapter.notifyDataSetChanged()

                // Scroll to selected spot
                recyclerView.smoothScrollToPosition(index)
            }
        }

        return true
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