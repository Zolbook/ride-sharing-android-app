package com.example.rideshareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class DriverStatusActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var availabilitySwitch: Switch
    private lateinit var navigateToMainButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_status2)

        // Initialize components
        initUIComponents()

        // Get the current Firebase user
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            setupUserDetails(it)
        } ?: run {
            redirectToLogin()
        }
    }

    private fun initUIComponents() {
        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        availabilitySwitch = findViewById(R.id.availabilitySwitch)
        navigateToMainButton = findViewById(R.id.btnNavigateToMain)

        navigateToMainButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun setupUserDetails(user: FirebaseUser) {
        nameTextView.text = user.displayName ?: "Unknown"
        emailTextView.text = user.email ?: "Unknown"

        // Connect to the socket and register the driver
        SocketManager.connect()
        SocketManager.registerDriver(user.uid)

        availabilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            SocketManager.updateDriverAvailability(user.uid, isChecked)
        }
    }

    private fun redirectToLogin() {
        Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Disconnect from the socket when the activity is destroyed
        SocketManager.disconnect()
    }
}


