package com.example.moveit.model;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moveit.R;
import com.example.moveit.model.theme.ThemeUtils;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class IntroSlider extends AppIntro {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSystemBackButtonLocked(true);

        addSlide(AppIntroFragment.createInstance("Welcome!", getString(R.string.slide_one_text),
                R.drawable.app_icon, R.color.dark_gray, ThemeUtils.getTextColor(getApplicationContext()),
                ThemeUtils.getTextColor(getApplicationContext()), R.font.raleway, R.font.raleway));
        addSlide(AppIntroFragment.createInstance("Creating entries", getString(R.string.slide_two_text),
                R.drawable.app_icon, R.color.dark_gray, ThemeUtils.getTextColor(getApplicationContext()),
                ThemeUtils.getTextColor(getApplicationContext()), R.font.raleway, R.font.raleway));
        addSlide(AppIntroFragment.createInstance("Viewing your progress", getString(R.string.slide_three_text),
                R.drawable.app_icon, R.color.dark_gray, ThemeUtils.getTextColor(getApplicationContext()),
                ThemeUtils.getTextColor(getApplicationContext()), R.font.raleway, R.font.raleway));
        addSlide(AppIntroFragment.createInstance("Settings", getString(R.string.slide_four_text),
                R.drawable.app_icon, R.color.dark_gray, ThemeUtils.getTextColor(getApplicationContext()),
                ThemeUtils.getTextColor(getApplicationContext()), R.font.raleway, R.font.raleway));
        addSlide(AppIntroFragment.createInstance("Thanks for reading!", getString(R.string.slide_five_text),
                R.drawable.app_icon, R.color.dark_gray, ThemeUtils.getTextColor(getApplicationContext()),
                ThemeUtils.getTextColor(getApplicationContext()), R.font.raleway, R.font.raleway));
    }

    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}
