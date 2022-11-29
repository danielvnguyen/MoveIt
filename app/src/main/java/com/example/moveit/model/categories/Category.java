package com.example.moveit.model.categories;

public class Category {
    private String name;
    private String categoryId;

    public Category(String name, String categoryId) {
        this.name = name;
        this.categoryId = categoryId;
    }

    //Do not remove
    public Category() {
        this.name = "";
        this.categoryId = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
