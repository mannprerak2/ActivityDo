package com.pkmnapps.activitydo.dataclasses

import org.junit.runner.RunWith

class ActivityData {
    private var name: String? = null
    private var id: String? = null
    private var color: String? = null
    private var pinned: Boolean? = null
    private var sortOrder = 0

    constructor() {}
    constructor(id: String?, name: String?, color: String?) {
        this.name = name
        this.id = id
        this.color = color
        pinned = false
    }

    constructor(id: String?, name: String?, color: String?, pinned: Boolean?, sortOrder: Int) {
        this.name = name
        this.id = id
        this.color = color
        this.pinned = pinned
        this.sortOrder = sortOrder
    }

    fun getId(): String? {
        return id
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getColor(): String? {
        return color
    }

    fun setColor(color: String?) {
        this.color = color
    }

    fun getPinned(): Boolean? {
        return pinned
    }

    fun setPinned(pinned: Boolean?) {
        this.pinned = pinned
    }

    fun getSortOrder(): Int {
        return sortOrder
    }

    fun setSortOrder(sortOrder: Int) {
        this.sortOrder = sortOrder
    }
}