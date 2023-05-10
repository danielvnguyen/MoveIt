package com.example.moveit.reminder;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.moveit.R;
import com.example.moveit.view.entries.AddEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/*
    This class is needed for background processes
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public void onReceive(Context context, Intent intent) {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        Intent notificationIntent = new Intent(context, AddEntry.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AddEntry.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        Notification notification = builder.setContentTitle("Daily Entry Reminder")
                .setContentText("How was your day?")
                .setSmallIcon(R.drawable.app_icon)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent).build();
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "MoveItNotification",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(0, notification);

        //Automatically create a new alarm 24H from now
        scheduleAlarm(context);
    }

    private void scheduleAlarm(Context context) {
        Calendar calendar = Calendar.getInstance();
        db.collection("reminders").document(currentUser.getUid())
                .collection("reminderTime").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).isEmpty()) {
                            calendar.set(Calendar.HOUR_OF_DAY, 20);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                        } else {
                            Reminder userSetTime = task.getResult().getDocuments().get(0).toObject(Reminder.class);
                            assert userSetTime != null;
                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(userSetTime.getReminderTime());
                            calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                            calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                            calendar.set(Calendar.SECOND, c.get(Calendar.SECOND));
                        }
                    }
                    //Check if a reminder has passed, instantiate a new one.
                    if (calendar.getTime().compareTo(new Date()) < 0) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    Intent intent = new Intent(context, NotificationReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

                    if (alarmManager != null) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                });
    }
}
