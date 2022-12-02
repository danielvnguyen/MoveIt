package com.example.moveit.model.meals;

public class ServingSize {
    private final Integer size;
    private final String units;

    public ServingSize(Integer size, String units) {
        this.size = size;
        this.units = units;
    }

    public ServingSize() {
        this.size = 0;
        this.units = "";
    }

    public Integer getSize() {
        return size;
    }

    public String getUnits() {
        return units;
    }
}
