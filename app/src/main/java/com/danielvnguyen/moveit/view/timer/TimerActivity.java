package com.danielvnguyen.moveit.view.timer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.view.reminder.ReminderActivity;
import com.google.android.material.tabs.TabLayout;
import java.util.Objects;

public class TimerActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    TimerViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.timer_btn));

        tabLayout = findViewById(R.id.timerTabs);
        viewPager = findViewById(R.id.timer_view_pager);
        viewPagerAdapter = new TimerViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.addTab(tabLayout.newTab().setText("Timer"));
        tabLayout.addTab(tabLayout.newTab().setText("Stopwatch"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                super.onPageSelected(position);
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, ReminderActivity.class);
    }
}
