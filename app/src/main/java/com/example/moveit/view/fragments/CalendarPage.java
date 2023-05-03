package com.example.moveit.view.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import com.example.moveit.model.entries.Entry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
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
        ArrayList<EventDay> daysWithEntries = new ArrayList<>();

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

                                EventDay currentDay = new EventDay(currentEntryDate, wrappedDrawable);
                                daysWithEntries.add(currentDay);
                            }
                        }
                        calendarView.setEvents(daysWithEntries);
                        progressDialog.dismiss();
                    } else {
                        Log.d("CalendarPage", "Error retrieving documents: ", task.getException());
                    }
                });
    }
}