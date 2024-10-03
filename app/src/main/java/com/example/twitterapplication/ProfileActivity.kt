package com.example.twitterapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private var userNameTextView: TextView? = null
    private var userEmailTextView: TextView? = null
    private lateinit var backButton: Button
    private lateinit var database: FirebaseDatabase
    private var currentUserFirstName: String = ""
    private var currentUserLastName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        backButton = findViewById(R.id.backButton)

        database = FirebaseDatabase.getInstance() // Initialize Firebase Database
        displayUserProfile() // Fetch user profile info

        // Set OnClickListener for backButton
        backButton.setOnClickListener {
            finish() // This will close the ProfileActivity and return to the previous one
        }
    }

    private fun displayUserProfile() {
        val user = FirebaseAuth.getInstance().currentUser // Get the current user
        if (user != null) {
            val userId = user.uid
            fetchUserDetails(userId) // Fetch details using user ID
            // Fetch user email from FirebaseAuth
            val userEmail = user.email
            userEmailTextView?.text = userEmail ?: "No Email Provided"
        } else {
            userNameTextView?.text = "No User Logged In"
            userEmailTextView?.text = "No Email Provided"
        }
    }

    private fun fetchUserDetails(userId: String) {
        val userRef = database.reference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentUserFirstName = snapshot.child("name").getValue(String::class.java) ?: "No Name Provided"
                    currentUserLastName = snapshot.child("surname").getValue(String::class.java) ?: "No Surname Provided"

                    // Display full name in userNameTextView
                    userNameTextView?.text = "$currentUserFirstName $currentUserLastName"
                } else {
                    userNameTextView?.text = "No User Details Found"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                userNameTextView?.text = "Failed to load user details"
            }
        })
    }
}
