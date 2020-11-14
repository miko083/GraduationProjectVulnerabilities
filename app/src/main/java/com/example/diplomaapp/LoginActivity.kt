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
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pddstudio.preferences.encrypted.EncryptedPreferences
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParserException
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*


class LoginActivity : AppCompatActivity() {

    // Creating login screen
    private var mQueue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val rootCheck = RootDetection().isDeviceRooted()
        Log.d("TEST_ROOT",rootCheck.toString())
        val emailAddress = findViewById<EditText>(R.id.et_Email)
        val password = findViewById<EditText>(R.id.et_Pass)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        var emailAddressText = ""
        var passwordText = ""

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val emailAddressFromSharedPref = sharedPref.getString("user", null)
        val passwordFromSharedPref = sharedPref.getString("password",null)

        // Log.d("EMAIL_SHARED_PREF", emailAddressFromSharedPref)
        // Log.d("PASSWORD_SHARED_PREF", passwordFromSharedPref)


        ////////////////////////
        // Testing encryption //
        ////////////////////////

        val key = "secret_key";
        val encryptedPreferences = EncryptedPreferences.Builder(this).withEncryptionPassword(key).build();
        // val editor = encryptedPreferences.edit();

        // val emailAddressFromSharedPref = encryptedPreferences.getString("user", null);
        // val passwordFromSharedPref = encryptedPreferences.getString("password", null);

        ////////////////////
        // Read variables //
        ////////////////////

        if (emailAddressFromSharedPref != null && passwordFromSharedPref != null) {
            Log.d("EMAIL_SHARED_PREF", emailAddressFromSharedPref)
            Log.d("PASSWORD_SHARED_PREF", passwordFromSharedPref)
        }

        // Read from Shared Pref
        if (emailAddressFromSharedPref != null) {
            emailAddressText = emailAddressFromSharedPref
            emailAddress.setText(emailAddressText)
        }

        if (passwordFromSharedPref != null) {
            passwordText = passwordFromSharedPref
            password.setText(passwordText)
        }

        /////////////////////////////////////////////
        // TODO More advanced using Android KeyGen //
        /////////////////////////////////////////////

        // val encryptedString = null;
        // val keyGenerator: KeyGenerator;
        // var TRANSFORMATION = "AES/GCM/NoPadding"
        // var encryption


        val url = "http://192.168.0.142/login"
        mQueue = Volley.newRequestQueue(this)

        ////////////////////////
        // Secured with HTTPS //
        ////////////////////////

        // mQueue = Volley.newRequestQueue(this, HurlStack(null, getSocketFactory()))

        loginButton.setOnClickListener {
            emailAddressText = emailAddress.text.toString()
            passwordText = password.text.toString()
            // Put into SharedPref for future use - use adb to pull it out from device. Clear first.
            // editor.clear().commit()
            editor.putString("user", emailAddressText).putString("password",passwordText).apply()
            // Using encrypted shared pref

            // Check if email and password have correct length. If correct, build JSON to send.
            if (emailAddressText.isEmpty() || passwordText.isEmpty())
                Toast.makeText(this, "Wrong username or password input!", Toast.LENGTH_SHORT).show()
            else if (emailAddressText == "Admin" && passwordText == "Admin") {
                Log.d("IMPORTANT", "You are administator!")
                startActivity(Intent(this@LoginActivity, AdministratorPanel::class.java))
            }
            else {
                var loginData: JSONObject = JSONObject()
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
                                if (jsonOutput == "Passed.") {
                                    val intent = Intent(this@LoginActivity, InboxActivity::class.java)
                                    val messages = ArrayList<Message>()
                                    val messagesFromJSON = response.getJSONArray("messages")
                                    for (i in 0 until messagesFromJSON.length()) {
                                        val personJSON = messagesFromJSON.getJSONObject(i)
                                        val userName = personJSON.getString("User_name")
                                        val message = personJSON.getString("Message")
                                        messages.add(Message(R.drawable.walach, userName, message, "04/11/2020"))
                                    }
                                    intent.putParcelableArrayListExtra("UserList",messages)
                                    intent.putExtra("SendMessageIcon","true")
                                    intent.putExtra("UserName", emailAddressText)
                                    intent.putExtra("PreviewMessage", "true")
                                    startActivity(intent)
                                }
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

    private fun getSocketFactory(): SSLSocketFactory? {
        var cf: CertificateFactory? = null
        try {
            cf = CertificateFactory.getInstance("X.509")
            val caInput: InputStream = resources.openRawResource(R.raw.serverpublic)
            val ca: Certificate
            try {
                ca = cf.generateCertificate(caInput)
                Log.e("CERT", "ca=" + (ca as X509Certificate).getSubjectDN())
            } finally {
                caInput.close()
            }
            val keyStoreType: String = KeyStore.getDefaultType()
            val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)
            val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
            val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)
            val hostnameVerifier: HostnameVerifier = object : HostnameVerifier {
                override fun verify(hostname: String, session: SSLSession): Boolean {
                    Log.e("CipherUsed", session.getCipherSuite())
                    return hostname.compareTo("192.168.0.142") == 0 // The Hostname of your server.
                }
            }
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier)
            var context: SSLContext? = null
            context = SSLContext.getInstance("TLS")
            context.init(null, tmf.getTrustManagers(), null)
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory())
            return context.getSocketFactory()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
        return null
    }


}
