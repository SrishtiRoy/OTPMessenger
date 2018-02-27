package com.srishti.otpmessenger.servcies;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.srishti.otpmessenger.MainActivity;
import com.srishti.otpmessenger.R;
import com.srishti.otpmessenger.receivers.OtpReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by srishtic on 2/13/18.
 */

public class OtpWakefulService extends IntentService {
    private static final Pattern UNWANTED_SYMBOLS =
            Pattern.compile("(?:--|[\\[\\]{}()+/\\\\],.;)");

    public OtpWakefulService() {
        super("OtpWakefulService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String message = intent.getExtras().getString("message");

        String otp = getverificationCode(message);
        if (!TextUtils.isEmpty(otp)) {

            sendNotification(this, otp);
            setClipboard(this, otp);
        }

        OtpReader.completeWakefulIntent(intent);

    }

    private void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void sendNotification(Context context, String otp) {


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher) // notification icon
                .setContentTitle("CODE is copied to clipboard:") // title for notification
                .setContentText(otp).setColor(getResources().getColor(R.color.colorAccent)) // message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.putExtra("otp", otp);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }


    private String getverificationCode(String message) {


        String[] arrOfStr = message.trim().split(" ");
        String regex = "\\d+";
        for (String a : arrOfStr) {
            if (a.length() > 2) {

                char exp = a.charAt(a.length() - 1);
                if (exp == '.'||exp == ','||exp == ';') {
                    String substring=a.substring(0,a.length() - 1);

                    if ((substring.length()>3 &&substring.length() < 8) && substring.matches(regex)) {

                        return substring;

                    }


                }
            }
            if ((a.length()>3 && a.length() < 7) && a.matches(regex)) {

                return a;

            }

        }
        return "";
    }

    private String stripNonDigits(
            final CharSequence input /* inspired by seh's comment */) {
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c > 47 && c < 58) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
