package com.example.diplomaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class UserPanel : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_panel)

        val userNameTextView = findViewById<TextView>(R.id.userName)
        val firstNameTextView = findViewById<TextView>(R.id.firstName)
        val lastNameTextView = findViewById<TextView>(R.id.lastName)
        val registrationDateTextView = findViewById<TextView>(R.id.registrationDate)

        supportActionBar!!.title = "User Panel"

        userNameTextView.setText("User Name: " + intent.getStringExtra("UserName"))
        firstNameTextView.setText("First Name: " + intent.getStringExtra("FirstName"))
        lastNameTextView.setText("Last Name: " + intent.getStringExtra("LastName"))
        registrationDateTextView.setText("Registration Date: " + intent.getStringExtra("RegistrationDate"))

    }
}
