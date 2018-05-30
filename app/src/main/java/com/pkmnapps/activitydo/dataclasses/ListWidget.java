package com.pkmnapps.activitydo.dataclasses;

public class ListWidget {
    String uid,aid;//uid is unique id, lid is list id, aid is activity id
    String head;

    public ListWidget(String uid, String aid, String head) {
        this.uid = uid;
        this.aid = aid;
        this.head = head;
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

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }
}
