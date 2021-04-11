package com.pkmnapps.activitydo.databasehelpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pkmnapps.activitydo.dataclasses.Widget
import org.junit.runner.RunWith

class DBHelperWidgets(val context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + HOME_TABLE_NAME +
                        " (id integer primary key, aid text, uid text, sort integer) "
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS widget")
        onCreate(db)
    }

    fun insertWidget(uid: String?, sortOrder: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_UID, uid)
        contentValues.put(HOME_COLUMN_SORT, sortOrder)
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun getSortValue(uid: String?): Int {
        val sort: Int
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME + " where uid=?", arrayOf(uid))
        res.moveToFirst()
        sort = res.getInt(res.getColumnIndex(HOME_COLUMN_SORT))
        res.close()
        return sort
    }

    private fun updateSort(uid: String?, sortOrder: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_SORT, sortOrder)
        db.update(HOME_TABLE_NAME, contentValues, "uid = ?", arrayOf(uid))
    }

    fun deleteWidget(uid: String?): Int { //call from all databases when deleting something
        val db = this.writableDatabase
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ", arrayOf(uid))
    }

    fun updateAllWidgetSortOrders(widgets: MutableList<Widget?>?) { //call when deleting and when adding new widgets
        val db = this.writableDatabase
        for (a in widgets) {
            updateSort(a.getUid(), widgets.indexOf(a))
        }
    }

    companion object {
        val DATABASE_NAME: String? = "widget.db"
        val HOME_TABLE_NAME: String? = "widget"
        val HOME_COLUMN_ID: String? = "id"
        val HOME_COLUMN_UID: String? = "uid" //uniqueid
        val HOME_COLUMN_SORT: String? = "sort" //order
    }
}