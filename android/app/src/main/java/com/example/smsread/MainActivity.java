package com.example.smsread;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

    private static final String CHANNEL = "com.example.smsapp/sms";
    private static final int SMS_PERMISSION_CODE = 100; // Unique request code
    private SMSBroadcastReceiver smsBroadcastReceiver; // Declare the receiver here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestSmsPermission(); // Request SMS permission on app start

        // Initialize and register the broadcast receiver to receive SMS data
        smsBroadcastReceiver = new SMSBroadcastReceiver();
        IntentFilter filter = new IntentFilter("SMS_RECEIVED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(smsBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    private void requestSmsPermission() {
        // Check if SMS permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the receiver when the app goes into the background
        if (smsBroadcastReceiver != null) {
            unregisterReceiver(smsBroadcastReceiver);
            smsBroadcastReceiver = null; // Clear the reference
        }
    }

    // Handle permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            // If permission is granted, we can proceed with SMS operations
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "SMS permission granted");
            } else {
                Log.d("MainActivity", "SMS permission denied");
            }
        }
    }

    // BroadcastReceiver to receive the SMS data and send it to Flutter
    public class SMSBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String smsBody = intent.getStringExtra("sms_body");
            String smsAddress = intent.getStringExtra("sms_address");

            Log.d("SMSBroadcastReceiver", "SMS Received: " + smsBody + " from: " + smsAddress);

            new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), CHANNEL)
                    .invokeMethod("onSMSReceived", new java.util.HashMap<String, String>() {{
                        put("body", smsBody);
                        put("address", smsAddress);
                    }});
        }
    }
}
