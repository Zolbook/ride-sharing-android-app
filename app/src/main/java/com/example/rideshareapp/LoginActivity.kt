package com.example.rideshareapp
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val googleSignInButton = findViewById<com.google.android.gms.common.SignInButton>(R.id.googleSignInButton)


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (!isEmailValid(email) || !isPasswordValid(password)) {
                // Display error messages
                return@setOnClickListener
            }

            signInWithEmailPassword(email, password)
        }

        registerButton.setOnClickListener {
            // Registration logic here
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

    }

    private fun signInWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                if (firebaseUser?.isEmailVerified == true) {
                    navigateToMapActivity(firebaseUser.uid)
                } else {
                    Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMapActivity(uid: String) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("driverId", uid)
        startActivity(intent)
        finish()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it) } ?: run {
                Toast.makeText(this, "Google sign-in failed: Account is null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                navigateToMapActivity(firebaseUser?.uid.orEmpty())
            } else {
                Toast.makeText(this, "Google authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        // Logic for logout
    }

    private fun isEmailValid(email: String): Boolean {
        // Implement email validation logic
        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        // Implement password validation logic
        return true
    }
}





