package com.example.umdstudyspotfinder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

class GPSManager(
    private val context: Context,
    private val onLocationUpdate: () -> Unit
) {
    private lateinit var locationClient: FusedLocationProviderClient
    var curLatLng: LatLng? = null

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            val location = p0.lastLocation
            if(location != null) {
                curLatLng = LatLng(location.latitude, location.longitude)
                onLocationUpdate.invoke()

                Log.w("GPSManager", "Location updated!")
            }
        }
    }

    init {
        Log.w("GPSManager", "Starting GPS requests!")
        startGPSRequests()
    }

    private fun startGPSRequests() {
        locationClient = LocationServices.getFusedLocationProviderClient(context)

        // GPS request every 10 seconds
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()

        // Make sure we actually have the perms
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        Log.w("GPSManager", "Requesting Location Updates!")
        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        Log.w("GPSManager", "GPS requests started!")
    }
}