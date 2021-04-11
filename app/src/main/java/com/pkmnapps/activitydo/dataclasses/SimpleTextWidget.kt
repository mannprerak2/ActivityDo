package com.pkmnapps.activitydo.dataclasses

import org.junit.runner.RunWith

class SimpleTextWidget     //aid is of activity
//uid is unique aid of this element
(var uid: String?, var aid: String?, var head: String?, var body: String?) {
    fun getAid(): String? {
        return aid
    }

    fun setAid(aid: String?) {
        this.aid = aid
    }

    fun getHead(): String? {
        return head
    }

    fun setHead(head: String?) {
        this.head = head
    }

    fun getBody(): String? {
        return body
    }

    fun setBody(body: String?) {
        this.body = body
    }

    fun getUid(): String? {
        return uid
    }

    fun setUid(uid: String?) {
        this.uid = uid
    }
}