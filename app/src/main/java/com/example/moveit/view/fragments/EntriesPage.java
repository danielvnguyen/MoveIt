package com.example.moveit.view.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.moveit.R;
import com.example.moveit.view.entries.AddEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EntriesPage extends Fragment {
    private FloatingActionButton addEntryBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entries_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addEntryBtn = requireView().findViewById(R.id.addEntry);

        setUpAddEntryBtn();
        setUpEntryList();
    }

    private void setUpEntryList() {
        ListView entryListView = requireView().findViewById(R.id.entriesListView);
        entryListView.setEmptyView(requireView().findViewById(R.id.emptyTV));
    }

    private void setUpAddEntryBtn() {
        addEntryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEntry.class);
            startActivity(intent);
        });

    }
}