package com.delta.pragyanoc;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rb on 12/1/16.
 */
public class GCMNotificationIntentService extends IntentService {
    // Sets an ID for the notification, so it can be updated
    public static final int notifyID = 9001;
    NotificationCompat.Builder builder;

    public GCMNotificationIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCMNotificationIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                Log.d(Utilities.LOGGING,"Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                Log.d(Utilities.LOGGING,"Deleted messages on server: "
                        + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {
                Log.d(Utilities.LOGGING,"GCM : "+extras.toString());
                Log.d(Utilities.LOGGING, "" + extras.get("message")); //When Message is received normally from GCM Cloud Server

                String message = extras.getString("message");
                parseMessage(message);

            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void parseMessage(String message) {
        try {
            JSONObject messages = new JSONObject(message);
            String type = messages.getString("type");
            JSONObject msg = messages.getJSONObject("message");
            String taskName;
            try {
                taskName = msg.getString("task_name");
            }catch (Exception e) {
                taskName = "";
            }
            sendNotification(type,taskName);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String type, String taskName) {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(type.toUpperCase())
                .setContentText(taskName)
                .setSmallIcon(R.drawable.ic_plusone_standard_off_client);
        // Set pending intent
        mNotifyBuilder.setContentIntent(resultPendingIntent);
        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        mNotifyBuilder.setDefaults(defaults);
        // Set the content for Notification
        mNotifyBuilder.setContentText(taskName);
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        // Post a notification
        mNotificationManager.notify(notifyID, mNotifyBuilder.build());
    }
}