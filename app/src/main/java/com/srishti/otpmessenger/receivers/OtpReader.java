package com.srishti.otpmessenger.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;
import android.util.Log;
import android.text.TextUtils;

import com.srishti.otpmessenger.MainActivity;
import com.srishti.otpmessenger.R;
import com.srishti.otpmessenger.interfaces.OTPListener;
import com.srishti.otpmessenger.servcies.OtpWakefulService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OtpReader extends WakefulBroadcastReceiver {


    /**
     * Constant TAG for logging key.
     */
    private static final String TAG = "OtpReader";
    public static final String OTP_DELIMITER = ":";

    /**
     * The bound OTP Listener that will be trigerred on receiving message.
     */
    private static OTPListener otpListener;

    /**
     * The Sender number string.
     */
    private static String receiverString;

    /**
     * Binds the sender string and listener for callback.
     *
     * @param listener
     * @param sender
     */
    public static void bind(OTPListener listener) {
        otpListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Pattern p = Pattern.compile("(|^)\\d{6}");

            final Object[] pdusArr = (Object[]) bundle.get("pdus");

            for (int i = 0; i < pdusArr.length; i++) {

                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusArr[i]);
                String senderNum = currentMessage.getDisplayOriginatingAddress();
                String message = currentMessage.getDisplayMessageBody().toLowerCase();
                Log.i(TAG, "senderNum: " + senderNum + " message: " + message);
                if (message.contains("otp") || message.contains("verification") || message.contains("code")) {

                    Intent service = new Intent(context, OtpWakefulService.class);
                    service.putExtra("message", message);

                    // Start the service, keeping the device awake while it is launching.
                    Log.i("SimpleWakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
                    startWakefulService(context, service);

                }
            }


        }


    }


    /**
     * Unbinds the sender string and listener for callback.
     */
    public static void unbind() {
        otpListener = null;
        receiverString = null;
    }
}
