package com.example.moveit.model.activities;

public class BalanceActivity implements Activity {
    private final String name;
    private final String categoryId;
    private final String activityId;

    public BalanceActivity(String name, String categoryId, String activityId) {
        this.name = name;
        this.categoryId = categoryId;
        this.activityId = activityId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getCategoryId() {
        return this.categoryId;
    }

    @Override
    public String getActivityId() {
        return this.activityId;
    }
}
