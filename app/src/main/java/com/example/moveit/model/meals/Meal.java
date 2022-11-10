package com.example.moveit.model.meals;

import androidx.annotation.NonNull;

public class Meal {
    private final String id;
    private final String name;
    private final Integer calories;
    private final String imageId;
    private final String note;

    public Meal(String id, String name, Integer calories, String note, String imageId) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.note = note;
        this.imageId = imageId;
    }

    //This is being used
    public Meal() {
        this.id = "";
        this.name = "";
        this.calories = 0;
        this.note = "";
        this.imageId = "";
    }

    @NonNull
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Integer getCalories() {
        return calories;
    }

    public String getNote() {
        return note;
    }

    public String getImageId() {
        return imageId;
    }

    public String getId() {
        return id;
    }
}
