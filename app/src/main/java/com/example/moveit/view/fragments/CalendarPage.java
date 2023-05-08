package com.example.moveit.view.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;

import com.applandeo.materialcalendarview.EventDay;
import com.example.moveit.R;
import com.example.moveit.model.theme.ThemeUtils;
import com.example.moveit.model.entries.Entry;
import com.example.moveit.view.entries.DayEntryList;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CalendarPage extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private CalendarView calendarView;
    private Calendar calendar;
    private Calendar realDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        calendar = Calendar.getInstance();
        realDate = Calendar.getInstance();
        calendarView = requireView().findViewById(R.id.calendarView);
        calendarView.setMaximumDate(realDate);
        calendarView.setOnPreviousPageChangeListener(this::handlePageChange);
        calendarView.setOnForwardPageChangeListener(this::handlePageChange);
        calendarView.setOnDayClickListener(eventDay -> {
            if (eventDay.getCalendar().getTimeInMillis() > realDate.getTimeInMillis()) {
                Toast.makeText(requireActivity(), "This date has yet to come", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(requireActivity(), DayEntryList.class);
                intent.putExtra("currentDate", eventDay.getCalendar().getTimeInMillis());
                startActivity(intent);
            }
        });

        setUpInterface();
    }

    private void handlePageChange() {
        calendar = calendarView.getCurrentPageDate();
        setUpInterface();
    }

    private void setUpMoodCount(ArrayList<Entry> entriesInMonth) {
        TextView amazingCountTV = requireView().findViewById(R.id.amazingMoodCountTV);
        TextView greatCountTV = requireView().findViewById(R.id.greatMoodCountTV);
        TextView goodCountTV = requireView().findViewById(R.id.goodMoodCountTV);
        TextView mehCountTV = requireView().findViewById(R.id.mehMoodCountTV);
        TextView badCountTV = requireView().findViewById(R.id.badMoodCountTV);

        int amazingCount, greatCount, goodCount, mehCount, badCount;
        amazingCount = greatCount = goodCount = mehCount = badCount = 0;

        if (!entriesInMonth.isEmpty()) {
            for (Entry entry : entriesInMonth) {
                switch (entry.getMood()) {
                    case "Amazing":
                        amazingCount += 1;
                        break;
                    case "Great":
                        greatCount += 1;
                        break;
                    case "Good":
                        goodCount += 1;
                        break;
                    case "Meh":
                        mehCount += 1;
                        break;
                    case "Bad":
                        badCount += 1;
                        break;
                }
            }
            amazingCountTV.setText(String.valueOf(amazingCount));
            greatCountTV.setText(String.valueOf(greatCount));
            goodCountTV.setText(String.valueOf(goodCount));
            mehCountTV.setText(String.valueOf(mehCount));
            badCountTV.setText(String.valueOf(badCount));

            setUpPieChart(amazingCount, greatCount, goodCount, mehCount, badCount);
        }
    }

    private void setUpPieChart(int amazingCount, int greatCount, int goodCount, int mehCount, int badCount) {
        PieChart pieChart = requireView().findViewById(R.id.pieChart);
        pieChart.setRotationAngle(180f);
        pieChart.setMaxAngle(180f);
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float)amazingCount/5, ""));
        entries.add(new PieEntry((float)greatCount/5, ""));
        entries.add(new PieEntry((float)goodCount/5, ""));
        entries.add(new PieEntry((float)mehCount/5, ""));
        entries.add(new PieEntry((float)badCount/5, ""));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ContextCompat.getColor(requireContext(), R.color.amazingColour), ContextCompat.getColor(requireContext(), R.color.greatColour),
                ContextCompat.getColor(requireContext(), R.color.goodColour), ContextCompat.getColor(requireContext(), R.color.mehColour),
                ContextCompat.getColor(requireContext(), R.color.badColour));
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        pieChart.setData(data);

        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setTransparentCircleRadius(1f);
        pieChart.setTouchEnabled(false);
        pieChart.getLegend().setEnabled(false);

        Typeface ralewayTypeface = getResources().getFont(R.font.raleway);
        pieChart.setCenterTextTypeface(ralewayTypeface);
        pieChart.setCenterText(String.valueOf(amazingCount + greatCount + goodCount + mehCount + badCount));
        pieChart.setCenterTextSize(35);
        pieChart.setCenterTextOffset(0, -25);
        pieChart.setCenterTextColor(ThemeUtils.getTextColor(requireContext()));
        pieChart.setHoleColor(Color.TRANSPARENT);

        pieChart.invalidate();
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("ResourceAsColor")
    private void setUpInterface() {
        ProgressDialog progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        ArrayList<Calendar> entryDates = new ArrayList<>();
        ArrayList<Drawable> entryMoods = new ArrayList<>();
        ArrayList<Entry> entriesInMonth = new ArrayList<>();

        db.collection("entries").document(currentUser.getUid()).collection("entryList")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot entryDoc : Objects.requireNonNull(task.getResult())) {
                            Entry currentEntry = entryDoc.toObject(Entry.class);
                            Calendar currentEntryDate = Calendar.getInstance();
                            currentEntryDate.setTimeInMillis(currentEntry.getDateTime());

                            if (currentEntryDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                    currentEntryDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                                entriesInMonth.add(currentEntry);
                                Drawable unwrappedDrawable;
                                Drawable wrappedDrawable = null;
                                switch (currentEntry.getMood()) {
                                    case "Bad":
                                        unwrappedDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.bad_icon);
                                        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), R.color.badColour));
                                        break;
                                    case "Meh":
                                        unwrappedDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.meh_icon);
                                        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), R.color.mehColour));
                                        break;
                                    case "Good":
                                        unwrappedDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.good_icon);
                                        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), R.color.goodColour));
                                        break;
                                    case "Great":
                                        unwrappedDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.great_icon);
                                        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), R.color.greatColour));
                                        break;
                                    case "Amazing":
                                        unwrappedDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.amazing_icon);
                                        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), R.color.amazingColour));
                                        break;
                                }
                                entryDates.add(currentEntryDate);
                                entryMoods.add(wrappedDrawable);
                            }
                        }

                        ArrayList<EventDay> daysWithEntries = createEventDays(entryDates, entryMoods);
                        calendarView.setEvents(daysWithEntries);
                        setUpMoodCount(entriesInMonth);
                        progressDialog.dismiss();
                    } else {
                        Log.d("CalendarPage", "Error retrieving documents: ", task.getException());
                    }
                });
    }

    //Detect multiple entries in the days of the current month and update accordingly
    private ArrayList<EventDay> createEventDays(ArrayList<Calendar> entryDates, ArrayList<Drawable> entryMoods) {
        ArrayList<EventDay> daysWithEntries = new ArrayList<>();
        HashMap<Integer, Integer> dayOfMonthCountMap = new HashMap<>();
        for (Calendar day : entryDates) {
            int dayOfMonth = day.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonthCountMap.containsKey(dayOfMonth)) {
                int count = dayOfMonthCountMap.get(dayOfMonth);
                dayOfMonthCountMap.put(dayOfMonth, count + 1);
            } else {
                dayOfMonthCountMap.put(dayOfMonth, 1);
            }
        }

        for (Map.Entry<Integer, Integer> entry : dayOfMonthCountMap.entrySet()) {
            int dayOfMonth = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < entryDates.size(); i++) {
                if (dayOfMonth == entryDates.get(i).get(Calendar.DAY_OF_MONTH)) {
                    if (count > 1) {
                        //Obtain most recent entry
                        ArrayList<Calendar> matchingEntries = new ArrayList<>();
                        for (Calendar date : entryDates) {
                            if (date.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                                matchingEntries.add(date);
                            }
                        }
                        Calendar latestEntry = Collections.max(matchingEntries);
                        int index = entryDates.indexOf(latestEntry);

                        Drawable drawable = createCountLabel(index, count, entryMoods);
                        EventDay day = new EventDay(entryDates.get(index), drawable);
                        daysWithEntries.add(day);
                    } else {
                        EventDay day = new EventDay(entryDates.get(i), entryMoods.get(i));
                        daysWithEntries.add(day);
                    }
                }
            }
        }
        return daysWithEntries;
    }

    private Drawable createCountLabel(int i, int count, ArrayList<Drawable> entryMoods) {
        Drawable moodDrawable = entryMoods.get(i);
        int newWidth = moodDrawable.getIntrinsicWidth() + 150;

        Bitmap bitmap = Bitmap.createBitmap(newWidth, moodDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        moodDrawable.setBounds(0, 0, moodDrawable.getIntrinsicWidth(), moodDrawable.getIntrinsicHeight());
        moodDrawable.draw(canvas);

        Paint paint = new Paint();
        paint.setColor(ThemeUtils.getTextColor(requireContext()));
        paint.setTextSize(100);
        canvas.drawText(("+"+(count-1)), canvas.getWidth() * 0.5f, canvas.getHeight() / 2f, paint);

        return new BitmapDrawable(getResources(), bitmap);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (calendarView.getCurrentPageDate().get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
            calendar = calendarView.getCurrentPageDate();
            setUpInterface();
        }

        if (requireActivity().getIntent().getExtras() != null &&
                requireActivity().getIntent().getExtras().getBoolean("isChangedCalendar")){
            setUpInterface();
            requireActivity().getIntent().removeExtra("isChangedCalendar");
        }
    }
}