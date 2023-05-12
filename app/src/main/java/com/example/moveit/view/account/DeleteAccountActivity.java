package com.example.moveit.view.account;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.util.Objects;

public class DeleteAccountActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.delete_account));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        assert currentUser != null;
        passwordInput = findViewById(R.id.passwordInput);

        setUpDeleteBtn();
        setUpShowHideBtn();
    }

    private void setUpDeleteBtn() {
        Button deleteBtn = findViewById(R.id.deleteButton);
        deleteBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.confirm_delete_account);
            builder.setMessage(R.string.no_takesies_backsies);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> handleDeleteAccount());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void handleDeleteAccount() {
        String passwordText = passwordInput.getText().toString();
        if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(DeleteAccountActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else {
            auth.signInWithEmailAndPassword(Objects.requireNonNull(currentUser.getEmail()), passwordText).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    deleteUserData();
                } else {
                    Toast.makeText(DeleteAccountActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteUserData() {
    }

    private void setUpShowHideBtn() {
        Button currentShowHideBtn = findViewById(R.id.passwordShowHideBtn);
        currentShowHideBtn.setOnClickListener(v -> {
            if(currentShowHideBtn.getText().toString().equals("Show")){
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                currentShowHideBtn.setText(R.string.hide);
            } else{
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                currentShowHideBtn.setText(R.string.show);
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, DeleteAccountActivity.class);
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