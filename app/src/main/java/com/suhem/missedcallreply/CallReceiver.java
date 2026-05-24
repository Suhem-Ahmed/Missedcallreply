package com.suhem.missedcallreply;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.Calendar;

public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "MissedCallReply";
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static String incomingNumber = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.intent.action.PHONE_STATE")) return;

        String state  = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (number != null && !number.isEmpty()) {
            incomingNumber = number;
        }

        // Detect: was RINGING → now IDLE = missed call
        if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING)
                && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Log.d(TAG, "Missed call from: " + incomingNumber);
            handleMissedCall(context, incomingNumber);
        }

        lastState = state;
    }

    private void handleMissedCall(Context context, String number) {
        if (number == null || number.isEmpty()) {
            Log.d(TAG, "No number available, skipping");
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("MissedCallReply", Context.MODE_PRIVATE);
        boolean allCalls   = prefs.getBoolean("allCalls",   false);
        boolean timeWindow = prefs.getBoolean("timeWindow", false);
        String  message    = prefs.getString("message",    "Hi! I missed your call. I'll get back to you soon.");

        if (!allCalls && !timeWindow) {
            Log.d(TAG, "Both modes disabled, not sending");
            return;
        }

        if (timeWindow && !isWithinTimeWindow(prefs)) {
            Log.d(TAG, "Outside time window, not sending");
            return;
        }

        sendSMS(number, message);
    }

    private boolean isWithinTimeWindow(SharedPreferences prefs) {
        int startHour = prefs.getInt("startHour", 10);
        int startMin  = prefs.getInt("startMin",   0);
        int endHour   = prefs.getInt("endHour",   15);
        int endMin    = prefs.getInt("endMin",      0);

        Calendar now   = Calendar.getInstance();
        int nowMins    = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int startMins  = startHour * 60 + startMin;
        int endMins    = endHour   * 60 + endMin;

        return nowMins >= startMins && nowMins <= endMins;
    }

    private void sendSMS(String number, String message) {
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(number, null, message, null, null);
            Log.d(TAG, "SMS sent to " + number);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
        }
    }
}
