package com.example.moveit.view.account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.moveit.R;
import com.example.moveit.model.account.PasswordValidator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentPasswordInput;
    private EditText newPasswordInput;
    private Button resetButton;
    private FirebaseAuth auth;
    private PasswordValidator passwordValidator;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("Change Password");

        currentPasswordInput = findViewById(R.id.currentPassword);
        newPasswordInput = findViewById(R.id.newPassword);
        resetButton = findViewById(R.id.resetButton);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        passwordValidator = new PasswordValidator();

        FloatingActionButton fab = findViewById(R.id.passwordToolTip);
        TooltipCompat.setTooltipText(fab, getString(R.string.password_rules));

        setUpShowHideBtn();
        setUpResetButton();
    }

    private void setUpResetButton() {
        resetButton.setOnClickListener(view -> {
            String currentPasswordText = currentPasswordInput.getText().toString();
            String newPasswordText = newPasswordInput.getText().toString();

            if (TextUtils.isEmpty(currentPasswordText) || TextUtils.isEmpty(newPasswordText)) {
                Toast.makeText(ChangePasswordActivity.this, "Please fill in both inputs", Toast.LENGTH_SHORT).show();
            } else if (!passwordValidator.validatePassword(newPasswordText)) {
                Toast.makeText(ChangePasswordActivity.this, "New password is not strong enough!", Toast.LENGTH_SHORT).show();
            } else {
                auth.signInWithEmailAndPassword(Objects.requireNonNull(currentUser.getEmail()), currentPasswordText)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                currentUser.updatePassword(newPasswordText).addOnCompleteListener(task1 -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this, "Password successfully updated", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void setUpShowHideBtn() {
        Button currentShowHideBtn = findViewById(R.id.currentPasswordShowHideBtn);
        Button newShowHideBtn = findViewById(R.id.newPasswordShowHideBtn);
        currentShowHideBtn.setOnClickListener(v -> {
            if(currentShowHideBtn.getText().toString().equals("Show")){
                currentPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                currentShowHideBtn.setText(R.string.hide);
            } else{
                currentPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                currentShowHideBtn.setText(R.string.show);
            }
        });
        newShowHideBtn.setOnClickListener(v -> {
            if(newShowHideBtn.getText().toString().equals("Show")){
                newPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                newShowHideBtn.setText(R.string.hide);
            } else{
                newPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                newShowHideBtn.setText(R.string.show);
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, ChangePasswordActivity.class);
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