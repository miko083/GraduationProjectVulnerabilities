package com.example.diplomaapp

import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley

class InboxActivity : AppCompatActivity() {

    private var mQueue: RequestQueue? = null
    private var userName = ""
    private var previewMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        previewMessage = intent.getStringExtra("PreviewMessage")
        userName = intent.getStringExtra("UserName")
        if ( userName != null )
            supportActionBar!!.title = "Messages " + userName
        else {
            supportActionBar!!.title = "Messages"
            userName = ""
        }

        val messagesList = findViewById<ListView>(R.id.messagesListView) as ListView

        var messages = intent?.getParcelableArrayListExtra<Message>("UserList")

        // ArrayList<Message>() messages = intent.getParcelableArrayListExtra("UserDatabase")

        // val customAdapter = CustomAdapter(this,messages)
        val customAdapter = messages?.let { CustomAdapter(this, it, supportFragmentManager, applicationContext, contentResolver, userName, previewMessage ) }
        messagesList.adapter = customAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var menuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.messages_view_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> Toast.makeText(this, "Selected Settings", Toast.LENGTH_SHORT).show()
            R.id.userPanel -> Toast.makeText(this, "Selected UserPanel", Toast.LENGTH_SHORT).show()
            R.id.camera -> openCameraApp()
        }
        return true
    }

    private fun openCameraApp(){
        var showSendMessageIcon = intent.getStringExtra("SendMessageIcon")
        if (showSendMessageIcon == "true") {
            val url = "http://192.168.0.142/get-users"
            mQueue = Volley.newRequestQueue(this)
            val messages = ArrayList<Message>()
            val request = JsonArrayRequest(
                Request.Method.POST, url, null, Response.Listener { response ->
                val jsonOutput = response.length()
                for (i in 0 until response.length()) {
                    val username = response.getJSONObject(i).getString("Username")
                    val firstName = response.getJSONObject(i).getString("First_Name")
                    val lastName = response.getJSONObject(i).getString("Last_Name")
                    messages.add(Message(R.drawable.walach, username, firstName + " " + lastName, "04/11/2020"))
                }

                // Log.d("LENGTH", jsonOutput.toString())
                // Log.d("NUMER_ONE",response.getJSONObject(0).getString("Username"))
                val intent = Intent(this@InboxActivity, InboxActivity::class.java)
                intent.putParcelableArrayListExtra("UserList",messages)
                    intent.putExtra("UserName", userName)
                //intent.putExtra("Title","Choose user to send Message")
                // TODO Change to Boolean TODO Move to seperate function
                intent.putExtra("SendMessageIcon","false")
                    intent.putExtra("PreviewMessage", "false")
                startActivity(intent)
            },
                Response.ErrorListener { error ->
                    Log.d("ERROR", error.toString())
                })

            // Add request to Queue
            mQueue!!.add(request)
        }
    }

    class CustomAdapter(private var myContext: Context, private var messages: ArrayList<Message>, private var mySupportFragmentManager: FragmentManager, private var applicationContext: Context, private var contentResolver: ContentResolver, private var userName : String, private var previewMessage: String) : BaseAdapter() {

        override fun getCount(): Int {
            return messages.size
        }

        override fun getItem(position: Int): Any {
            return messages[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
            var myView = view
            var savedDraft = ""
            var helper = MyHelper(applicationContext)
            var db: SQLiteDatabase = helper.readableDatabase

            val message = this.getItem(position) as Message
            var recipient = message.getPersonName()

            var rs = contentResolver.query(DraftProviders.CONTENT_URI, arrayOf(DraftProviders.MESSAGE,DraftProviders.SENDER, DraftProviders.RECIPIENT),"SENDER='$userName' AND RECIPIENT='$recipient'",null,null)
            if(rs?.moveToNext() !! ) {
                savedDraft = rs.getString(0)
                if (savedDraft != null)
                    Toast.makeText(myContext, "Read from drafts...", Toast.LENGTH_SHORT).show()
            }

            // To avoid warning - only if myView is not null - create new one.
            if (myView == null)
                myView = LayoutInflater.from(myContext).inflate(R.layout.custom_list_layout, viewGroup, false)

            val imageView = myView!!.findViewById<ImageView>(R.id.personAvatar)
            val personName = myView.findViewById<TextView>(R.id.personName)
            val personMessage = myView.findViewById<TextView>(R.id.personMessage)
            val messageDateReceived = myView.findViewById<TextView>(R.id.messageDateReceived)

            imageView.setImageResource(message.getAvatar())
            personName.text = message.getPersonName()
            personMessage.text = message.getPersonMessage()
            messageDateReceived.text = message.getMessageDate()

            myView.setOnClickListener {
                if (previewMessage == "false") {
                    Toast.makeText(myContext, message.getPersonMessage(), Toast.LENGTH_SHORT).show()
                    val args = Bundle()
                    args.putString("Message", savedDraft)
                    args.putString("Sender", userName)
                    args.putString("Recipient", recipient)
                    val dialog = MyDialog()
                    dialog.setArguments(args)
                    dialog.show(mySupportFragmentManager, "Dialog")
                }
                else{
                    val intent = Intent(myContext, PreviewMessage::class.java)
                    intent.putExtra("UserName", userName)
                    intent.putExtra("Recipient", recipient)
                    myContext.startActivity(intent)
                }

            }

            return myView
        }
    }

    class MyDialog : DialogFragment() {

        private var message: String? = null
        private var sender: String? = null
        private var recipient: String? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            message = arguments?.getString("Message")
            sender = arguments?.getString("Sender")
            recipient = arguments?.getString("Recipient")
            return super.onCreateDialog(savedInstanceState)
        }


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            var myView = inflater.inflate(R.layout.write_message, container, false)
            var saveToDraftButton = myView.findViewById<Button>(R.id.saveToDraft)
            var selectPhoto = myView.findViewById<Button>(R.id.selectPhoto)
            var writeMessage = myView.findViewById<EditText>(R.id.et_writeMessage)
            writeMessage.setText(message)
            saveToDraftButton.setOnClickListener {
                context?.contentResolver?.delete(DraftProviders.CONTENT_URI, "SENDER='$sender' AND RECIPIENT='$recipient'", null)
                var cv = ContentValues()
                cv.put(DraftProviders.SENDER,sender)
                cv.put(DraftProviders.RECIPIENT,recipient)
                cv.put(DraftProviders.MESSAGE,writeMessage.text.toString())
                context?.contentResolver?.insert(DraftProviders.CONTENT_URI, cv)
                Toast.makeText(context, "Saved for future use...", Toast.LENGTH_SHORT).show()
            }

            selectPhoto.setOnClickListener {
                val intent = Intent(this.context, TakePhotoActivity::class.java)
                intent.putExtra("Sender", sender)
                intent.putExtra("Recipient", recipient)
                intent.putExtra("Message", message)
                startActivity(intent)
            }

            getDialog()!!.getWindow()
            return myView
        }

        override fun onStart() {
            super.onStart()
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
            dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}
