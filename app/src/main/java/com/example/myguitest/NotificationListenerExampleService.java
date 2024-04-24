package com.example.myguitest;

import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;


public class NotificationListenerExampleService extends NotificationListenerService {
    MediaPlayer mediaPlayer = null;
    boolean enabled = false;
    int batchTriggerAmount = 0;

    public class LocalBinder extends Binder {
        NotificationListenerExampleService getService() {
            return NotificationListenerExampleService.this;
        }
    }
    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {

    if (intent.getAction() != null) {
        return super.onBind(intent);
    }
     return binder;

     }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String logLine = "";
        String packageName = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;
        String title =  "";

        Log.i("myLogTag", "onNotificationPosted called!!");

        if (extras.containsKey("android.title")) {
            title = extras.getString("android.title");
        }

        // com.instacart.shopper // com.google.android.apps.googlevoice
        if (packageName.equals("com.instacart.shopper") && enabled && title.contains("available")) {
            if (extractAmountFromBatchString(title) > batchTriggerAmount)
                playNotification();
        }
    }
    public void setNotificationStatus(boolean on) {
        enabled = on;
    }
    public boolean getNotificationStatus() { return enabled; }

    public void setBatchTriggerAmount(int amt) { batchTriggerAmount = amt; }

    public void playNotification() {
        if ( mediaPlayer == null ) {
            mediaPlayer = MediaPlayer.create(this, R.raw.alert);
        }
        if ( !mediaPlayer.isPlaying() ) {
            mediaPlayer.start();
        }
    }
    
    private int extractAmountFromBatchString(String str) {
        int dollarSignIndex = str.indexOf("$") ;
        int dollarEndIndex = 0;
        
        // Sometimes dollar amount is not a decimal, so check for that..
        if (str.contains(".")) {
            dollarEndIndex = str.indexOf(".", dollarSignIndex);
        }  else {
            dollarEndIndex = str.indexOf(" ", dollarSignIndex);
        }
        String strDollarAmount = str.substring( dollarSignIndex + 1, dollarEndIndex );
        return Integer.valueOf(strDollarAmount);

    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Implement what you want here
    }

}
