package com.example.moveit.model.entries;

import android.media.Image;

import com.example.moveit.model.activities.Activity;
import com.example.moveit.model.meals.Meal;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public class Entry {
    private String mood;
    private ArrayList<Activity> activities;
    private ArrayList<Meal> meals;
    private Integer calories;
    private Date date;
    private Time time;
    private String note;
    private ArrayList<Image> images;

    public Entry(String mood, ArrayList<Activity> activities, ArrayList<Meal> meals, Integer calories,
                 Date date, Time time, String note, ArrayList<Image> images) {
        this.mood = mood;
        this.activities = activities;
        this.meals = meals;
        this.calories = calories;
        this.date = date;
        this.time = time;
        this.note = note;
        this.images = images;
    }

    public Entry() {
        this.mood = "";
        this.activities = null;
        this.meals = null;
        this.calories = 0;
        this.date = null;
        this.time = null;
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

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }
}
