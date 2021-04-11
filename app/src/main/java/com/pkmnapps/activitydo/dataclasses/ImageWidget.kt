package com.pkmnapps.activitydo.dataclasses

import org.junit.runner.RunWith

class ImageWidget(var uid: String?, var aid: String?, var imageUri: String?) {
    fun getImageUri(): String? {
        return imageUri
    }

    fun setImageUri(imageUri: String?) {
        this.imageUri = imageUri
    }

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