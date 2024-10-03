package com.example.twitterapplication

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TweetAdapter(
    private val tweetList: MutableList<Tweet>,
    private val currentUserId: String,
    private val onDeleteClicked: (Tweet) -> Unit
) : RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tweet, parent, false)
        return TweetViewHolder(view)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val tweet = tweetList[position]

        // Create a SpannableStringBuilder for formatting
        val formattedText = SpannableStringBuilder()

        // Bold name and surname
        val nameText = "${tweet.firstName} ${tweet.lastName}\n\n"
        formattedText.append(nameText)
        val start = formattedText.length - nameText.length
        val end = formattedText.length
        formattedText.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Append tweet content
        val contentText = tweet.content
        formattedText.append(contentText)

        // Set the text to the TextView
        holder.tweetTextView.text = formattedText

        // Format the timestamp
        holder.timestampTextView.text = formatTimestamp(tweet.timestamp)

        // Show the delete button only if the logged-in user is the owner of the tweet
        if (tweet.userId == currentUserId) {
            holder.deleteButton.visibility = View.VISIBLE
        } else {
            holder.deleteButton.visibility = View.GONE
        }

        // Update like button text and likes count
        holder.likeButton.text = if (tweet.likedBy.contains(currentUserId)) "Liked" else "Like"
        holder.likesCountTextView.text = "${tweet.likesCount} Likes"

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            onDeleteClicked(tweet) // Pass the tweet to the delete function
        }

        // Handle like button click
        holder.likeButton.setOnClickListener {
            toggleLike(tweet, holder) // Toggle like/unlike
        }
    }

    private fun toggleLike(tweet: Tweet, holder: TweetViewHolder) {
        if (tweet.likedBy.contains(currentUserId)) {
            // Unlike
            tweet.likedBy.remove(currentUserId)
            tweet.likesCount--
            holder.likeButton.text = "Like"
        } else {
            // Like
            tweet.likedBy.add(currentUserId)
            tweet.likesCount++
            holder.likeButton.text = "Liked"
        }

        // Update the database with the new like information
        updateTweetLikesInDatabase(tweet)

        // Update likes count in UI
        holder.likesCountTextView.text = "${tweet.likesCount} Likes"
    }

    private fun updateTweetLikesInDatabase(tweet: Tweet) {
        val tweetRef = FirebaseDatabase.getInstance().reference.child("tweets").child(tweet.tweetId)
        tweetRef.setValue(tweet).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Update succeeded
            } else {
                // Handle failure
            }
        }
    }

    override fun getItemCount(): Int {
        return tweetList.size
    }

    class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tweetTextView: TextView = itemView.findViewById(R.id.tweetTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val likeButton: Button = itemView.findViewById(R.id.likeButton)
        val likesCountTextView: TextView = itemView.findViewById(R.id.likesCountTextView)
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}
