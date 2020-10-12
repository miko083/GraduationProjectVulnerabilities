package com.example.diplomaapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParserException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException


class LoginActivity : AppCompatActivity() {

    // Creating login screen
    private var mQueue: RequestQueue? = null
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

        val url = "http://192.168.0.142/"
        mQueue = Volley.newRequestQueue(this)

        loginButton.setOnClickListener {
            emailAddressText = emailAddress.text.toString()
            passwordText = password.text.toString()
            // Put into SharedPref for future use - use adb to pull it out from device.
            editor.putString("user", emailAddressText)
            editor.putString("password",passwordText)
            editor.apply()

            // Check if email and password have correct length. If correct, build JSON to send.
            if (emailAddress.length() == 0 || password.length() < 8)
                Toast.makeText(this, "Wrong username or password input!", Toast.LENGTH_SHORT).show()
            else {
                var loginData: JSONObject = JSONObject()
                loginData.put("inquiry","login")
                loginData.put("username",emailAddressText)
                loginData.put("password",passwordText)

                // Request for JSON
                val request =
                    JsonObjectRequest(
                        Request.Method.POST, url, loginData,
                        Response.Listener { response ->
                            try {
                                val jsonOutput = response.getString("status")
                                Log.d("VOLLEY",jsonOutput)
                                if (jsonOutput == "Passed.")
                                    startActivity(Intent(this@LoginActivity, InboxActivity::class.java))
                                else
                                    Toast.makeText(this, "Wrong password or username.", Toast.LENGTH_SHORT).show()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Log.d("VOLLEY",e.printStackTrace().toString())
                            }
                        }, Response.ErrorListener { error -> error.printStackTrace() })
                // Add request to Queue
                mQueue!!.add(request)
            }

            // Checking for admin rights
            if (emailAddressText == "Krzysztof" && passwordText == "Walaszek") {
                Log.d("IMPORTANT", "You are administator!")
            }
        }

        registerButton.setOnClickListener{
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }

    fun Activity.getVolleyError(error: VolleyError): String {
        var errorMsg = ""
        if (error is NoConnectionError) {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var activeNetwork: NetworkInfo? = null
            activeNetwork = cm.activeNetworkInfo
            errorMsg = if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
                "Server is not connected to the internet. Please try again"
            } else {
                "Your device is not connected to internet.please try again with active internet connection"
            }
        } else if (error is NetworkError || error.cause is ConnectException) {
            errorMsg = "Your device is not connected to internet.please try again with active internet connection"
        } else if (error.cause is MalformedURLException) {
            errorMsg = "That was a bad request please try again…"
        } else if (error is ParseError || error.cause is IllegalStateException || error.cause is JSONException || error.cause is XmlPullParserException) {
            errorMsg = "There was an error parsing data…"
        } else if (error.cause is OutOfMemoryError) {
            errorMsg = "Device out of memory"
        } else if (error is AuthFailureError) {
            errorMsg = "Failed to authenticate user at the server, please contact support"
        } else if (error is ServerError || error.cause is ServerError) {
            errorMsg = "Internal server error occurred please try again...."
        } else if (error is TimeoutError || error.cause is SocketTimeoutException || error.cause is ConnectTimeoutException || error.cause is SocketException || (error.cause!!.message != null && error.cause!!.message!!.contains(
                "Your connection has timed out, please try again"
            ))
        ) {
            errorMsg = "Your connection has timed out, please try again"
        } else {
            errorMsg = "An unknown error occurred during the operation, please try again"
        }
        return errorMsg
    }

}
