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
import com.example.moveit.view.activities.ActivitiesList;
import com.example.moveit.view.meals.MealList;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsPage extends Fragment {

    private Button logoutBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logoutBtn = requireView().findViewById(R.id.logout);

        setUpLogOutBtn();
        setUpMealsBtn();
        setUpActivitiesBtn();
    }

    private void setUpMealsBtn() {
        Button editMealsBtn = requireView().findViewById(R.id.editMeals);
        editMealsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MealList.class);
            startActivity(intent);
        });
    }

    private void setUpActivitiesBtn() {
        Button editActivitiesBtn = requireView().findViewById(R.id.editActivities);
        editActivitiesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ActivitiesList.class);
            startActivity(intent);
        });
    }

    private void setUpLogOutBtn() {
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getActivity(), "Log Out Successful!", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        });
    }
}