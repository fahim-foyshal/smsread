package com.example.smsread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdus.length];
                String smsBody = "";
                String address = "";

                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    smsBody += messages[i].getMessageBody();
                    address = messages[i].getOriginatingAddress();
                }

                // Log the received SMS
                Log.d("SMSReceiver", "SMS Received: " + smsBody + " from: " + address);

                // Send a broadcast to the Flutter activity
                Intent smsIntent = new Intent("SMS_RECEIVED");
                smsIntent.putExtra("sms_body", smsBody);
                smsIntent.putExtra("sms_address", address);
                context.sendBroadcast(smsIntent);
            }
        }
    }
}
