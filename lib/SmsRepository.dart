import 'package:flutter/services.dart';
import 'dart:convert';

class SmsRepository {
  static const platform = MethodChannel('com.example.sms/chat');

  static Future<List<Map<String, dynamic>>> getFailedSms() async {
    try {
      final String result = await platform.invokeMethod('getFailedSms');
      final List<dynamic> jsonList = json.decode(result);
      return jsonList.cast<Map<String, dynamic>>();
    } on PlatformException catch (e) {
      print("❌ Failed to get SMS: ${e.message}");
      return [];
    }
  }
}
