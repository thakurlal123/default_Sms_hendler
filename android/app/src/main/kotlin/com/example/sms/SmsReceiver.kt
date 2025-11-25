package com.example.sms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException


import okhttp3.RequestBody



class SmsReceiver : BroadcastReceiver() {

    private val client = OkHttpClient()

    @RequiresPermission(allOf = [Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE])
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                for (sms in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {

                    val sender = sms.originatingAddress ?: ""
                    val body = sms.displayMessageBody ?: ""
                    val date = (sms.timestampMillis / 1000).toString()
                print("message from "+sender +"body"+body)
                    Toast.makeText(context, "📩 $body", Toast.LENGTH_SHORT).show()

                    val telephonyManager =
                        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                    val receiverNo: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        telephonyManager.line1Number ?: "UNKNOWN"
                    } else {
                        telephonyManager.line1Number ?: "UNKNOWN"
                    }

                    saveSmsToApi(context, sender, body, date, receiverNo)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveSmsToApi(
        context: Context,
        from: String,
        body: String,
        date: String,
        receiverNo: String
    ) {
        val url = "";

        val json = JSONObject().apply {
            put("senderid", from)
            put("message", body)
            put("trndate", date)
            put("landing_no", receiverNo)
        }
        println("📤 Sending JSON: $json")

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ Network error: ${e.message}")

                // Store SMS locally for retry
                val store = SmsLocalStore(context)
                store.saveFailedSms(SmsModel("",from, body, date, receiverNo))


                val failedSmsList = store.getAllFailedSms()

                println("📦 Locally Saved SMS:")
                for (sms in failedSmsList) {

                    println("Id:${sms.id} Sender: ${sms.sender}, Body: ${sms.body}, Date: ${sms.date}, Receiver: ${sms.receiverNo}")
                }

            }

            override fun onResponse(call: Call, response: Response) {
                val statusCode = response.code
                println("🔁 API Response Code: $statusCode")

                if (!response.isSuccessful) {
                    println("⚠️ API call failed with status: $statusCode")

                    // Save locally for retry
                    val store = SmsLocalStore(context)
                    store.saveFailedSms(SmsModel("",from, body, date, receiverNo))
                } else {
                    val store = SmsLocalStore(context)
                    val failedSmsList = store.getAllFailedSms()

                    println("📦 Locally Saved SMS:")
                    if(failedSmsList.isNotEmpty()){
                        for (sms in failedSmsList) {
                            saveAndDelete(context,sms)
                            println("Id:${sms.id} Sender: ${sms.sender}, Body: ${sms.body}, Date: ${sms.date}, Receiver: ${sms.receiverNo}")
                        }
                    }

                    println("✅ SMS successfully sent with status: $statusCode")
                }

                response.close()
            }
        })
    }

    private fun saveAndDelete(context: Context,sms: SmsModel) {
        val url ='';

        val json = JSONObject().apply {
            put("senderid", sms.sender)
            put("message", sms.body)
            put("trndate", sms.date)
            put("landing_no", sms.receiverNo)
        }
        println("📤 Sending JSON: $json")

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ Network error: ${e.message}")


            }

            override fun onResponse(call: Call, response: Response) {
                val statusCode = response.code
                println("🔁 API Response Code: $statusCode")

                if (!response.isSuccessful) {
                    println("⚠️ API call failed with status: $statusCode")


                } else {
                    val store = SmsLocalStore(context)
                    store.deleteSms(sms.id.toInt());



                    println("✅ SMS successfully sent with status: $statusCode")
                }

                response.close()
            }
        })
    }


}
