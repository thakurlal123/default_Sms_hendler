package com.example.sms
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.Secure
import android.provider.Telephony
import androidx.core.content.ContextCompat.getSystemService
import com.google.gson.JsonObject

class IncomingSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle the SMS received logic here
    }
}