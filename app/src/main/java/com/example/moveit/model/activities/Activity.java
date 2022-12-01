package com.example.moveit.model.activities;

public class Activity {

    private final String name;
    private final String categoryId;
    private final String activityId;

    public Activity(String name, String categoryId, String activityId){
        this.name = name;
        this.categoryId = categoryId;
        this.activityId = activityId;
    };

    public String getName() {
        return name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getActivityId() {
        return activityId;
    }
}
