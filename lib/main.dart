import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: SMSHomePage(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class SMSHomePage extends StatefulWidget {
  @override
  _SMSHomePageState createState() => _SMSHomePageState();
}

class _SMSHomePageState extends State<SMSHomePage> {
  static const platform = MethodChannel('com.example.smsapp/sms');
  String _smsBody = "No SMS received yet.";
  String _smsAddress = "Unknown";

  @override
  void initState() {
    super.initState();
    platform.setMethodCallHandler(_onSMSReceived);
  }

  Future<void> _onSMSReceived(MethodCall call) async {
    if (call.method == "onSMSReceived") {
      final sms = call.arguments as Map;
      setState(() {
        _smsBody = sms["body"] ?? "No content";
        _smsAddress = sms["address"] ?? "Unknown";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("SMS Listener"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text("From: $_smsAddress", style: TextStyle(fontSize: 20)),
            SizedBox(height: 10),
            Text("Message: $_smsBody", style: TextStyle(fontSize: 16)),
          ],
        ),
      ),
    );
  }
}
