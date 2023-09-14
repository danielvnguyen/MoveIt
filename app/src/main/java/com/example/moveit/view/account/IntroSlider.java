package com.example.moveit.view.account;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.example.moveit.R;
import com.example.moveit.model.theme.ThemeUtils;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class IntroSlider extends AppIntro {

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSystemBackButtonLocked(true);
        setNavBarColor(getResources().getColor(R.color.darkest_green));

        int backgroundColour = R.color.light_green;
        int textColour = ThemeUtils.getTextColor(getApplicationContext());
        int fontFamily = R.font.raleway_bold;

        addSlide(AppIntroFragment.createInstance("Welcome!", getString(R.string.slide_one_text),
                R.drawable.app_icon, backgroundColour, textColour, textColour, fontFamily, fontFamily));
        addSlide(AppIntroFragment.createInstance("Creating entries", getString(R.string.slide_two_text),
                R.drawable.slide_2, backgroundColour, textColour, textColour, fontFamily, fontFamily));
        addSlide(AppIntroFragment.createInstance("Viewing your progress", getString(R.string.slide_three_text),
                R.drawable.slide_3, backgroundColour, textColour, textColour, fontFamily, fontFamily));
        addSlide(AppIntroFragment.createInstance("Settings", getString(R.string.slide_four_text),
                R.drawable.slide_4, backgroundColour, textColour, textColour, fontFamily, fontFamily));
        addSlide(AppIntroFragment.createInstance("Thanks for reading!", getString(R.string.slide_five_text),
                R.drawable.amazing_icon_slide, backgroundColour, textColour, textColour, fontFamily, fontFamily));

        String[] permissions = {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS};
        askForPermissions(permissions, 1, false);
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
