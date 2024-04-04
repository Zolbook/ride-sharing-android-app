import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import android.util.Log

object SocketManager {
    private var socket: Socket? = null

    init {
        try {
            val opts = IO.Options()
            socket = IO.socket("https://c7b5-103-57-92-77.ngrok-free.app", opts).apply {
                setupEventListeners()
            }
        } catch (e: Exception) {
            Log.e("SocketManager", "Socket initialization error: ${e.message}")
            throw RuntimeException(e)
        }
    }

    private fun setupEventListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) { Log.d("SocketManager", "Connected") }
            on(Socket.EVENT_DISCONNECT) { Log.d("SocketManager", "Disconnected") }
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val message = if (args.isNotEmpty() && args[0] is Exception) {
                    (args[0] as Exception).message.toString()
                } else {
                    args[0].toString()
                }
                Log.e("SocketManager", "Connect Error: $message")
            }
            // Add other event listeners here
        }
    }

    fun connect() {
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun isConnected(): Boolean {
        return socket?.connected() ?: false
    }

    fun registerDriver(driverId: String) {
        if (!isConnected()) {
            connect()
            // Optionally, listen for the connect event before emitting
            socket?.once(Socket.EVENT_CONNECT) {
                emitRegisterDriver(driverId)
            }
        } else {
            emitRegisterDriver(driverId)
        }
    }

    private fun emitRegisterDriver(driverId: String) {
        socket?.emit("registerDriver", driverId)
    }
    fun sendLocationUpdate(driverId: String, latitude: Double, longitude: Double) {
        val locationData = JSONObject().apply {
            put("driverId", driverId)
            put("latitude", latitude)
            put("longitude", longitude)
        }
        socket?.emit("driverLocation", locationData)
    }
    fun updateDriverAvailability(driverId: String, isAvailable: Boolean) {
        val availabilityData = JSONObject().apply {
            put("driverId", driverId)       // Here you used "driverId"
            put("isAvailable", isAvailable) // Here you used "isAvailable"
        }
        socket?.emit("updateDriverAvailability", availabilityData)
    }
    fun setNewAssignmentListener(listener: (String) -> Unit) {
        socket?.on("newAssignment", Emitter.Listener { args ->
            if (args.isNotEmpty() && args[0] is String) {
                val message = args[0] as String
                listener(message)
            }
        })
    }

    fun checkDriverStatus(driverId: String, callback: (Boolean) -> Unit) {
        socket?.emit("checkDriverStatus", driverId)
        socket?.once("driverStatusResponse") { args ->
            if (args.isNotEmpty() && args[0] is Boolean) {
                val isOnlineAndAvailable = args[0] as Boolean
                callback(isOnlineAndAvailable)
            }
        }
    }
    fun getSocket(): Socket? {
        return socket
    }

    // Implement other functionalities as per your requirement
    // ...

}


