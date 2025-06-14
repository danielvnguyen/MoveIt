package com.danielvnguyen.moveit.view.yearlyView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.entries.Entry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class YearlyCalendarActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private GridLayout yearGrid;
    private Spinner yearSpinner;
    private int selectedYear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yearly_calendar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.yearly_view));

        yearGrid = findViewById(R.id.year_grid);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        assert currentUser != null;

        yearSpinner = findViewById(R.id.year_spinner);
        setupYearSpinner();
    }

    private void setupYearSpinner() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= 2000; y--) {
            years.add(String.valueOf(y));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(years.get(position));
                loadYearlyEntries(selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        selectedYear = currentYear;
    }

    private void loadYearlyEntries(int year) {
        yearGrid.removeAllViews();

        db.collection("entries")
                .document(currentUser.getUid())
                .collection("entryList")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<Integer, List<Entry>> entriesByMonth = new HashMap<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Entry entry = doc.toObject(Entry.class);
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(entry.getDateTime());
                            int entryYear = cal.get(Calendar.YEAR);
                            int month = cal.get(Calendar.MONTH);

                            if (entryYear == year) {
                                entriesByMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(entry);
                            }
                        }

                        for (int month = 0; month < 12; month++) {
                            List<Entry> monthEntries = entriesByMonth.getOrDefault(month, new ArrayList<>());
                            View monthView = createMiniMonthView(month, monthEntries);
                            yearGrid.addView(monthView);
                        }
                    }
                });
    }

    private View createMiniMonthView(int month, List<Entry> entries) {
        Context context = this;

        // Root layout for a month cell
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(8, 8, 8, 8);

        // Layout params to ensure grid cell fits screen evenly
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        layout.setLayoutParams(params);

        TextView title = new TextView(context);
        title.setText(new DateFormatSymbols().getMonths()[month]);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(16);
        layout.addView(title);

        GridLayout monthGrid = new GridLayout(context);
        monthGrid.setColumnCount(7);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Empty spaces before the first day
        for (int i = 1; i < firstDayOfWeek; i++) {
            TextView empty = new TextView(context);
            empty.setText(" ");
            monthGrid.addView(empty);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            TextView dayView = new TextView(context);
            dayView.setText(String.valueOf(day));
            dayView.setTextSize(12);
            dayView.setPadding(2, 2, 2, 2);
            dayView.setGravity(Gravity.CENTER);

            for (Entry entry : entries) {
                Calendar entryDate = Calendar.getInstance();
                entryDate.setTimeInMillis(entry.getDateTime());
                if (entryDate.get(Calendar.DAY_OF_MONTH) == day) {
                    Drawable moodIcon = getMoodIconDrawable(entry.getMood());
                    moodIcon.setBounds(0, 0, 48, 48);
                    dayView.setCompoundDrawables(null, moodIcon, null, null);
                    break;
                }
            }

            monthGrid.addView(dayView);
        }

        layout.addView(monthGrid);
        return layout;
    }

    private Drawable getMoodIconDrawable(String mood) {
        Drawable unwrappedDrawable;
        Drawable wrappedDrawable = null;
        switch (mood) {
            case "Bad":
                unwrappedDrawable = AppCompatResources.getDrawable(this, R.drawable.bad_icon);
                wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.badColour));
                break;
            case "Meh":
                unwrappedDrawable = AppCompatResources.getDrawable(this, R.drawable.meh_icon);
                wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.mehColour));
                break;
            case "Good":
                unwrappedDrawable = AppCompatResources.getDrawable(this, R.drawable.good_icon);
                wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.goodColour));
                break;
            case "Great":
                unwrappedDrawable = AppCompatResources.getDrawable(this, R.drawable.great_icon);
                wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.greatColour));
                break;
            case "Amazing":
                unwrappedDrawable = AppCompatResources.getDrawable(this, R.drawable.amazing_icon);
                wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.amazingColour));
                break;
        }
        return wrappedDrawable;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, YearlyCalendarActivity.class);
    }
}
