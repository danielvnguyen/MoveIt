package com.example.moveit.model;

import android.media.Image;

import java.util.ArrayList;
import java.util.Date;

public class Entry {
    private String mood;
    private ArrayList<Activity> activities;
    private ArrayList<Meal> meals;
    private Integer calories;
    private Date date;
    private String note;
    private ArrayList<Image> images;

    public Entry(String mood, ArrayList<Activity> activities, ArrayList<Meal> meals, Integer calories,
                 Date date, String note, ArrayList<Image> images) {
        this.mood = mood;
        this.activities = activities;
        this.meals = meals;
        this.calories = calories;
        this.date = date;
        this.note = note;
        this.images = images;
    }

    public Entry() {
        this.mood = "";
        this.activities = null;
        this.meals = null;
        this.calories = 0;
        this.date = null;
        this.note = "";
        this.images = null;
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

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }
}
