package com.pkmnapps.activitydo.dataclasses;

public class ListItem {
    String uid, content, lid;
    Boolean checked;

    public ListItem(String uid, String lid, String content, Boolean checked) {
        this.uid = uid;
        this.lid = lid;
        this.content = content;
        this.checked = checked;
    }

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
