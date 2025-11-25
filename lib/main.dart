import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'dart:convert';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const platform = MethodChannel("com.example.sms/chat");

  List<Map<String, dynamic>> failedSmsList = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    setDefaultSMSApp();
    fetchFailedSms();
  }

  Future<void> setDefaultSMSApp() async {
    try {
      await platform.invokeMethod('setDefaultSms');
    } on PlatformException catch (e) {
      print("⚠️ Error setting default SMS app: $e");
    }
  }

  Future<void> fetchFailedSms() async {

    try {
      final String result = await platform.invokeMethod('getFailedSms');
      final List<dynamic> jsonList = json.decode(result);
      setState(() {
        failedSmsList = jsonList.cast<Map<String, dynamic>>();
        isLoading = false;
      });
    } on PlatformException catch (e) {
      print("❌ Failed to get SMS: ${e.message}");
      setState(() => isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    fetchFailedSms();
    return MaterialApp(
      title: 'Failed SMS Viewer',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primarySwatch: Colors.blue),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('📨 Inbox SMS'),
          actions: [
            IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: fetchFailedSms,
            ),
          ],
        ),
        body: isLoading
            ? const Center(child: CircularProgressIndicator())
            : failedSmsList.isEmpty
            ? const Center(child: Text('No failed SMS found'))
            : ListView.builder(
          itemCount: failedSmsList.length,
          itemBuilder: (context, index) {
            final sms = failedSmsList[index];
            return Card(
              margin:
              const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
              elevation: 3,
              child: ListTile(
                leading: const Icon(Icons.sms, color: Colors.red),
                title: Text(
                  sms['sender'] ?? 'Unknown',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
                subtitle: Text(sms['body'] ?? ''),
                trailing: Text(
                  sms['date'] ?? '',
                  style: const TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}


// import 'package:flutter/services.dart';
// import 'package:flutter/material.dart';
//
// void main() async {
//   WidgetsFlutterBinding.ensureInitialized();
//
//   runApp(const MyApp());
// }
//
// class MyApp extends StatefulWidget {
//   const MyApp({super.key});
//
//   @override
//   State<StatefulWidget> createState() {
//     return _MyApp();
//   }
// }
//
// class _MyApp extends State<MyApp> {
//   static const platform = MethodChannel("com.example.sms/chat");
//
//   Future<void> setDefaultSMSApp() async {
//     try {
//       platform.invokeMethod('setDefaultSms');
//     } on PlatformException catch (e) {
//       print("Error: $e");
//     }
//   }
//
//   @override
//   void initState() {
//     super.initState();
//     setDefaultSMSApp();
//   }
//
//   @override
//   Widget build(BuildContext context) {
//     return MaterialApp(
//       title: 'Flutter Demo',
//       debugShowCheckedModeBanner: false,
//       theme: ThemeData(
//         primarySwatch: Colors.blue,
//       ),
//
//       home: Container(
//         decoration: const BoxDecoration(color: Colors.white),
//       ),
//     );
//   }
// }
