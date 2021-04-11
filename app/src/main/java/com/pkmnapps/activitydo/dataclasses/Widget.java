package com.pkmnapps.activitydo.dataclasses;

public class Widget {
    final int type;
    final Object object;
    int sortOrder;
    String uid;
    public Widget(int type, Object object, String uid, int sortOrder) {
        this.type = type;
        this.object = object;
        this.uid = uid;
        this.sortOrder = sortOrder;
    }

    public int getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
