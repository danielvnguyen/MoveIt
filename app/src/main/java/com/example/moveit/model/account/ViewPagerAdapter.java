package com.example.moveit.model.account;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.moveit.view.homepage.CalendarPage;
import com.example.moveit.view.homepage.EntriesPage;
import com.example.moveit.view.homepage.SettingsPage;

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
