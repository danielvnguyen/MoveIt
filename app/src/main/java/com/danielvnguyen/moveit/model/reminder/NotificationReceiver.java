package com.danielvnguyen.moveit.model.reminder;

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
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.theme.ThemeSharedPreferences;
import com.danielvnguyen.moveit.view.entries.AddEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

/*
    This class is needed for background processes corresponding to notifications
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private final String[] reminderMessages = {
            "Reflect on your daily habits for a healthier, happier you! Share your wellness journey.",
            "Small changes lead to big results. Jot down your healthy choices and wins today!",
            "Nourish your mind, body, and soul. Document your healthy choices in your journal.",
            "Wellness is a daily commitment. Record how today's choices contribute to your health!",
            "Your wellness story matters. Reflect on today's healthy habits and discoveries.",
            "Every healthy choice counts. Share your journey toward a balanced lifestyle!",
            "Discover the power of mindfulness in your wellness journey. Reflect on today's steps.",
            "Celebrate progress, no matter how small. Share your healthy lifestyle wins!",
            "Building healthy habits one day at a time. What positive changes did you make today?",
            "Embrace a vibrant life through reflection. Record your steps toward a healthier you!"};

    @Override
    public void onReceive(Context context, Intent intent) {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        setUpTheme(context);

        Intent notificationIntent = new Intent(context, AddEntry.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AddEntry.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        Notification notification = builder.setContentTitle("Daily Entry Reminder")
                .setContentText(reminderMessages[new Random().nextInt(reminderMessages.length)])
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

    private void setUpTheme(Context context) {
        int currentTheme = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        ThemeSharedPreferences preferencesManager = new ThemeSharedPreferences(context);

        if (currentUser != null) {
            if (preferencesManager.getValue(currentUser.getUid() + ".currentTheme", "Dark").equals("Dark")){
                switch (currentTheme) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }
            } else if (preferencesManager.getValue(currentUser.getUid() + ".currentTheme", "Light").equals("Light")) {
                switch (currentTheme) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        break;
                }
            }
        }
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
