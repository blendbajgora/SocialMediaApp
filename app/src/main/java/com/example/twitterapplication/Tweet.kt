package com.example.twitterapplication

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

data class Tweet(
    val firstName: String = "",
    val lastName: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    var tweetId: String = "",
    val userId: String = "",  // Add userId field
    var likesCount: Int = 0,  // Count of likes
    var likedBy: MutableList<String> = mutableListOf()
)

val database = FirebaseDatabase.getInstance().getReference("tweets")

// Function to store a tweet
fun postTweet(context: Context, username: String, content: String) {
    val database = FirebaseDatabase.getInstance().getReference("tweets")
    val tweetId = database.push().key // Generate a unique key for each tweet
    val tweet = Tweet(username, content)

    tweetId?.let {
        database.child(it).setValue(tweet).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Tweet successfully stored in Firebase
                Toast.makeText(context, "Tweet posted!", Toast.LENGTH_SHORT).show()
            } else {
                // Handle failure
                Toast.makeText(context, "Failed to post tweet!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}