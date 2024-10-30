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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodChannel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends FlutterActivity {

    private static final String CHANNEL = "com.example.smsapp/sms";
    private static final int SMS_PERMISSION_CODE = 100;
    private SMSBroadcastReceiver smsBroadcastReceiver;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestSmsPermission();

        // Initialize and register the SMS broadcast receiver
        smsBroadcastReceiver = new SMSBroadcastReceiver();
        IntentFilter filter = new IntentFilter("SMS_RECEIVED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(smsBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (smsBroadcastReceiver != null) {
            unregisterReceiver(smsBroadcastReceiver);
            smsBroadcastReceiver = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "SMS permission granted");
            } else {
                Log.d("MainActivity", "SMS permission denied");
            }
        }
    }

    // BroadcastReceiver to receive SMS data and send it to Flutter and the API
    public class SMSBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String smsBody = intent.getStringExtra("sms_body");
            String smsAddress = intent.getStringExtra("sms_address");

            Log.d("SMSBroadcastReceiver", "SMS Received: " + smsBody + " from: " + smsAddress);

            // Send data to Flutter via MethodChannel
            new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), CHANNEL)
                    .invokeMethod("onSMSReceived", new HashMap<String, String>() {{
                        put("body", smsBody);
                        put("address", smsAddress);
                    }});

            // Extract OTP and PID from SMS and send it to the API
//            String otp = extractOtp(smsBody);
//            String pid = extractPid(smsBody);

            if (smsAddress != null && smsAddress.trim().equalsIgnoreCase("MollikPlaza")) {
                sendOtpDataToApi(smsBody, smsAddress);
            }
        }
    }

    private String extractOtp(String message) {
        // Extract first 4-digit sequence as OTP
        Pattern pattern = Pattern.compile("\\b\\d{4}\\b");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group() : "";
    }

    private String extractPid(String message) {
        // Custom logic to extract PID (replace with your logic if needed)
        return "1234"; // Placeholder for PID extraction
    }

    private void sendOtpDataToApi(String otp, String pid) {
        String apiUrl = "https://azadenterprisebsl.com/api/api_data_push.php";

        // Create the JSON data
        JSONArray jsonData = new JSONArray();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("otp", otp);
            jsonObject.put("pid", pid);
            jsonData.put(jsonObject);
        } catch (JSONException e) {
            Log.d("APIRequest", "JSON creation error", e);
            return;
        }

        // Set media type and request body
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonData.toString(), mediaType);

        // Build the request
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("APIRequest", "Error sending OTP data: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("APIRequest", "OTP data sent successfully");
                } else {
                    Log.d("APIRequest", "Failed to send OTP data. Status code: " + response.code());
                }
            }
        });
    }
}
