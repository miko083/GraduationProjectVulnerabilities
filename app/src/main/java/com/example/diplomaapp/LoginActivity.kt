package com.example.diplomaapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {

    // Creating login screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailAddress = findViewById<EditText>(R.id.et_Email)
        val password = findViewById<EditText>(R.id.et_Pass)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        var emailAddressText = ""
        var passwordText = ""

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        loginButton.setOnClickListener {
            emailAddressText = emailAddress.text.toString()
            passwordText = password.text.toString()
            // Put into SharedPref for future use - use adb to pull it out from device.
            editor.putString("user", emailAddressText)
            editor.putString("password",passwordText)
            editor.apply()

            startActivity(Intent(this@LoginActivity, InboxActivity::class.java))
            //Todo check if user and password are correct
            if (emailAddressText == "Krzysztof" && passwordText == "Walaszek") {
                Log.d("IMPORTANT", "You are administator!")
            }
        }

        registerButton.setOnClickListener{
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }
}
