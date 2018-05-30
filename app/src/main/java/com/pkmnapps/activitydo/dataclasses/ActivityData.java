package com.pkmnapps.activitydo.dataclasses;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Date;

public class ActivityData {
    private String name,id;
    private String color;
    private Boolean pinned;
    private int sortOrder;

    public ActivityData() {
    }

    public ActivityData(String id, String name, String color) {
        this.name = name;
        this.id = id;
        this.color = color;
        this.pinned = false;
    }

    public ActivityData(String id, String name, String color, Boolean pinned, int sortOrder ) {
        this.name = name;
        this.id = id;
        this.color = color;
        this.pinned = pinned;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
