package com.pkmnapps.activitydo.dataclasses

import org.junit.runner.RunWith

class ListWidget(var uid: String?, //uid is unique id, lid is list id, aid is activity id
                 var aid: String?, var head: String?) {
    fun getUid(): String? {
        return uid
    }

    fun setUid(uid: String?) {
        this.uid = uid
    }

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
}