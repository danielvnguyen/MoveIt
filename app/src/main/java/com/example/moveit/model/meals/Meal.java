package com.example.moveit.model.meals;

public class Meal {
    private String id;
    private String name;
    private Integer calories;
    private String imageId;
    private String note;

    public Meal(String id, String name, Integer calories, String note, String imageId) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.note = note;
        this.imageId = imageId;
    }

    public Meal(String id, String name, Integer calories, String note) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.note = note;
        this.imageId = "";
    }

    //This is being used
    public Meal() {
        this.id = "";
        this.name = "";
        this.calories = 0;
        this.note = "";
        this.imageId = "";
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
