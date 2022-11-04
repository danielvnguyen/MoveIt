package com.example.moveit.model.meals;

import com.google.firebase.firestore.FirebaseFirestore;

public class Meal {
    private String name;
    private Integer calories;
    private String imageId;
    private String note;

    public Meal(String name, Integer calories, String note, String imageId) {
        this.name = name;
        this.calories = calories;
        this.note = note;
        this.imageId = imageId;
    }

    public Meal(String name, Integer calories, String note) {
        this.name = name;
        this.calories = calories;
        this.note = note;
        this.imageId = "";
    }

    //This is being used
    public Meal() {
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

    public String getId(String userId) {
        String mealId = this.name.replaceAll("\\s+","");
        return FirebaseFirestore.getInstance().collection("meals").document(userId)
                .collection("mealList").document(mealId).getId();
    }
}
