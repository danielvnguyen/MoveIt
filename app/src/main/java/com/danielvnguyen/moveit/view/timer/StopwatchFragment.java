package com.danielvnguyen.moveit.view.timer;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.danielvnguyen.moveit.R;
import java.util.ArrayList;
import java.util.Locale;

public class StopwatchFragment extends Fragment {

    private TextView stopwatchDisplay;
    private Button startBtn, stopBtn, lapBtn, resetBtn, resumeBtn;
    private ListView lapListView;

    private LinearLayout activeButtonsLayout;
    private LinearLayout pausedButtonsLayout;

    private long startTime = 0L;
    private long timeBuffer = 0L;
    private boolean isRunning = false;

    private Handler handler = new Handler();
    private Runnable runnable;

    private ArrayList<String> lapTimes = new ArrayList<>();
    private ArrayAdapter<String> lapAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);

        stopwatchDisplay = view.findViewById(R.id.stopwatchDisplay);
        startBtn = view.findViewById(R.id.startBtn);
        stopBtn = view.findViewById(R.id.stopBtn);
        lapBtn = view.findViewById(R.id.lapBtn);
        resetBtn = view.findViewById(R.id.resetBtn);
        resumeBtn = view.findViewById(R.id.resumeBtn);
        lapListView = view.findViewById(R.id.lapListView);

        activeButtonsLayout = view.findViewById(R.id.activeButtons);
        pausedButtonsLayout = view.findViewById(R.id.pausedButtons);

        lapAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, lapTimes);
        lapListView.setAdapter(lapAdapter);

        runnable = new Runnable() {
            @Override
            public void run() {
                long timeElapsed = SystemClock.elapsedRealtime() - startTime + timeBuffer;

                int minutes = (int) (timeElapsed / 60000);
                int seconds = (int) (timeElapsed % 60000) / 1000;
                int milliseconds = (int) (timeElapsed % 1000);
                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", minutes, seconds, milliseconds / 10);
                stopwatchDisplay.setText(time);
                handler.postDelayed(this, 10);
            }
        };

        startBtn.setOnClickListener(v -> startStopwatch());
        stopBtn.setOnClickListener(v -> stopStopwatch());
        lapBtn.setOnClickListener(v -> recordLap());
        resumeBtn.setOnClickListener(v -> resumeStopwatch());
        resetBtn.setOnClickListener(v -> resetStopwatch());

        resetStopwatch();

        return view;
    }

    private void startStopwatch() {
        startTime = SystemClock.elapsedRealtime();
        handler.post(runnable);
        isRunning = true;

        activeButtonsLayout.setVisibility(View.VISIBLE);
        pausedButtonsLayout.setVisibility(View.GONE);

        startBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.VISIBLE);
        lapBtn.setVisibility(View.VISIBLE);
    }

    private void stopStopwatch() {
        timeBuffer += SystemClock.elapsedRealtime() - startTime;
        handler.removeCallbacks(runnable);
        isRunning = false;

        activeButtonsLayout.setVisibility(View.GONE);
        pausedButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void resumeStopwatch() {
        startTime = SystemClock.elapsedRealtime();
        handler.post(runnable);
        isRunning = true;

        activeButtonsLayout.setVisibility(View.VISIBLE);
        pausedButtonsLayout.setVisibility(View.GONE);

        startBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.VISIBLE);
        lapBtn.setVisibility(View.VISIBLE);
    }

    private void resetStopwatch() {
        handler.removeCallbacks(runnable);
        isRunning = false;
        startTime = 0L;
        timeBuffer = 0L;
        stopwatchDisplay.setText("00:00:00");

        lapTimes.clear();
        lapAdapter.notifyDataSetChanged();

        activeButtonsLayout.setVisibility(View.VISIBLE);
        pausedButtonsLayout.setVisibility(View.GONE);

        startBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.GONE);
        lapBtn.setVisibility(View.GONE);
    }

    private void recordLap() {
        String lapTime = stopwatchDisplay.getText().toString();
        lapTimes.add("Lap " + (lapTimes.size() + 1) + ": " + lapTime);
        lapAdapter.notifyDataSetChanged();
    }
}
