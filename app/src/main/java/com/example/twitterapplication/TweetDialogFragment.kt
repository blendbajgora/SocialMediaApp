package com.example.twitterapplication

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class TweetDialogFragment : DialogFragment() {

    private lateinit var tweetDialogListener: TweetDialogListener
    private lateinit var tweetContentInput: EditText

    interface TweetDialogListener {
        fun onTweetPosted(tweetContent: String)
    }

    fun setTweetDialogListener(listener: TweetDialogListener) {
        tweetDialogListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_tweet, null)
        tweetContentInput = view.findViewById(R.id.tweetContentInput)

        builder.setView(view)
            .setTitle("Post a Tweet")
            .setPositiveButton("Tweet") { _, _ ->
                val tweetContent = tweetContentInput.text.toString()
                if (tweetContent.isNotEmpty()) {
                    tweetDialogListener.onTweetPosted(tweetContent) // Call the interface method
                } else {
                    Toast.makeText(requireContext(), "Please enter a tweet!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

}