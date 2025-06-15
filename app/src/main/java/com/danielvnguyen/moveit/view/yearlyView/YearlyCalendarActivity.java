package com.danielvnguyen.moveit.view.yearlyView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.entries.Entry;
import com.danielvnguyen.moveit.model.theme.ThemeUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
    private PieChart yearlyPieChart;
    private int selectedYear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yearly_calendar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.yearly_view));

        yearGrid = findViewById(R.id.year_grid);
        yearlyPieChart = findViewById(R.id.yearlyPieChart);
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

        TextView amazingCountTV = this.findViewById(R.id.amazingMoodCountTV);
        TextView greatCountTV = this.findViewById(R.id.greatMoodCountTV);
        TextView goodCountTV = this.findViewById(R.id.goodMoodCountTV);
        TextView mehCountTV = this.findViewById(R.id.mehMoodCountTV);
        TextView badCountTV = this.findViewById(R.id.badMoodCountTV);

        db.collection("entries")
                .document(currentUser.getUid())
                .collection("entryList")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<Integer, List<Entry>> entriesByMonth = new HashMap<>();

                        // Mood counters
                        int amazingCount = 0, greatCount = 0, goodCount = 0, mehCount = 0, badCount = 0;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Entry entry = doc.toObject(Entry.class);
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(entry.getDateTime());
                            int entryYear = cal.get(Calendar.YEAR);
                            int month = cal.get(Calendar.MONTH);

                            if (entryYear == year) {
                                entriesByMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(entry);

                                switch (entry.getMood()) {
                                    case "Amazing":
                                        amazingCount++;
                                        break;
                                    case "Great":
                                        greatCount++;
                                        break;
                                    case "Good":
                                        goodCount++;
                                        break;
                                    case "Meh":
                                        mehCount++;
                                        break;
                                    case "Bad":
                                        badCount++;
                                        break;
                                }
                            }
                        }

                        for (int month = 0; month < 12; month++) {
                            List<Entry> monthEntries = entriesByMonth.getOrDefault(month, new ArrayList<>());
                            View monthView = createMiniMonthView(year, month, monthEntries);
                            yearGrid.addView(monthView);
                        }

                        amazingCountTV.setText(String.valueOf(amazingCount));
                        greatCountTV.setText(String.valueOf(greatCount));
                        goodCountTV.setText(String.valueOf(goodCount));
                        mehCountTV.setText(String.valueOf(mehCount));
                        badCountTV.setText(String.valueOf(badCount));
                        setUpPieChart(amazingCount, greatCount, goodCount, mehCount, badCount);
                    }
                });
    }

    private void setUpPieChart(int amazingCount, int greatCount, int goodCount, int mehCount, int badCount) {
        yearlyPieChart.setRotationAngle(180f);
        yearlyPieChart.setMaxAngle(180f);

        ArrayList<PieEntry> entries = new ArrayList<>();
        int total = amazingCount + greatCount + goodCount + mehCount + badCount;
        if (total == 0) {
            yearlyPieChart.clear();
            yearlyPieChart.setCenterText("No Data");
            yearlyPieChart.invalidate();
            return;
        }

        entries.add(new PieEntry((float) amazingCount / total, ""));
        entries.add(new PieEntry((float) greatCount / total, ""));
        entries.add(new PieEntry((float) goodCount / total, ""));
        entries.add(new PieEntry((float) mehCount / total, ""));
        entries.add(new PieEntry((float) badCount / total, ""));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                ContextCompat.getColor(this, R.color.amazingColour),
                ContextCompat.getColor(this, R.color.greatColour),
                ContextCompat.getColor(this, R.color.goodColour),
                ContextCompat.getColor(this, R.color.mehColour),
                ContextCompat.getColor(this, R.color.badColour)
        );
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        yearlyPieChart.setData(data);

        yearlyPieChart.getDescription().setEnabled(false);
        yearlyPieChart.setRotationEnabled(false);
        yearlyPieChart.setDrawEntryLabels(false);
        yearlyPieChart.setTransparentCircleRadius(1f);
        yearlyPieChart.setTouchEnabled(false);
        yearlyPieChart.getLegend().setEnabled(false);

        Typeface ralewayTypeface = getResources().getFont(R.font.raleway);
        yearlyPieChart.setCenterTextTypeface(ralewayTypeface);
        yearlyPieChart.setCenterText(String.valueOf(total));
        yearlyPieChart.setCenterTextSize(35);
        yearlyPieChart.setCenterTextOffset(0, -25);
        yearlyPieChart.setCenterTextColor(ThemeUtils.getTextColor(this));
        yearlyPieChart.setHoleColor(android.graphics.Color.TRANSPARENT);

        yearlyPieChart.invalidate();
    }

    private View createMiniMonthView(int year, int month, List<Entry> entries) {
        Context context = this;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(8, 8, 8, 8);

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
        monthGrid.setRowCount(7);

        // Add header row with day initials
        String[] dayInitials = {"S", "M", "T", "W", "T", "F", "S"};
        for (String day : dayInitials) {
            TextView dayHeader = new TextView(context);
            dayHeader.setText(day);
            dayHeader.setTextSize(12);
            dayHeader.setGravity(Gravity.CENTER);
            dayHeader.setPadding(4, 4, 4, 4);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.height = dpToPx(context, 24);
            dayHeader.setLayoutParams(lp);

            monthGrid.addView(dayHeader);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int totalCells = 0;

        for (int i = 1; i < firstDayOfWeek; i++) {
            monthGrid.addView(createFixedDayCell(context, "", false));
            totalCells++;
        }

        for (int day = 1; day <= daysInMonth; day++) {
            List<Entry> todaysEntries = new ArrayList<>();
            for (Entry entry : entries) {
                Calendar entryDate = Calendar.getInstance();
                entryDate.setTimeInMillis(entry.getDateTime());
                if (entryDate.get(Calendar.DAY_OF_MONTH) == day) {
                    todaysEntries.add(entry);
                }
            }

            boolean hasEntry = !todaysEntries.isEmpty();
            Drawable moodIcon = null;

            if (hasEntry) {
                todaysEntries.sort((a, b) -> Long.compare(b.getDateTime(), a.getDateTime()));
                moodIcon = getMoodIconDrawable(todaysEntries.get(0).getMood());

                if (todaysEntries.size() > 1 && moodIcon != null) {
                    moodIcon = createCountLabel(moodIcon, todaysEntries.size());
                }
            }

            monthGrid.addView(createFixedDayCell(context, String.valueOf(day), hasEntry, moodIcon));
            totalCells++;
        }

        while (totalCells < 42) {
            monthGrid.addView(createFixedDayCell(context, "", false));
            totalCells++;
        }

        layout.addView(monthGrid);
        return layout;
    }

    private View createFixedDayCell(Context context, String text, boolean hasEntry) {
        return createFixedDayCell(context, text, hasEntry, null);
    }

    private View createFixedDayCell(Context context, String text, boolean hasEntry, @Nullable Drawable moodIcon) {
        LinearLayout cellLayout = new LinearLayout(context);
        cellLayout.setOrientation(LinearLayout.VERTICAL);
        cellLayout.setGravity(Gravity.CENTER);
        cellLayout.setPadding(4, 4, 4, 4);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.height = dpToPx(context, 48);
        cellLayout.setLayoutParams(lp);

        TextView dayNumber = new TextView(context);
        dayNumber.setText(text);
        dayNumber.setTextSize(12);
        dayNumber.setGravity(Gravity.CENTER);
        cellLayout.addView(dayNumber);

        ImageView moodView = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(context, 16), dpToPx(context, 16));
        iconParams.topMargin = dpToPx(context, 2);
        moodView.setLayoutParams(iconParams);

        if (hasEntry && moodIcon != null) {
            moodView.setImageDrawable(moodIcon);
            moodView.setVisibility(View.VISIBLE);
        } else {
            moodView.setVisibility(View.INVISIBLE);
        }
        cellLayout.addView(moodView);

        return cellLayout;
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

    private Drawable createCountLabel(Drawable moodDrawable, int count) {
        int newWidth = moodDrawable.getIntrinsicWidth() + 150;

        Bitmap bitmap = Bitmap.createBitmap(newWidth, moodDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        moodDrawable.setBounds(0, 0, moodDrawable.getIntrinsicWidth(), moodDrawable.getIntrinsicHeight());
        moodDrawable.draw(canvas);

        Paint paint = new Paint();
        paint.setColor(ThemeUtils.getTextColor(this));
        paint.setTextSize(100);
        canvas.drawText(("+" + (count - 1)), canvas.getWidth() * 0.5f, canvas.getHeight() / 2f, paint);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
