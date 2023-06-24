package com.example.moveit.view.account;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.example.moveit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import java.util.List;
import java.util.Objects;

public class AccountSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.account_settings_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_account_settings);

        setUpButtons();
    }

    private void setUpButtons() {
        Button changePasswordBtn = findViewById(R.id.changePasswordBtn);
        Button updateEmailBtn = findViewById(R.id.updateEmailBtn);
        Button deleteAccountBtn = findViewById(R.id.deleteAccountBtn);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;

        List<? extends UserInfo> providerData = currentUser.getProviderData();
        boolean isGoogleSignInOnly = false;
        for (int i = 1; i < providerData.size(); i++) {
            if (providerData.get(i).getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                isGoogleSignInOnly = true;
            } else {
                isGoogleSignInOnly = false;
                break;
            }
        }
        if (isGoogleSignInOnly) {
            changePasswordBtn.setVisibility(View.GONE);
            updateEmailBtn.setVisibility(View.GONE);
            deleteAccountBtn.setOnClickListener(view -> {
                Intent intent = DeleteAccountActivity.makeIntent(this);
                intent.putExtra("isGoogleSignInOnly", true);
                startActivity(intent);
            });
        } else {
            changePasswordBtn.setOnClickListener(view -> {
                Intent intent = ChangePasswordActivity.makeIntent(this);
                startActivity(intent);
            });
            updateEmailBtn.setOnClickListener(view -> {
                Intent intent = UpdateEmailActivity.makeIntent(this);
                startActivity(intent);
            });
            deleteAccountBtn.setOnClickListener(view -> {
                Intent intent = DeleteAccountActivity.makeIntent(this);
                startActivity(intent);
            });
        }
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
}