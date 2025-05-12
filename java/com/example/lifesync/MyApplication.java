package com.example.lifesync;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channel
        new NotificationHelper(this).createNotificationChannel();
        // Schedule daily notification at 4 PM
        AlarmUtils.setDailyNotificationAlarm(this);
        Log.d("MyApp","works");
        registerBootReceiver();
    }
    private void registerBootReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlarmUtils.setDailyNotificationAlarm(context);
            }
        }, filter);
    }
}
