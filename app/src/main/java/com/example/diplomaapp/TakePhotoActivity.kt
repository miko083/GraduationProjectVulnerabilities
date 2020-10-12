// ---------- BASED ON ----------
// https://medium.com/better-programming/how-to-upload-an-image-file-to-your-server-using-volley-in-kotlin-a-step-by-step-tutorial-23f3c0603ec2
// -----------------------------

package com.example.diplomaapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import java.io.IOException

class TakePhotoActivity : AppCompatActivity() {
    // Set variables
    // Lateinit to avoid null check
    private lateinit var imageView: ImageView
    private var imageData: ByteArray? = null
    private val postURL: String = "http://192.168.0.142/send-image"

    // IMAGE_PICK_CODE to open gallery
    companion object {
        private const val IMAGE_PICK_CODE = 999
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_photo)

        // Assign imageView and buttons to the layout file.
        imageView = findViewById(R.id.imageView)

        val imageButton = findViewById<Button>(R.id.imageButton)
        imageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            // Set intent type to open gallery.
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
        val sendButton = findViewById<Button>(R.id.sendButton)
        sendButton.setOnClickListener {
            uploadImage()
        }
    }

    private fun uploadImage() {
        imageData?: return
        val request = object : VolleyFileUploadRequest(
            Method.POST,
            postURL,
            Response.Listener {
                Log.d("UPLOAD_IMAGE_RESPONSE","Response is: $it")
            },
            Response.ErrorListener {
                Log.d("UPLOAD_IMAGE_ERROR","Error is: $it")
            }
        ) {
            // Insert parameters for request - must match in Flask app.
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                params["image"] = FileDataPart("image", imageData!!, "jpeg")
                return params
            }
        }
        // Add to the Queue
        Volley.newRequestQueue(this).add(request)
    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val uri = data?.data
            if (uri != null) {
                imageView.setImageURI(uri)
                createImageData(uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}