package com.example.rideshareapp
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import io.socket.client.IO
import io.socket.client.Socket
import com.google.android.gms.location.FusedLocationProviderClient

class LocationService : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationReference: DatabaseReference
    private var userId: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        initializeServiceComponents()
    }

    private fun initializeServiceComponents() {
        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            stopSelf() // Stop the service if there's no logged-in user
            return
        }

        locationReference = FirebaseDatabase.getInstance().getReference("Users/$userId/location")

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("LocationUpdate", "Location received: Lat ${location.latitude}, Lon ${location.longitude}")
                    updateLocationToFirebase(location.latitude, location.longitude)
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            Log.e("LocationService", "Location permission not granted")
        }
    }

    private fun updateLocationToFirebase(latitude: Double, longitude: Double) {
        // Here you can update the location to Firebase as per your existing logic
        // This function can be modified or extended based on your needs
    }

    override fun onDestroy() {
        super.onDestroy()
        // Perform any necessary cleanup
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Handle task removal if needed
    }
}







