package com.danielvnguyen.moveit.view.homepage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.reminder.NotificationReceiver;
import com.danielvnguyen.moveit.model.account.ViewPagerAdapter;
import com.danielvnguyen.moveit.model.reminder.Reminder;
import com.danielvnguyen.moveit.view.account.IntroSlider;
import com.danielvnguyen.moveit.view.reminder.ReminderActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter viewPagerAdapter;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        tabLayout = findViewById(R.id.homeTabs);
        viewPager = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
            }
        });

        //Show introduction slide show if new user
        boolean seenSlideshow = getSharedPreferences("PREFS", MODE_PRIVATE).getBoolean(currentUser.getUid() + ".seenSlideshow", false);
        if (!seenSlideshow) {
            Intent i = new Intent(getApplicationContext(), IntroSlider.class);
            startActivity(i);
            getSharedPreferences("PREFS", MODE_PRIVATE).edit().putBoolean(currentUser.getUid()+".seenSlideshow", true).apply();
        }

        setUpAlarm();
    }

    private void setUpAlarm() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Redirect to system settings to ask user for permission
                Toast.makeText(HomeActivity.this, "Please allow permission for MoveIt!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

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

                        if (calendar.getTime().compareTo(new Date()) < 0) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                        if (alarmManager != null) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}