package com.example.lifesync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {
    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final int NOTIFICATION_ID = 100;
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminders";
            String description = "Notifications for upcoming tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400});
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setShowBadge(true); // Allow app icon badges
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showTaskNotification(String taskTitle, String timeLeft) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return; // Or handle the case where permission isn't granted
        }
        // Create intent for when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "tasks");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.splash_icon)
                .setContentTitle("Task Due Today: " + taskTitle)
                .setContentText("at " + timeLeft)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(taskTitle + " is due in " + timeLeft + ". Don't forget to complete it!"));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Use a unique ID for each notification
        int notificationId = (int) System.currentTimeMillis();
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

        }
        notificationManager.notify(notificationId, builder.build());
    }
    public void getNotification()  {
        try{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);  // 11 PM (23 in 24-hour format)
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        String today = sdf.format(calendar.getTime());

        Date date1 = sdf.parse(today);

        FirebaseFirestore.getInstance().collection("Tasks")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .orderBy("taskStatus", Query.Direction.DESCENDING)
                .orderBy("taskDeadline", Query.Direction.ASCENDING)
                .orderBy("taskPriority", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                com.example.lifesync.TaskModel taskModel = document.toObject(com.example.lifesync.TaskModel.class);
                                taskModel.setTaskId(document.getId());
                                if (taskModel.getTaskStatus().equals("Pending")) {
                                    if(!taskModel.getTaskDeadline().isEmpty()) {
                                        Date date2 = null;
                                        try {
                                            date2 = sdf.parse(taskModel.getTaskDeadline());
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        if (date2.before(date1)) {
                                            Log.d("Notificationhehe", "task added");
                                            showTaskNotification(taskModel.getTaskName(), taskModel.getTaskDeadline());
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
