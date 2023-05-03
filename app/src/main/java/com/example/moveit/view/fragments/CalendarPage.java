package com.example.moveit.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.applandeo.materialcalendarview.CalendarView;

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

        calendarView = requireView().findViewById(R.id.calendarView);

        setUpCalendar();
    }

    private void setUpCalendar() {
        Calendar calendar = Calendar.getInstance();
        ArrayList<Integer> daysWithEntries = getDaysWithEntriesForMonth(calendar);

//        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> setUpCalendar());
    }

    // Retrieve the entries for the current month
    private ArrayList<Integer> getDaysWithEntriesForMonth(Calendar calendar) {
        ArrayList<Integer> daysWithEntries = new ArrayList<>();

        db.collection("entries").document(currentUser.getUid()).collection("entryList")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot entryDoc : Objects.requireNonNull(task.getResult())) {
                            Entry currentEntry = entryDoc.toObject(Entry.class);
                            Calendar currentEntryDate = Calendar.getInstance();
                            currentEntryDate.setTimeInMillis(currentEntry.getDateTime());

                            if (currentEntryDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                                daysWithEntries.add(currentEntryDate.get(Calendar.DAY_OF_MONTH));
                            }
                        }
                    } else {
                        Log.d("CalendarPage", "Error retrieving documents: ", task.getException());
                    }
                });

        return daysWithEntries;
    }
}