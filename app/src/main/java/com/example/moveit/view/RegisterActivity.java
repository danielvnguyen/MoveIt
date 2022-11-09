package com.example.moveit.view;

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
import com.example.moveit.model.PasswordValidator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private Button registerBtn;
    private FirebaseAuth auth;
    private PasswordValidator passwordValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        registerBtn = findViewById(R.id.register);

        auth = FirebaseAuth.getInstance();

        passwordValidator = new PasswordValidator();
        FloatingActionButton fab = findViewById(R.id.passwordToolTip);
        TooltipCompat.setTooltipText(fab, getString(R.string.password_rules));

        setUpRegisterBtn();
        setUpShowHideBtn();
    }

    private void setUpRegisterBtn() {
        registerBtn.setOnClickListener(view -> {
            String emailText = emailInput.getText().toString();
            String passwordText = passwordInput.getText().toString();

            if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
                Toast.makeText(RegisterActivity.this, "Email or Password is empty", Toast.LENGTH_SHORT).show();
            } else if (!passwordValidator.validatePassword(passwordText)) {
                Toast.makeText(RegisterActivity.this, "Password is not strong enough!", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(emailText, passwordText);
            }
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

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, RegisterActivity.class);
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