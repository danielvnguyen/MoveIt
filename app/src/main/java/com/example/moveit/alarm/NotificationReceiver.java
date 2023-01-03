package com.example.moveit.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.moveit.R;
import com.example.moveit.view.NotificationActivity;
import com.example.moveit.view.entries.AddEntry;

/*
    This class is needed for background processes
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String NOTIFICATION_CHANNEL_ID = "10001";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, AddEntry.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AddEntry.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        Notification notification = builder.setContentTitle("Daily Entry Reminder")
                .setContentText("How was your day?")
                .setSmallIcon(R.drawable.app_icon)
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
    }
}
