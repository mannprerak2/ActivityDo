package com.pkmnapps.activitydo.dataclasses;

public class SimpleTextWidget {
    String aid,uid;
    String head,body;

    public SimpleTextWidget(String uid, String aid, String head, String body) {
        this.aid = aid;//aid is of activity
        this.uid = uid;//uid is unique aid of this element
        this.head = head;
        this.body = body;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
