package com.example.diplomaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class AdministratorPanel : AppCompatActivity() {

    private var mQueue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administrator_panel)
        val url = "http://192.168.0.142/get-users"
        mQueue = Volley.newRequestQueue(this)
        val userDatabaseButton = findViewById<Button>(R.id.userDatabase)

        val messages = ArrayList<Message>()

        userDatabaseButton.setOnClickListener {
            /*
            val request = JsonObjectRequest(Request.Method.POST, url, Response.Listener { response ->
                // Log.d("RESPONSE", response)
                val jsonOutput = response.getJSONArray()
                val intent = Intent(this@AdministratorPanel, InboxActivity::class.java)
                intent.putParcelableArrayListExtra("UserDatabase",messages)
                startActivity(intent)
            },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Error!")
                })

             */
            val request = JsonArrayRequest(Request.Method.POST, url, null, Response.Listener { response ->
                for (i in 0 until response.length()) {
                    val username = response.getJSONObject(i).getString("Username")
                    val firstName = response.getJSONObject(i).getString("First_Name")
                    val lastName = response.getJSONObject(i).getString("Last_Name")
                    messages.add(Message(R.drawable.walach, username, firstName + " " + lastName, "04/11/2020"))
                }

                // Log.d("LENGTH", jsonOutput.toString())
                // Log.d("NUMER_ONE",response.getJSONObject(0).getString("Username"))
                val intent = Intent(this@AdministratorPanel, InboxActivity::class.java)
                intent.putParcelableArrayListExtra("UserDatabase",messages)
                intent.putExtra("Title","List of users")
                // TODO Change to Boolean
                intent.putExtra("SendMessageIcon","false")
                startActivity(intent)
            },
                Response.ErrorListener { error ->
                    Log.d("ERROR", error.toString())
                })

            // Add request to Queue
            mQueue!!.add(request)

        }

    }
}
