package com.example.moveit.model.entries;

import java.util.ArrayList;

public class Entry {
    private String id;
    private String mood;
    private ArrayList<String> meals;
    private ArrayList<String> activities;
    private Integer caloriesEaten;
    private long date;
    private long time;
    private String note;
    private String imageId;

    public Entry() {
        this.id = "";
        this.mood = "";
        this.meals = null;
        this.activities = null;
        this.caloriesEaten = 0;
        this.date = 0;
        this.time = 0;
        this.note = "";
        this.imageId = "";
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public ArrayList<String> getActivities() {
        return activities;
    }

    public void setActivities(ArrayList<String> activities) {
        this.activities = activities;
    }

    public ArrayList<String> getMeals() {
        return meals;
    }

    public void setMeals(ArrayList<String> meals) {
        this.meals = meals;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Integer getCaloriesEaten() {
        return caloriesEaten;
    }

    public void setCaloriesEaten(Integer caloriesEaten) {
        this.caloriesEaten = caloriesEaten;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
