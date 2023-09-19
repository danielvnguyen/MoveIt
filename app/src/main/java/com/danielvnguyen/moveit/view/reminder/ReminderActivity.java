package com.danielvnguyen.moveit.view.reminder;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.danielvnguyen.moveit.R;
import com.danielvnguyen.moveit.model.reminder.NotificationReceiver;
import com.danielvnguyen.moveit.model.reminder.Reminder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ReminderActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Reminder currentTime;
    private EditText timeInput;

    private Button saveBtn;
    private Button resetBtn;
    private Boolean isDefault = true;

    private int selectedHour;
    private int selectedMinute;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.remind_me_at));

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        saveBtn = findViewById(R.id.reminderSaveBtn);
        resetBtn = findViewById(R.id.reminderResetBtn);
        currentTime = new Reminder();

        db.collection("reminders").document(currentUser.getUid())
            .collection("reminderTime").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (Objects.requireNonNull(task.getResult()).isEmpty()) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, 20);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        currentTime.setReminderTime(c.getTimeInMillis());
                        isDefault = true;
                    } else {
                        currentTime = task.getResult().getDocuments().get(0).toObject(Reminder.class);
                        isDefault = false;
                    }
                    assert currentTime != null;
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(currentTime.getReminderTime());
                    selectedHour = c.get(Calendar.HOUR_OF_DAY);
                    selectedMinute = c.get(Calendar.MINUTE);
                    setUpTime(currentTime);
                    setUpButtons();
                }
            });
    }

    private void setUpButtons() {
        saveBtn = findViewById(R.id.reminderSaveBtn);
        resetBtn = findViewById(R.id.reminderResetBtn);
        if (isDefault) {
            resetBtn.setVisibility(View.GONE);
        } else {
            resetBtn.setVisibility(View.VISIBLE);
        }

        saveBtn.setOnClickListener(v -> db.collection("reminders").document(currentUser.getUid())
                .collection("reminderTime").document("reminder").set(currentTime).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ReminderActivity.this, ("Successfully updated reminder time to " +
                                sdf.format(currentTime.getReminderTime())), Toast.LENGTH_SHORT).show();
                        resetBtn.setVisibility(View.VISIBLE);
                        setUpAlarm(currentTime);
                    } else {
                        Toast.makeText(ReminderActivity.this, "Failed to updated reminder time", Toast.LENGTH_SHORT).show();
                    }
                }));

        resetBtn.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 20);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            currentTime.setReminderTime(c.getTimeInMillis());
            db.collection("reminders").document(currentUser.getUid())
                    .collection("reminderTime").document("reminder").delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            timeInput.setText(sdf.format(c.getTimeInMillis()));
                            Toast.makeText(ReminderActivity.this, ("Successfully reset reminder time to "+
                                    sdf.format(c.getTimeInMillis())), Toast.LENGTH_SHORT).show();
                            resetBtn.setVisibility(View.GONE);
                            setUpAlarm(currentTime);
                        } else {
                            Toast.makeText(ReminderActivity.this, "Failed to reset reminder time", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void setUpAlarm(Reminder currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime.getReminderTime());

        if (calendar.getTime().compareTo(new Date()) < 0) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void setUpTime(Reminder currentTime) {
        timeInput = findViewById(R.id.reminderTimeInput);
        timeInput.setText(sdf.format(currentTime.getReminderTime()));
    }

    public void showTimePickerDialog(View v) {
        TimePickerDialog.OnTimeSetListener timeSetListener = this;
        TimePickerDialog dialog = new TimePickerDialog(this,
                timeSetListener, selectedHour, selectedMinute, false);
        dialog.show();
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
        return new Intent(context, ReminderActivity.class);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedHour = hourOfDay;
        selectedMinute = minute;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, selectedHour);
        c.set(Calendar.MINUTE, selectedMinute);
        c.set(Calendar.SECOND, 0);
        currentTime.setReminderTime(c.getTimeInMillis());
        timeInput.setText(sdf.format(c.getTimeInMillis()));
    }
}