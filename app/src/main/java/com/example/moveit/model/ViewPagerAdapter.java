package com.example.moveit.model;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.moveit.view.fragments.CalendarPage;
import com.example.moveit.view.fragments.EntriesPage;
import com.example.moveit.view.fragments.SettingsPage;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new CalendarPage();
            case 2:
                return new SettingsPage();
            default:
                return new EntriesPage();
        }
    }


    @Override
    public int getItemCount() {
        return 3;
    }
}
