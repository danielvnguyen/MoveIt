package com.danielvnguyen.moveit.view.timer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.danielvnguyen.moveit.R;

import java.util.Locale;
import java.util.Map;

public class TimerFragment extends Fragment {

    private TextView timerDisplay;
    private Button setTimeBtn, startBtn, pauseBtn, resumeBtn, resetBtn;
    private Button savePresetBtn, loadPresetBtn, deletePresetBtn;

    private CountDownTimer countDownTimer;
    private long totalMillis = 0;
    private long millisLeft = 0;
    private boolean isTimerRunning = false;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "timer_presets";

    private MediaPlayer mediaPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        timerDisplay = view.findViewById(R.id.timerDisplay);
        setTimeBtn = view.findViewById(R.id.setTimeBtn);
        startBtn = view.findViewById(R.id.startBtn);
        pauseBtn = view.findViewById(R.id.pauseBtn);
        resumeBtn = view.findViewById(R.id.resumeBtn);
        resetBtn = view.findViewById(R.id.resetBtn);
        savePresetBtn = view.findViewById(R.id.savePresetBtn);
        loadPresetBtn = view.findViewById(R.id.loadPresetBtn);
        deletePresetBtn = view.findViewById(R.id.deletePresetBtn);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setTimeBtn.setOnClickListener(v -> showTimePickerDialog());

        startBtn.setOnClickListener(v -> {
            if (millisLeft > 0) {
                startTimer();
            }
        });

        pauseBtn.setOnClickListener(v -> pauseTimer());
        resumeBtn.setOnClickListener(v -> resumeTimer());
        resetBtn.setOnClickListener(v -> resetTimer());

        savePresetBtn.setOnClickListener(v -> promptPresetNameToSave());
        loadPresetBtn.setOnClickListener(v -> showLoadPresetDialog());
        deletePresetBtn.setOnClickListener(v -> showDeletePresetDialog());

        return view;
    }

    private void showTimePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set Duration");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(32, 32, 32, 32);

        NumberPicker hourPicker = new NumberPicker(getContext());
        hourPicker.setMaxValue(23);
        hourPicker.setMinValue(0);

        NumberPicker minutePicker = new NumberPicker(getContext());
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);

        NumberPicker secondPicker = new NumberPicker(getContext());
        secondPicker.setMaxValue(59);
        secondPicker.setMinValue(0);

        layout.addView(hourPicker);
        layout.addView(minutePicker);
        layout.addView(secondPicker);

        builder.setView(layout);

        builder.setPositiveButton("Set", (dialog, which) -> {
            int h = hourPicker.getValue();
            int m = minutePicker.getValue();
            int s = secondPicker.getValue();

            totalMillis = ((h * 3600) + (m * 60) + s) * 1000L;
            millisLeft = totalMillis;
            updateDisplay(millisLeft);
            toggleButtonsInitialState();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(millisLeft, 1000) {
            public void onTick(long millisUntilFinished) {
                millisLeft = millisUntilFinished;
                updateDisplay(millisLeft);
            }

            public void onFinish() {
                isTimerRunning = false;
                timerDisplay.setText("Done!");
                toggleButtonsAfterFinish();
                playAlarmSound();
            }
        }.start();

        isTimerRunning = true;
        toggleButtonsRunningState();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        pauseBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.VISIBLE);
    }

    private void resumeTimer() {
        startTimer();
        resumeBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        millisLeft = totalMillis;
        updateDisplay(millisLeft);
        isTimerRunning = false;

        setTimeBtn.setVisibility(View.VISIBLE);
        resetBtn.setVisibility(View.GONE);
        startBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.GONE);
    }

    private void updateDisplay(long millis) {
        int hours = (int) (millis / 1000) / 3600;
        int minutes = (int) ((millis / 1000) % 3600) / 60;
        int seconds = (int) (millis / 1000) % 60;

        String formatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        timerDisplay.setText(formatted);
    }

    private void toggleButtonsInitialState() {
        setTimeBtn.setVisibility(View.VISIBLE);
        resetBtn.setVisibility(View.GONE);
        startBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.GONE);
    }

    private void toggleButtonsRunningState() {
        setTimeBtn.setVisibility(View.GONE);
        resetBtn.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
        resumeBtn.setVisibility(View.GONE);
    }

    private void toggleButtonsAfterFinish() {
        setTimeBtn.setVisibility(View.VISIBLE);
        resetBtn.setVisibility(View.GONE);
        startBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.GONE);
    }

    private void promptPresetNameToSave() {
        EditText input = new EditText(getContext());

        new AlertDialog.Builder(getContext())
                .setTitle("Save Preset")
                .setMessage("Enter a name for this preset:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        prefs.edit().putLong(name, totalMillis).apply();
                        Toast.makeText(getContext(), "Preset saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoadPresetDialog() {
        Map<String, ?> allPresets = prefs.getAll();
        if (allPresets.isEmpty()) {
            Toast.makeText(getContext(), "No presets saved", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = allPresets.keySet().toArray(new String[0]);

        new AlertDialog.Builder(getContext())
                .setTitle("Load Preset")
                .setItems(names, (dialog, which) -> {
                    String name = names[which];
                    totalMillis = prefs.getLong(name, 0);
                    millisLeft = totalMillis;
                    updateDisplay(millisLeft);
                    toggleButtonsInitialState();
                })
                .show();
    }

    private void showDeletePresetDialog() {
        Map<String, ?> allPresets = prefs.getAll();
        if (allPresets.isEmpty()) {
            Toast.makeText(getContext(), "No presets saved", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = allPresets.keySet().toArray(new String[0]);

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Preset")
                .setItems(names, (dialog, which) -> {
                    String name = names[which];
                    prefs.edit().remove(name).apply();
                    Toast.makeText(getContext(), "Deleted preset: " + name, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void playAlarmSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.timer);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        new AlertDialog.Builder(getContext())
                .setTitle("Time's up!")
                .setMessage("The timer has completed.")
                .setCancelable(false)
                .setPositiveButton("Dismiss", (dialog, which) -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
