package com.example.moveit.view.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.moveit.R;
import com.example.moveit.view.PhotoGalleryActivity;
import com.example.moveit.view.SetThemeActivity;
import com.example.moveit.view.StartActivity;
import com.example.moveit.view.account.AccountSettingsActivity;
import com.example.moveit.view.activities.ActivitiesList;
import com.example.moveit.view.meals.MealList;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SettingsPage extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpButtons();
    }

    private void setUpButtons() {
        Button editMealsBtn = requireView().findViewById(R.id.editMealsBtn);
        Button editActivitiesBtn = requireView().findViewById(R.id.editActivitiesBtn);
        Button logoutBtn = requireView().findViewById(R.id.logout);
        Button editThemeBtn = requireView().findViewById(R.id.setThemeBtn);
        Button aboutBtn = requireView().findViewById(R.id.aboutBtn);
        Button photoGalleryBtn = requireView().findViewById(R.id.photoGalleryBtn);
        Button editReminderBtn = requireView().findViewById(R.id.editReminderBtn);
        Button accountSettingsBtn = requireView().findViewById(R.id.accountSettingsBtn);

        editActivitiesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ActivitiesList.class);
            startActivity(intent);
        });
        editMealsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MealList.class);
            startActivity(intent);
        });
        editThemeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SetThemeActivity.class);
            startActivity(intent);
        });
        accountSettingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
            startActivity(intent);
        });
        photoGalleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PhotoGalleryActivity.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getActivity(), "Log out successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), StartActivity.class);
            startActivity(intent);
            requireActivity().finish();
            requireActivity().overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
        });
    }
}