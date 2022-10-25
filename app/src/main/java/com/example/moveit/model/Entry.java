package com.example.moveit.model;

import android.media.Image;
import java.util.Date;

public class Entry {
    private String mood;
    private Activity[] activities;
    private Meal[] meals;
    private Integer calories;
    private Date date;
    private String note;
    private Image[] images;

    public Entry(String mood, Activity[] activities, Meal[] meals, Integer calories,
                 Date date, String note, Image[] images) {
        this.mood = mood;
        this.activities = activities;
        this.meals = meals;
        this.calories = calories;
        this.date = date;
        this.note = note;
        this.images = images;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public Activity[] getActivities() {
        return activities;
    }

    public void setActivities(Activity[] activities) {
        this.activities = activities;
    }

    public Meal[] getMeals() {
        return meals;
    }

    public void setMeals(Meal[] meals) {
        this.meals = meals;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
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

    public Image[] getImages() {
        return images;
    }

    public void setImages(Image[] images) {
        this.images = images;
    }
}
