package com.example.moveit.view.account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moveit.R;
import com.example.moveit.view.HomeActivity;
import com.example.moveit.view.StartActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.login_title));

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);

        auth = FirebaseAuth.getInstance();

        setUpShowHideBtn();
        setUpLoginBtn();
        setUpForgotPasswordBtn();
    }

    private void setUpForgotPasswordBtn() {
        Button forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);
        forgotPasswordBtn.setOnClickListener(view -> {
            Intent intent = ForgotPasswordActivity.makeIntent(this);
            startActivity(intent);
        });
    }

    private void setUpShowHideBtn() {
        Button showHideBtn = findViewById(R.id.showHideBtn);
        showHideBtn.setOnClickListener(v -> {
            if(showHideBtn.getText().toString().equals("Show")){
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showHideBtn.setText(R.string.hide);
            } else{
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showHideBtn.setText(R.string.show);
            }
        });
    }

    private void setUpLoginBtn() {
        loginBtn.setOnClickListener(v -> {
            String emailText = emailInput.getText().toString();
            String passwordText = passwordInput.getText().toString();

            if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
                Toast.makeText(LoginActivity.this, "Email or Password is empty!", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(LoginActivity.this, "Email has invalid format!", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(emailText, passwordText);
            }
        });
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Log in successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Failed to log in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}