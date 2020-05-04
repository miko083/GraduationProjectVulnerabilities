package com.example.diplomaapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import java.text.FieldPosition

class InboxActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        supportActionBar!!.title = "Messages"

        val messagesList = findViewById<ListView>(R.id.messagesListView) as ListView

        val customAdapter = CustomAdapter(this, data)
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
        }
        return true
    }

    class Message(private var avatar: Int, private var personName: String, private var personMessage: String, private var messageDate: String){

        fun getAvatar(): Int {
            return avatar
        }

        fun getPersonName(): String {
            return personName
        }

        fun getPersonMessage(): String {
            return personMessage
        }

        fun getMessageDate(): String {
            return messageDate
        }
    }

    class CustomAdapter(private var myContext: Context, private var messages: ArrayList<Message>) : BaseAdapter() {

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

            // To avoid warning - only if myView is not null - create new one.
            if (myView == null)
                myView = LayoutInflater.from(myContext).inflate(R.layout.custom_list_layout, viewGroup, false)

            val message = this.getItem(position) as Message

            val imageView = myView!!.findViewById<ImageView>(R.id.personAvatar)
            val personName = myView.findViewById<TextView>(R.id.personName)
            val personMessage = myView.findViewById<TextView>(R.id.personMessage)
            val messageDateReceived = myView.findViewById<TextView>(R.id.messageDateReceived)

            imageView.setImageResource(message.getAvatar())
            personName.text = message.getPersonName()
            personMessage.text = message.getPersonMessage()
            messageDateReceived.text = message.getMessageDate()

            myView.setOnClickListener {
                Toast.makeText(myContext, message.getPersonMessage(), Toast.LENGTH_SHORT).show()
            }

            return myView
        }
    }

    // Probably unnecessary
    //private lateinit var adapter: CustomAdapter
    //private lateinit var messagesListView: ListView

    private val data: ArrayList<Message> get() {
        val messages = ArrayList<Message>()
        messages.add(Message(R.drawable.testo, "Testoviron", "Co tam", "2 April 21:37"))
        messages.add(Message(R.drawable.walach, "Walaszek", "Na kiedy plakat z chemii?", "5 March 10:20"))
        return messages
    }
}
