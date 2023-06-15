package com.example.moveit.model;

//Global class used to update fragments in the HomeActivity
public class GlobalUpdater {
    private static GlobalUpdater instance;
    private boolean entryListUpdated;
    private boolean calendarUpdated;
    private boolean galleryUpdated;

    private GlobalUpdater() {}

    public static synchronized GlobalUpdater getInstance() {
        if (instance == null) {
            instance = new GlobalUpdater();
        }
        return instance;
    }

    public boolean isEntryListUpdated() {
        return entryListUpdated;
    }

    public void setEntryListUpdated(boolean entryListUpdated) {
        this.entryListUpdated = entryListUpdated;
    }

    public boolean isCalendarUpdated() {
        return calendarUpdated;
    }

    public void setCalendarUpdated(boolean calendarUpdated) {
        this.calendarUpdated = calendarUpdated;
    }

    public boolean isGalleryUpdated() {
        return galleryUpdated;
    }

    public void setGalleryUpdated(boolean galleryUpdated) {
        this.galleryUpdated = galleryUpdated;
    }
}
