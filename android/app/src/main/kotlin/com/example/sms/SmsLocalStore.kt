package com.example.sms

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class SmsModel(
    val id: String,
    val sender: String,
    val body: String,
    val date: String,
    val receiverNo: String
)

class SmsLocalStore(context: Context) :
    SQLiteOpenHelper(context, "sms_store.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE sms_failed (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender TEXT, " +
                    "body TEXT, " +
                    "date TEXT, " +
                    "receiverNo TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS sms_failed")
        onCreate(db)
    }

    fun saveFailedSms(sms: SmsModel) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sender", sms.sender)
            put("body", sms.body)
            put("date", sms.date)
            put("receiverNo", sms.receiverNo)
        }
        db.insert("sms_failed", null, values)
        db.close()
    }

    fun getAllFailedSms(): List<SmsModel> {
        val list = mutableListOf<SmsModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM sms_failed", null)
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val sender = cursor.getString(cursor.getColumnIndexOrThrow("sender"))
            val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val receiverNo = cursor.getString(cursor.getColumnIndexOrThrow("receiverNo"))
            list.add(SmsModel(id,sender, body, date, receiverNo))
        }
        cursor.close()
        db.close()
        return list
    }

    fun deleteSms(id: Int) {
        val db = writableDatabase
        db.delete("sms_failed", "id=?", arrayOf(id.toString()))
        db.close()
    }
}
