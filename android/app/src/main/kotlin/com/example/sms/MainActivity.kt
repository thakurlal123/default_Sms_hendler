package com.example.sms

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.sms/chat"
    private var pendingResult: MethodChannel.Result? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 12) {
            // Send result back to Flutter once
            pendingResult?.success(resultCode)
            pendingResult = null  // important to prevent crash
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "setDefaultSms" -> {
                        // Store this result until we get a callback from onActivityResult
                        pendingResult = result
                        setDefaultSms()
                    }
                    "getFailedSms" -> getFailedSms(result)
                    else -> result.notImplemented()
                }
            }
    }

    private fun setDefaultSms() {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                val roleManager: RoleManager = getSystemService(RoleManager::class.java)
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivityForResult(intent, 12)
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivity(intent)
                // For old versions, reply immediately
                pendingResult?.success(0)
                pendingResult = null
            }
        } catch (ex: Exception) {
            pendingResult?.error("UNAVAILABLE", "Failed to set default SMS app.", null)
            pendingResult = null
        }
    }

    private fun getFailedSms(result: MethodChannel.Result) {
        try {
            val store = SmsLocalStore(this)
            val failedSmsList = store.getAllFailedSms()

            val jsonArray = JSONArray()
            for (sms in failedSmsList) {
                val json = JSONObject().apply {
                    put("sender", sms.sender)
                    put("body", sms.body)
                    put("date", sms.date)
                    put("receiverNo", sms.receiverNo)
                }
                jsonArray.put(json)
            }

            result.success(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            result.error("FAILED", "Could not fetch failed SMS", e.message)
        }
    }
}


//package com.example.sms
//
//import android.app.role.RoleManager
//import android.content.Intent
//import android.os.Build
//import android.provider.Telephony
//import androidx.annotation.NonNull
//import io.flutter.embedding.android.FlutterActivity
//import io.flutter.embedding.engine.FlutterEngine
//import io.flutter.plugin.common.MethodChannel
//import org.json.JSONArray
//import org.json.JSONObject
//
//class MainActivity : FlutterActivity() {
//    private val CHANNEL = "com.example.sms/chat"
//    private var flutterResult: MethodChannel.Result? = null
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == 12) {
//            flutterResult?.success(resultCode)
//        }
//    }
//
//    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
//        super.configureFlutterEngine(flutterEngine)
//
//        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
//            flutterResult = result
//
//            when (call.method) {
//                "setDefaultSms" -> setDefaultSms(result)
//                "getFailedSms" -> getFailedSms(result)
//                else -> result.notImplemented()
//            }
//        }
//    }
//
//    private fun setDefaultSms(result: MethodChannel.Result) {
//        try {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                val roleManager: RoleManager = getSystemService(RoleManager::class.java)
//                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
//                startActivityForResult(intent, 12)
//            } else {
//                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
//                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
//                startActivity(intent)
//            }
//        } catch (ex: Exception) {
//            result.error("UNAVAILABLE", "Failed to set default SMS app.", null)
//        }
//    }
//
//    private fun getFailedSms(result: MethodChannel.Result) {
//        try {
//            val store = SmsLocalStore(this)
//            val failedSmsList = store.getAllFailedSms()
//
//            val jsonArray = JSONArray()
//            for (sms in failedSmsList) {
//                val json = JSONObject().apply {
//                    put("sender", sms.sender)
//                    put("body", sms.body)
//                    put("date", sms.date)
//                    put("receiverNo", sms.receiverNo)
//                }
//                jsonArray.put(json)
//            }
//
//            // Send JSON string back to Flutter
//            result.success(jsonArray.toString())
//        } catch (e: Exception) {
//            e.printStackTrace()
//            result.error("FAILED", "Could not fetch failed SMS", e.message)
//        }
//    }
//}
//
//
//
//
//
//
//
////package com.example.sms
////
////import android.app.role.RoleManager
////import android.content.BroadcastReceiver
////import android.content.Context
////import android.content.Intent
////import android.content.IntentFilter
////import android.os.Build
////import android.provider.Telephony
////import androidx.annotation.NonNull
////import com.google.gson.JsonObject
////import io.flutter.embedding.android.FlutterActivity
////import io.flutter.embedding.engine.FlutterEngine
////import io.flutter.plugin.common.EventChannel
////import io.flutter.plugin.common.MethodChannel
////import android.widget.Toast
////
////
////class MainActivity : FlutterActivity() {
////    private val CHANNEL = "com.example.sms/chat";
////    var flutterResult: MethodChannel.Result? = null
////
////    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
////      if (requestCode == 12) {
////        flutterResult!!.success(resultCode);
////      }
////    }
////    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
////        super.configureFlutterEngine(flutterEngine)
////
////        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
////          flutterResult = result;
////          if (call.method == "setDefaultSms") {
////            try {
////              if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
////                val roleManager: RoleManager = getSystemService(RoleManager::class.java);
////                var intent = roleManager.createRequestRoleIntent (RoleManager.ROLE_SMS);
////                var res = startActivityForResult(intent, 12);
////              } else {
////                var intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
////                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, "ccom.example.sms");
////                startActivity(intent);
////              }
////            } catch (ex: Exception) {
////              result.error("UNAVAILABLE", "Setting default sms.", null);
////            }
////          } else {
////            result.notImplemented();
////          }
////
////        }
////
////    }
////
////    override fun onDestroy() {
////      super.onDestroy()
////      // Make sure to unregister the receiver to avoid leaks
////      // unregisterReceiver(smsReceiver)
////    }
////}