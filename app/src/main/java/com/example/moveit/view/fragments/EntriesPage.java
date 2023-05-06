package com.example.moveit.view.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.moveit.R;
import com.example.moveit.model.entries.Entry;
import com.example.moveit.model.entries.EntryComparator;
import com.example.moveit.model.entries.EntryListAdapter;
import com.example.moveit.view.entries.AddEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class EntriesPage extends Fragment {
    private FloatingActionButton addEntryBtn;
    private ArrayAdapter<Entry> adapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Calendar currentDate;
    private Calendar realDate;
    private Button nextMonthBtn;
    private Button previousMonthBtn;
    private TextView dateTextView;
    private Button resetDateBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entries_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        addEntryBtn = requireView().findViewById(R.id.addEntry);

        setUpAddEntryBtn();
        setUpEntryList();
        setUpDateAndPageBtns();
        setUpResetBtn();
    }

    private void setUpResetBtn() {
        resetDateBtn = requireView().findViewById(R.id.resetDateBtn);
        resetDateBtn.setOnClickListener(v -> {
            currentDate = Calendar.getInstance();
            setDateLabel(currentDate.getTimeInMillis());
            nextMonthBtn.setEnabled(false);
            resetDateBtn.setVisibility(View.GONE);
            setUpEntryList();
        });
    }

    private void setUpDateAndPageBtns() {
        nextMonthBtn = requireView().findViewById(R.id.nextMonthBtn);
        previousMonthBtn = requireView().findViewById(R.id.previousMonthBtn);
        dateTextView = requireView().findViewById(R.id.entryMonthTV);
        nextMonthBtn.setEnabled(false);

        currentDate = Calendar.getInstance(); // The current date that the user is on
        realDate = Calendar.getInstance(); // The real date in the world
        setDateLabel(currentDate.getTimeInMillis());

        nextMonthBtn.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, 1);
            setDateLabel(currentDate.getTimeInMillis());
            if (currentDate.get(Calendar.MONTH) < realDate.get((Calendar.MONTH))
                    || currentDate.get(Calendar.YEAR) < realDate.get((Calendar.YEAR))) {
                nextMonthBtn.setEnabled(true);
                resetDateBtn.setVisibility(View.VISIBLE);
            } else {
                nextMonthBtn.setEnabled(false);
                resetDateBtn.setVisibility(View.GONE);
            }

            setUpEntryList();
        });

        previousMonthBtn.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, -1);
            setDateLabel(currentDate.getTimeInMillis());
            if (currentDate.get(Calendar.MONTH) < realDate.get((Calendar.MONTH))
                    || currentDate.get(Calendar.YEAR) < realDate.get((Calendar.YEAR))) {
                nextMonthBtn.setEnabled(true);
                resetDateBtn.setVisibility(View.VISIBLE);
            } else {
                nextMonthBtn.setEnabled(false);
                resetDateBtn.setVisibility(View.GONE);
            }

            setUpEntryList();
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void setDateLabel(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM, yyyy");
        String dateText = (sdf.format(date));
        dateTextView.setText(dateText);
    }

    private void setUpEntryList() {
        ProgressDialog progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ListView entryListView = requireView().findViewById(R.id.entriesListView);
        entryListView.setEmptyView(requireView().findViewById(R.id.emptyTV));

        adapter = new EntryListAdapter(requireActivity(), R.layout.item_entry);
        entryListView.setAdapter(adapter);

        db.collection("entries").document(currentUser.getUid()).collection("entryList")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Entry> entryList = new ArrayList<>();
                        for (QueryDocumentSnapshot entryDoc : Objects.requireNonNull(task.getResult())) {
                            Entry currentEntry = entryDoc.toObject(Entry.class);
                            Calendar currentEntryDate = Calendar.getInstance();
                            currentEntryDate.setTimeInMillis(currentEntry.getDateTime());
                            //Only add current entry to entryList if within desired time period
                            if (currentDate.get(Calendar.MONTH) == currentEntryDate.get((Calendar.MONTH))
                            && currentDate.get(Calendar.YEAR) == currentEntryDate.get((Calendar.YEAR))) {
                                entryList.add(currentEntry);
                            }
                        }

                        adapter.clear();
                        entryList.sort(new EntryComparator());
                        adapter.addAll(entryList);
                        progressDialog.dismiss();
                    } else {
                        Log.d("EntriesPage", "Error retrieving documents: ", task.getException());
                    }
                });
    }

    private void setUpAddEntryBtn() {
        addEntryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEntry.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity().getIntent().getExtras() != null &&
                requireActivity().getIntent().getExtras().getBoolean("isChangedEntries")){
            setUpEntryList();
            requireActivity().getIntent().removeExtra("isChangedEntries");
        }
    }
}