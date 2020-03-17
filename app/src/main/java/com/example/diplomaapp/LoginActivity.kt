package com.example.diplomaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText

class LoginActivity : AppCompatActivity() {

    // Creating login screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailAddress = findViewById<EditText>(R.id.et_Email)
        val password = findViewById<EditText>(R.id.et_Pass)
        val loginButton = findViewById<Button>(R.id.loginButton)

        var emailAddressText = ""
        var passwordText = ""

        loginButton.setOnClickListener {
            emailAddressText = emailAddress.text.toString()
            passwordText = password.text.toString()
            if (emailAddressText == "Krzysztof" && passwordText == "Walaszek")
                Log.d("IMPORTANT", "You are administator!")

        }
    }
}
