package com.danielvnguyen.moveit.view.timer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TimerViewPagerAdapter extends FragmentStateAdapter {
    public TimerViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new TimerFragment();
        } else {
            return new StopwatchFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
