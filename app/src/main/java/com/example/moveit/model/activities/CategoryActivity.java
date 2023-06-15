package com.example.moveit.model.activities;

public class CategoryActivity {

    private final String name;
    private final String categoryId;
    private final String activityId;
    private String note;

    public CategoryActivity(String name, String categoryId, String activityId, String note){
        this.name = name;
        this.categoryId = categoryId;
        this.activityId = activityId;
        this.note = note;
    };

    public CategoryActivity() {
        this.name = "";
        this.categoryId = "";
        this.activityId = "";
        this.note = "";
    }

    public String getName() {
        return name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
