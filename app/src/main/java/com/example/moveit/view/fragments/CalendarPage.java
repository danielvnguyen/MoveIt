package com.example.moveit.view.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import com.applandeo.materialcalendarview.CalendarView;

import com.applandeo.materialcalendarview.EventDay;
import com.example.moveit.R;
import com.example.moveit.model.theme.ThemeUtils;
import com.example.moveit.model.entries.Entry;
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

    private ArrayList<Calendar> entryDates;
    private ArrayList<Drawable> entryMoods;

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
        calendarView.setOnPreviousPageChangeListener(() -> {
            calendar.add(Calendar.MONTH, -1);
            setUpCalendar();
        });
        calendarView.setOnForwardPageChangeListener(() -> {
            calendar.add(Calendar.MONTH, 1);
            setUpCalendar();
        });

        setUpCalendar();
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("ResourceAsColor")
    private void setUpCalendar() {
        ProgressDialog progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        entryDates = new ArrayList<>();
        entryMoods = new ArrayList<>();

        db.collection("entries").document(currentUser.getUid()).collection("entryList")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot entryDoc : Objects.requireNonNull(task.getResult())) {
                            Entry currentEntry = entryDoc.toObject(Entry.class);
                            Calendar currentEntryDate = Calendar.getInstance();
                            currentEntryDate.setTimeInMillis(currentEntry.getDateTime());

                            if (currentEntryDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
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

                        ArrayList<EventDay> daysWithEntries = createEventDays();
                        calendarView.setEvents(daysWithEntries);
                        progressDialog.dismiss();
                    } else {
                        Log.d("CalendarPage", "Error retrieving documents: ", task.getException());
                    }
                });
    }

    //Detect multiple entries in the days of the current month and update accordingly
    private ArrayList<EventDay> createEventDays() {
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

                        Drawable drawable = createCountLabel(index, count);
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

    private Drawable createCountLabel(int i, int count) {
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
}