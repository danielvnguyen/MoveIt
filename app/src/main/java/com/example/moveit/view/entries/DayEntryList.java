package com.example.moveit.view.entries;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.moveit.R;
import com.example.moveit.model.entries.Entry;
import com.example.moveit.model.entries.EntryComparator;
import com.example.moveit.model.entries.EntryListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class DayEntryList extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Calendar currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_entry_list);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (getIntent().getExtras() != null) {
            currentDate = Calendar.getInstance();
            currentDate.setTimeInMillis(getIntent().getExtras().getLong("currentDate"));
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
            setTitle(sdf.format(currentDate.getTimeInMillis()));
        }

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        setUpList();
    }

    private void setUpList() {
        ArrayList<Entry> entriesInDay = new ArrayList<>();
        ListView daysInEntriesLV = findViewById(R.id.entriesInDayLV);

        db.collection("entries").document(currentUser.getUid()).collection("entryList")
            .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot entryDoc : Objects.requireNonNull(task.getResult())) {
                        Entry currentEntry = entryDoc.toObject(Entry.class);
                        Calendar currentEntryDate = Calendar.getInstance();
                        currentEntryDate.setTimeInMillis(currentEntry.getDateTime());

                        if (currentEntryDate.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH) &&
                                currentEntryDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                                currentEntryDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)) {
                            entriesInDay.add(currentEntry);
                        }
                    }
                    ArrayAdapter<Entry> adapter = new EntryListAdapter(this, R.layout.item_entry);
                    daysInEntriesLV.setAdapter(adapter);
                    adapter.clear();
                    entriesInDay.sort(new EntryComparator());
                    adapter.addAll(entriesInDay);
                    daysInEntriesLV.requestLayout();
                }
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

    public static Intent makeIntent(Context context) {
        return new Intent(context, DayEntryList.class);
    }
}