package com.pkmnapps.activitydo.databasehelpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pkmnapps.activitydo.dataclasses.ActivityData
import org.junit.runner.RunWith
import java.util.*

class DBHelper(val context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + HOME_TABLE_NAME +
                        " (id integer primary key, name text, color text, pinned integer, sort integer) "
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS pinnedevents")
        onCreate(db)
    }

    fun insertActivity(id: String?, name: String?, descp: String?, color: String?, image: String?, pinned: Int, sort: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_ID, id)
        contentValues.put(HOME_COLUMN_NAME, name)
        contentValues.put(HOME_COLUMN_COLOR, color)
        contentValues.put(HOME_COLUMN_PINNED, pinned)
        contentValues.put(HOME_COLUMN_SORT, sort)
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun insertActivity(activityData: ActivityData?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_ID, activityData.getId())
        contentValues.put(HOME_COLUMN_NAME, activityData.getName())
        contentValues.put(HOME_COLUMN_COLOR, activityData.getColor())
        contentValues.put(HOME_COLUMN_PINNED, activityData.getPinned())
        contentValues.put(HOME_COLUMN_SORT, activityData.getSortOrder())
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun deleteActivity(id: String?): Int {
        //delete stuff from all databases
        DBHelperText(context).deleteAllTexts(id)
        DBHelperImage(context).deleteAllImages(id) //also deletes files in storage
        DBHelperList(context).deleteAllLists(id) //also deletes from list-items database
        val db = this.writableDatabase
        return db.delete(HOME_TABLE_NAME,
                "id = ? ", arrayOf(id))
    }

    fun getAllActivitiesAsList(): MutableList<ActivityData?>? {
        val activityDataList: MutableList<ActivityData?> = ArrayList()
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME, null)
        res.moveToFirst()
        var a: ActivityData
        while (!res.isAfterLast) {
            val sort = res.getInt(res.getColumnIndex(HOME_COLUMN_SORT))
            a = ActivityData(res.getString(res.getColumnIndex(HOME_COLUMN_ID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_NAME)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_COLOR)),
                    res.getInt(res.getColumnIndex(HOME_COLUMN_PINNED)) == 1,
                    sort)
            activityDataList.add(0, a)
            res.moveToNext()
        }
        //sorting this list
        var swap: Boolean
        for (i in 0 until activityDataList.size - 1) {
            swap = false
            for (j in 0 until activityDataList.size - 1) {
                if (activityDataList[j].getSortOrder() > activityDataList[j + 1].getSortOrder()) {
                    swap = true
                    val temp = activityDataList[j]
                    activityDataList[j] = activityDataList[j + 1]
                    activityDataList[j + 1] = temp
                }
            }
            if (!swap) break
        }
        res.close()
        return activityDataList
    }

    fun pinActivity(id: String?, bool1or0: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("pinned", bool1or0)
        db.update(HOME_TABLE_NAME, contentValues, "id = ?", arrayOf(id))
    }

    fun updateActivity(id: String?, name: String?, descp: String?, color: String?, image: String?, pinned: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_NAME, name)
        contentValues.put(HOME_COLUMN_COLOR, color)
        contentValues.put(HOME_COLUMN_PINNED, pinned)
        db.update(HOME_TABLE_NAME, contentValues, "id = ?", arrayOf(id))
    }

    fun updateActivity(activityData: ActivityData?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_NAME, activityData.getName())
        contentValues.put(HOME_COLUMN_COLOR, activityData.getColor())
        contentValues.put(HOME_COLUMN_PINNED, activityData.getPinned())
        db.update(HOME_TABLE_NAME, contentValues, "id = ?", arrayOf(activityData.getId()))
    }

    fun updateSort(id: String?, sort: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_SORT, sort)
        db.update(HOME_TABLE_NAME, contentValues, "id = ?", arrayOf(id))
    }

    fun updateAllSortOrders(activityDataList: MutableList<ActivityData?>?) {
        val db = this.writableDatabase
        for (a in activityDataList) {
            updateSort(a.getId(), activityDataList.indexOf(a))
        }
    }

    companion object {
        val DATABASE_NAME: String? = "activity.db"
        val HOME_TABLE_NAME: String? = "home"
        val HOME_COLUMN_ID: String? = "id"
        val HOME_COLUMN_NAME: String? = "name"
        val HOME_COLUMN_COLOR: String? = "color"
        val HOME_COLUMN_PINNED: String? = "pinned"
        val HOME_COLUMN_SORT: String? = "sort"
    }
}