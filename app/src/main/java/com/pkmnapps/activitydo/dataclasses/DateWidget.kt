package com.pkmnapps.activitydo.dataclasses

import org.junit.runner.RunWith

class DateWidget {
    var uid: String? = null
    var aid: String? = null
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
}