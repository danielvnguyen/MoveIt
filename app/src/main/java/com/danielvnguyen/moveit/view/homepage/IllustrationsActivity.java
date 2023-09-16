package com.danielvnguyen.moveit.view.homepage;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danielvnguyen.moveit.R;
import java.util.Objects;

public class IllustrationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illustrations);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.illustration_attributions);

        setUpList();
    }

    private void setUpList() {
        String[] attributions = {"UIcons by https://www.flaticon.com/uicons - Flaticon", "Emoji icons created by Smartline - Flaticon",
        "Folder icons created by Freepik - Flaticon", "Run icons created by Vitaly Gorbachev - Flaticon",
        "Camera icons created by Freepik - Flaticon", "Asterisk icons created by Freepik - Flaticon",
        "Email icons created by Bartama Graphic - Flaticon", "Mode icons created by GOFOX - Flaticon",
        "Dark icons created by adriansyah - Flaticon", "Gears icons created by Freepik - Flaticon",
        "User icons created by Phoenix Group - Flaticon", "Error icons created by Gregor Cresnar - Flaticon",
        "Key icons created by Freepik - Flaticon", "Star icons created by Pixel perfect - Flaticon",
        "Logout icons created by Pixel perfect - Flaticon", "Login icons created by Pixel perfect - Flaticon",
        "Guide icons created by Pixel perfect - Flaticon", "Stock images from Pixabay"};
        LinearLayout layout = findViewById(R.id.illustrationsTVLayout);

        for (String attributionText : attributions) {
            TextView textView = new TextView(this);
            textView.setText(attributionText);
            textView.setTextSize(16);
            layout.addView(textView);
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