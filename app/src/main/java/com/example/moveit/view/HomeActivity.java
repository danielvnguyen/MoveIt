package com.example.moveit.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import com.example.moveit.R;
import com.example.moveit.model.IntroSlider;
import com.example.moveit.reminder.NotificationReceiver;
import com.example.moveit.model.ViewPagerAdapter;
import com.example.moveit.reminder.Reminder;
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

    private boolean seenSlideshow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        seenSlideshow = getSharedPreferences("PREFS", MODE_PRIVATE).getBoolean("seenSlideshow", false);

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

        setUpAlarm();

        //Show introduction slide show if new user
        if (!seenSlideshow && (Objects.requireNonNull(currentUser.getMetadata()).getCreationTimestamp() == currentUser.getMetadata().getLastSignInTimestamp())) {
            Intent i = new Intent(getApplicationContext(), IntroSlider.class);
            startActivity(i);

            getSharedPreferences("PREFS", MODE_PRIVATE).edit().putBoolean("seenSlideshow", true).apply();
        }
    }

    private void setUpAlarm() {
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
                        //Check if a reminder has passed, instantiate a new one.
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