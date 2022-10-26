package com.example.moveit.model;

import android.media.Image;

public class Meal {
    private String name;
    private Integer calories;
    private Image image;

    public Meal(String name, Integer calories, Image image) {
        this.name = name;
        this.calories = calories;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Image getImage() {
        return image;
    }
}
