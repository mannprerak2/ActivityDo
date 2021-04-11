package com.pkmnapps.activitydo.dataclasses

import org.junit.runner.RunWith

class Widget(val type: Int, val `object`: Any?, var uid: String?, var sortOrder: Int) {
    fun getType(): Int {
        return type
    }

    fun getObject(): Any? {
        return `object`
    }

    fun getSortOrder(): Int {
        return sortOrder
    }

    fun setSortOrder(sortOrder: Int) {
        this.sortOrder = sortOrder
    }

    fun getUid(): String? {
        return uid
    }

    fun setUid(uid: String?) {
        this.uid = uid
    }
}