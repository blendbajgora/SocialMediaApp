package com.example.twitterapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), TweetDialogFragment.TweetDialogListener {

    private lateinit var database: FirebaseDatabase
    private lateinit var tweetList: MutableList<Tweet>
    private lateinit var tweetAdapter: TweetAdapter
    private lateinit var recyclerView: RecyclerView
    private var currentUserFirstName: String = ""
    private var currentUserLastName: String = ""
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Database at the start of onCreate
        database = FirebaseDatabase.getInstance()
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Initialize profile button
        val profileButton = findViewById<Button>(R.id.profileButton) // Add this line
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java) // Navigate to ProfileActivity
            startActivity(intent)
        }

        // Initialize tweet button
        val postTweetButton = findViewById<Button>(R.id.postTweetButton) // Adjust if ID is different
        postTweetButton.setOnClickListener {
            val tweetDialog = TweetDialogFragment()
            tweetDialog.setTweetDialogListener(this) // Set the listener to handle tweet posting
            tweetDialog.show(supportFragmentManager, "TweetDialogFragment")
        }

        // Fetch current user details
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            fetchUserDetails(currentUser.uid) // Fetch user details using their UID
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.tweetsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        tweetList = mutableListOf()
        tweetAdapter = TweetAdapter(tweetList, currentUserId) { tweet ->
            deleteTweet(tweet) // Call the delete function when "Delete" is clicked
        }
        recyclerView.adapter = tweetAdapter

        // Load tweets from Firebase
        loadTweets()

        // Initialize logout button
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Sign out the user
            val intent = Intent(this, AuthActivity::class.java) // Redirect to AuthActivity
            startActivity(intent)
            finish() // Close MainActivity
        }
    }

    private fun loadTweets() {
        val tweetRef = database.reference.child("tweets")
        tweetRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tweetList.clear()
                for (tweetSnapshot in snapshot.children) {
                    val tweet = tweetSnapshot.getValue(Tweet::class.java)
                    if (tweet != null) {
                        tweetList.add(tweet)
                    }
                }
                tweetAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load tweets!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Fetch user details from Firebase
    private fun fetchUserDetails(userId: String) {
        val userRef = database.reference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentUserFirstName = snapshot.child("name").getValue(String::class.java) ?: ""
                    currentUserLastName = snapshot.child("surname").getValue(String::class.java) ?: ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load user details!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Check for authentication in onStart
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity to prevent going back to it
        }
    }

    // Function to post the tweet
    fun postTweet(context: Context, tweet: Tweet) {
        val tweetRef = database.reference.child("tweets").push()
        tweet.tweetId = tweetRef.key ?: ""

        tweetRef.setValue(tweet).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Tweet posted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to post tweet: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTweet(tweet: Tweet) {
        val tweetRef = database.reference.child("tweets").child(tweet.tweetId) // Assuming you store tweetId

        tweetRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Tweet deleted!", Toast.LENGTH_SHORT).show()
                tweetList.remove(tweet) // Remove the tweet from the list
                tweetAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Failed to delete tweet: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle the tweet posted from the dialog
    override fun onTweetPosted(tweetContent: String) {
        if (currentUserFirstName.isNotEmpty() && currentUserLastName.isNotEmpty()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val tweet = Tweet(
                    firstName = currentUserFirstName,
                    lastName = currentUserLastName,
                    content = tweetContent,
                    timestamp = System.currentTimeMillis(),
                    userId = currentUser.uid // Add the userId to the Tweet object for identification
                )
                postTweet(this, tweet)
            }
        } else {
            Toast.makeText(this, "User details not available", Toast.LENGTH_SHORT).show()
        }
    }
}
