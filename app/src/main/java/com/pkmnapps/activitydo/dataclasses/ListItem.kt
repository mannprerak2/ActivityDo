package com.pkmnapps.activitydo.dataclasses

import org.junit.runner.RunWith

class ListItem(var uid: String?, var lid: String?, var content: String?, var checked: Boolean?) {
    fun getLid(): String? {
        return lid
    }

    fun setLid(lid: String?) {
        this.lid = lid
    }

    fun getUid(): String? {
        return uid
    }

    fun setUid(uid: String?) {
        this.uid = uid
    }

    fun getContent(): String? {
        return content
    }

    fun setContent(content: String?) {
        this.content = content
    }

    fun getChecked(): Boolean? {
        return checked
    }

    fun setChecked(checked: Boolean?) {
        this.checked = checked
    }
}