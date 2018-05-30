package com.pkmnapps.activitydo.dataclasses;

public class ImageWidget {
    String uid,aid;
    String imageUri;


    public ImageWidget(String uid, String aid, String imageUri) {
        this.uid = uid;
        this.aid = aid;
        this.imageUri = imageUri;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }
}
