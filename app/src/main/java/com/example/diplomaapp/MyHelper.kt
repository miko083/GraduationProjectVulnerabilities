package com.example.diplomaapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MyHelper(context: Context?) : SQLiteOpenHelper(context, "Draft",null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE DRAFT_TABLE (MESSAGE TEXT, SENDER TEXT, RECIPIENT TEXT)")
        db?.execSQL("INSERT INTO DRAFT_TABLE(MESSAGE,SENDER,RECIPIENT) VALUES ('TV Walach','Bednarz','Adam')")
        db?.execSQL("INSERT INTO DRAFT_TABLE(MESSAGE,SENDER,RECIPIENT) VALUES ('Hej Tu Walach','Test1','Test2')")
        //og.d("TEST", db?.execSQL("SELECT * FROM DRAFT_TABLE").toString())
        //db?.execSQL("INSERT INTO ACTABLE(NAME,MEANING) VALUES ('BCA','Bachelor of Computer Applications')")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}