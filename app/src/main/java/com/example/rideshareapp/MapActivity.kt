package com.example.rideshareapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Start LocationService if needed
        startLocationService()

        // Get the driver ID passed from LoginActivity and register the driver
        val driverId = intent.getStringExtra("driverId")
        driverId?.let {
            registerDriver(it)
        }
        val buttonSendLocation = findViewById<Button>(R.id.buttonSendLocation)

        buttonSendLocation.setOnClickListener {
            val intent = Intent(this, DriverStatusActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.disconnect() // Disconnect the socket when the activity is destroyed
    }

    private fun registerDriver(driverId: String) {
        SocketManager.registerDriver(driverId)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MapActivity", "Permissions not granted. Requesting...")
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if(location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                Log.d("MapActivity", "Received location is null.")
            }
        }.addOnFailureListener { exception ->
            Log.e("MapActivity", "Failed to get location: ${exception.localizedMessage}", exception)
        }
    }

    private fun startLocationService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val serviceIntent = Intent(this, LocationService::class.java)
            startService(serviceIntent)
            Log.d("MapActivity", "Location service started")
        } else {
            Log.d("MapActivity", "Location permission not granted. Cannot start location service.")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        mMap.isMyLocationEnabled = true
                        getCurrentLocation()
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
