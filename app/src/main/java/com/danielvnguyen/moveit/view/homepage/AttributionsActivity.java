package com.danielvnguyen.moveit.view.homepage;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import com.danielvnguyen.moveit.R;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import java.util.Objects;

public class AttributionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attributions);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.attributions);

        setUpButtons();
    }

    private void setUpButtons() {
        Button licensesBtn = findViewById(R.id.licensesBtn);
        Button illustrationsBtn = findViewById(R.id.illustrationsBtn);
        licensesBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licenses));
        });
        illustrationsBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, IllustrationsActivity.class));
        });
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