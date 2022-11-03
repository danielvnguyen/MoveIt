package com.example.moveit.model.meals;

import com.google.firebase.firestore.FirebaseFirestore;

public class Meal {
    private String name;
    private Integer calories;
    private String image;
    private String note;

    public Meal(String name, Integer calories, String note, String image) {
        this.name = name;
        this.calories = calories;
        this.note = note;
        this.image = image;
    }

    public Meal(String name, Integer calories, String note) {
        this.name = name;
        this.calories = calories;
        this.note = note;
        this.image = "";
    }

    public Meal() {
        this.name = "";
        this.calories = 0;
        this.note = "";
        this.image = "";
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId(String userId) {
        String mealId = this.name.replaceAll("\\s+","");
        return FirebaseFirestore.getInstance().collection("meals").document(userId)
                .collection("mealList").document(mealId).getId();
    }
}
