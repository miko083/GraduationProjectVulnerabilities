package com.example.diplomaapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.net.ssl.*


class LoginActivity : AppCompatActivity() {

    // Creating login screen
    private var mQueue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val rootCheck = RootDetection().isDeviceRooted()
        if (rootCheck)
            Toast.makeText(this, "DANGEROUS! Device Rooted! ", Toast.LENGTH_SHORT).show()

        val emailAddress = findViewById<EditText>(R.id.et_Email)
        val password = findViewById<EditText>(R.id.et_Pass)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        var emailAddressText = ""
        var passwordText = ""

        val sharedPref = getPreferences(Context.MODE_PRIVATE)

        val TRANSFORMATION = "AES/GCM/NoPadding"

        ////////////////////////
        // SERCURED SOLTUTION //
        ///////////////////////

        val editorEncryptionData = sharedPref.edit()
        var unencryptedString : String? = null
        if (sharedPref.getString("rh93hrsah83d",null) != null && sharedPref.getString("dah412D8dj*&dhsadi",null) != null) {
            var keyStore : KeyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            var secretKeyEntry : KeyStore.SecretKeyEntry = keyStore.getEntry("sample_alice",null) as KeyStore.SecretKeyEntry
            val secretKeyDecrypt : SecretKey = secretKeyEntry.secretKey
            val cipherDecrypt : Cipher = Cipher.getInstance(TRANSFORMATION)
            val spec : GCMParameterSpec = GCMParameterSpec(128, Base64.decode(sharedPref.getString("rh93hrsah83d",null),Base64.DEFAULT))
            cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeyDecrypt,spec)
            val decodedData : ByteArray = cipherDecrypt.doFinal(Base64.decode(sharedPref.getString("dah412D8dj*&dhsadi",null),Base64.DEFAULT))
            unencryptedString = String(decodedData)
        }

        var emailAddressFromSharedPref = ""
        var passwordFromSharedPref = ""
        if (unencryptedString != null) {
            var encryptedPreferences = EncryptedPreferences.Builder(this).withEncryptionPassword(unencryptedString).build();
            emailAddressFromSharedPref = encryptedPreferences.getString("user", "");
            passwordFromSharedPref = encryptedPreferences.getString("password", "");
        }
        
        val secretString = getRandomString(16)
        val iv: ByteArray
        val encryption: ByteArray
        val keyGenerator : KeyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec: KeyGenParameterSpec =
            KeyGenParameterSpec.Builder("sample_alice", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()
        keyGenerator.init(keyGenParameterSpec)
        val secretKey: SecretKey = keyGenerator.generateKey()
        val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        iv = cipher.iv
        editorEncryptionData.putString("rh93hrsah83d",Base64.encodeToString(iv,Base64.DEFAULT)).apply()
        encryption = cipher.doFinal(secretString.toByteArray())
        editorEncryptionData.putString("dah412D8dj*&dhsadi", Base64.encodeToString(encryption,Base64.DEFAULT)).apply()

        if (emailAddressFromSharedPref != null && passwordFromSharedPref != null) {
            Log.d("EMAIL", emailAddressFromSharedPref)
            Log.d("PASS", passwordFromSharedPref)
        }

        ////////////////////
        // Read variables //
        ////////////////////

        if (emailAddressFromSharedPref != "") {
            emailAddressText = emailAddressFromSharedPref
            emailAddress.setText(emailAddressText)
        }

        if (passwordFromSharedPref != "") {
            passwordText = passwordFromSharedPref
            password.setText(passwordText)
        }

        val url = "https://192.168.0.142/login"

        ////////////////////////
        // Secured with HTTPS //
        ////////////////////////

        mQueue = Volley.newRequestQueue(this, HurlStack(null, getSocketFactory()))

        loginButton.setOnClickListener {
            emailAddressText = emailAddress.text.toString()
            passwordText = password.text.toString()
            // Put into SharedPref for future use - use adb to pull it out from device. Clear first.
            // editor.clear().commit()
            var encryptedPreferences = EncryptedPreferences.Builder(this).withEncryptionPassword(secretString).build()
            var editor = encryptedPreferences.edit();
            editor.clear()
            editor.putString("user", emailAddressText).putString("password",passwordText).apply()
            // Using encrypted shared pref

            // Check if email and password have correct length. If correct, build JSON to send.
            if (emailAddressText.isEmpty() || passwordText.isEmpty())
                Toast.makeText(this, "Wrong username or password input!", Toast.LENGTH_SHORT).show()
            else {
                var loginData: JSONObject = JSONObject()
                loginData.put("UserName",emailAddressText)
                loginData.put("Password",passwordText)

                // Request for JSON
                val request =
                    JsonObjectRequest(
                        Request.Method.POST, url, loginData,
                        Response.Listener { response ->
                            try {
                                val jsonOutput = response.getString("Status")
                                Log.d("VOLLEY",jsonOutput)
                                if (jsonOutput == "Passed.") {
                                    val intent = Intent(this@LoginActivity, InboxActivity::class.java)
                                    val messages = ArrayList<Message>()
                                    val messagesFromJSON = response.getJSONArray("Messages")
                                    for (i in 0 until messagesFromJSON.length()) {
                                        val personJSON = messagesFromJSON.getJSONObject(i)
                                        val userName = personJSON.getString("UserName")
                                        val message = personJSON.getString("Message")
                                        val messageDate = personJSON.getString("MessageDate")
                                        messages.add(Message(R.drawable.walach, userName, message,"","",message,messageDate))
                                    }
                                    intent.putParcelableArrayListExtra("UserList",messages)
                                    intent.putExtra("SendMessageIcon",true)
                                    intent.putExtra("UserName", emailAddressText)
                                    intent.putExtra("PreviewMessage", true)
                                    intent.putExtra("Title", "")
                                    intent.putExtra("Admin", false)
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

    // Insert HTTP Certificate
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

    fun getRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(length) { charset.random() }.joinToString("")
    }


}
