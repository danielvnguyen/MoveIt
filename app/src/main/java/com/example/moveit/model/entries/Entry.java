package com.example.moveit.model.entries;

import com.example.moveit.model.activities.Activity;
import com.example.moveit.model.meals.Meal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public class Entry {
    private String mood;
    private ArrayList<Activity> activities;
    private ArrayList<Meal> meals;
    private Date date;
    private Time time;
    private String note;
    private String imageId;

    public Entry(String mood, ArrayList<Activity> activities, ArrayList<Meal> meals,
                 Date date, Time time, String note, String imageId) {
        this.mood = mood;
        this.activities = activities;
        this.meals = meals;
        this.date = date;
        this.time = time;
        this.note = note;
        this.imageId = imageId;
    }

    public Entry() {
        this.mood = "";
        this.activities = null;
        this.meals = null;
        this.date = null;
        this.time = null;
        this.note = "";
        this.imageId = "";
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public ArrayList<Activity> getActivities() {
        return activities;
    }

    public void setActivities(ArrayList<Activity> activities) {
        this.activities = activities;
    }

    public ArrayList<Meal> getMeals() {
        return meals;
    }

    public void setMeals(ArrayList<Meal> meals) {
        this.meals = meals;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }
}
