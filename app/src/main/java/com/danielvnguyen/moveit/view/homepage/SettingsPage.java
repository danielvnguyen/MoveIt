package com.danielvnguyen.moveit.view.homepage;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.view.account.IntroSlider;
import com.danielvnguyen.moveit.view.categories.CategoriesList;
import com.danielvnguyen.moveit.view.photoGallery.PhotoGalleryActivity;
import com.danielvnguyen.moveit.view.account.SetThemeActivity;
import com.danielvnguyen.moveit.view.account.StartActivity;
import com.danielvnguyen.moveit.view.account.AccountSettingsActivity;
import com.danielvnguyen.moveit.view.meals.MealList;
import com.danielvnguyen.moveit.view.reminder.ReminderActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsPage extends Fragment {
    private static final String ID_TOKEN = "446715183529-ucspush1pj4sqs89s71ipeeoooq476e5.apps.googleusercontent.com";

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
        Button attributionsBtn = requireView().findViewById(R.id.aboutBtn);
        Button photoGalleryBtn = requireView().findViewById(R.id.photoGalleryBtn);
        Button editReminderBtn = requireView().findViewById(R.id.editReminderBtn);
        Button accountSettingsBtn = requireView().findViewById(R.id.accountSettingsBtn);
        Button seeGuideBtn = requireView().findViewById(R.id.seeGuideBtn);

        editActivitiesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CategoriesList.class);
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
        attributionsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AttributionsActivity.class);
            startActivity(intent);
        });
        editReminderBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ReminderActivity.class);
            startActivity(intent);
        });
        seeGuideBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), IntroSlider.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setCancelable(true);
            builder.setTitle(R.string.confirm_logout);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> handleLogout());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ID_TOKEN)
                .requestEmail()
                .build();
        GoogleSignInClient signInClient = GoogleSignIn.getClient(requireActivity(), signInOptions);
        signInClient.signOut();

        Toast.makeText(getActivity(), "Log out successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), StartActivity.class);
        startActivity(intent);
        requireActivity().finish();
        requireActivity().overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
    }
}