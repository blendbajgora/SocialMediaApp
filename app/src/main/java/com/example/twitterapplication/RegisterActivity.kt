package com.example.twitterapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var nameInput: EditText
    private lateinit var surnameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var backButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        nameInput = findViewById(R.id.nameInput)
        surnameInput = findViewById(R.id.surnameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)

        // Initialize backButton
        backButton = findViewById(R.id.backButton)

        // Set up back button functionality
        backButton.setOnClickListener {
            finish() // This will close the current activity and return to the previous one
        }

        // Register a new user
        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val surname = surnameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isNotEmpty() && surname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Store additional user information in Firebase Realtime Database
                        val userId = auth.currentUser?.uid
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

                        val user = hashMapOf<String, String>(
                            "name" to name,
                            "surname" to surname,
                            "email" to email
                        )

                        userRef.setValue(user).addOnCompleteListener { databaseTask ->
                            if (databaseTask.isSuccessful) {
                                Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()

                                // Sign out the user after registration
                                auth.signOut()

                                // Redirect to login screen
                                val intent = Intent(this, AuthActivity::class.java)
                                startActivity(intent)
                                finish() // Close the RegisterActivity
                            } else {
                                Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
