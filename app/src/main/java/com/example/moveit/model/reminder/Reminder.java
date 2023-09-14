package com.example.moveit.model.reminder;

public class Reminder {
    private long reminderTime;

    public Reminder() {
        this.reminderTime = 0;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }
}
