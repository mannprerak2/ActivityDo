package com.pkmnapps.activitydo.databasehelpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pkmnapps.activitydo.dataclasses.ListWidget
import org.junit.runner.RunWith
import java.util.*

class DBHelperList(val context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + HOME_TABLE_NAME +
                        " (id integer primary key, aid text, uid text, head text) "
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS list")
        onCreate(db)
    }

    fun insertList(uid: String?, aid: String?, head: String?): Boolean {
        insertWidgetForSort(uid)
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_AID, aid)
        contentValues.put(HOME_COLUMN_UID, uid)
        contentValues.put(HOME_COLUMN_HEAD, head)
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun insertList(listWidget: ListWidget?): Boolean {
        insertWidgetForSort(listWidget.getUid())
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_AID, listWidget.getAid())
        contentValues.put(HOME_COLUMN_UID, listWidget.getUid())
        contentValues.put(HOME_COLUMN_HEAD, listWidget.getHead())
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun deleteList(uid: String?): Int {
        deleteWidgetinSort(uid)
        //also delete from second database
        DBHelperListItems(context).deleteAllListItem(uid)
        val db = this.writableDatabase
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ", arrayOf(uid))
    }

    fun deleteAllLists(aid: String?) {
        for (l in getAllListAsList(aid)) {
            deleteList(l.getUid())
        }
    }

    fun getListWidget(lid: String?): ListWidget? {
        var listWidget: ListWidget? = null
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME + " where uid=?", arrayOf(lid))
        res.moveToFirst()
        if (res.count > 0) {
            listWidget = ListWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_HEAD)))
        }
        res.close()
        return listWidget
    }

    fun getAllListAsList(aid: String?): MutableList<ListWidget?>? {
        val listWidgets: MutableList<ListWidget?> = ArrayList()
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME + " where aid=?", arrayOf(aid))
        res.moveToFirst()
        var a: ListWidget
        while (!res.isAfterLast) {
            a = ListWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_HEAD)))
            listWidgets.add(a)
            res.moveToNext()
        }
        res.close()
        return listWidgets
    }

    fun updateActivity(listWidget: ListWidget?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_HEAD, listWidget.getHead())
        db.update(HOME_TABLE_NAME, contentValues, "uid = ?", arrayOf(listWidget.getUid()))
    }

    fun updateHead(lid: String?, head: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_HEAD, head)
        db.update(HOME_TABLE_NAME, contentValues, "uid = ?", arrayOf(lid))
    }

    fun updateAid(uid: String?, aid: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_AID, aid)
        db.update(HOME_TABLE_NAME, contentValues, "uid = ?", arrayOf(uid))
    }

    private fun insertWidgetForSort(uid: String?) { //call this in every insert method
        DBHelperWidgets(context).insertWidget(uid, 0)
    }

    private fun deleteWidgetinSort(uid: String?) { //call this in every delete method
        DBHelperWidgets(context).deleteWidget(uid)
    }

    companion object {
        val DATABASE_NAME: String? = "list.db"
        val HOME_TABLE_NAME: String? = "list"
        val HOME_COLUMN_ID: String? = "id"
        val HOME_COLUMN_AID: String? = "aid" //activityid
        val HOME_COLUMN_UID: String? = "uid" //uniqueid
        val HOME_COLUMN_HEAD: String? = "head"
    }
}