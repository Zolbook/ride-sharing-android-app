package com.example.rideshareapp
import SocketManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var hasNewAssignment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize and connect to socket
        SocketManager.connect()
    }

    override fun onResume() {
        super.onResume()

        // Check if the socket is connected
        if (SocketManager.getSocket()?.connected() == true) {
            SocketManager.setNewAssignmentListener { assignment ->
                // Handle new assignment
                runOnUiThread {
                    hasNewAssignment = true
                    updateTasksIcon(hasNewAssignment)
                }
            }
        } else {
            // Optionally, handle reconnection or notify the user
        }
    }

    override fun onPause() {
        super.onPause()
        // Clean up listener to avoid memory leaks
        SocketManager.getSocket()?.off("newAssignment")
    }

    private fun updateTasksIcon(hasNewAssignment: Boolean) {
        invalidateOptionsMenu() // Forces onCreateOptionsMenu to be called again
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        // Update the notification icon based on new assignment status
        menu?.findItem(R.id.action_new_assignment)?.setIcon(
            if (hasNewAssignment) R.drawable.ic_notification_active
            else R.drawable.ic_notification
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_assignment -> {
                if (hasNewAssignment) {
                    showNewAssignmentMessage()
                } else {
                    Toast.makeText(this, "No new assignments", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNewAssignmentMessage() {
        Toast.makeText(this, "You have a new assignment!", Toast.LENGTH_LONG).show()
    }

    // ... [rest of your code] ...
}


