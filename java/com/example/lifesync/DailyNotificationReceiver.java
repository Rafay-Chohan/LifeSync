package com.example.lifesync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DailyNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Check for tasks and show notification
        NotificationHelper noti=new NotificationHelper(context);
        noti.getNotification();
        Log.d("Notificationhehe","works");
        // Reschedule the alarm for next day (important for Android 6+)
        AlarmUtils.setDailyNotificationAlarm(context);
    }
}
