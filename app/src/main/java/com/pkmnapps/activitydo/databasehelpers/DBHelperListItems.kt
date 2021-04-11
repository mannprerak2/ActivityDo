package com.pkmnapps.activitydo.databasehelpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pkmnapps.activitydo.dataclasses.ListItem
import org.junit.runner.RunWith
import java.util.*

class DBHelperListItems(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + HOME_TABLE_NAME +
                        " (id integer primary key, uid text, lid text, content text, checker integer) "
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS listitem")
        onCreate(db)
    }

    fun insertListItem(uid: String?, lid: String?, content: String?, check: Boolean?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_UID, uid)
        contentValues.put(HOME_COLUMN_LID, lid)
        contentValues.put(HOME_COLUMN_CONTENT, content)
        contentValues.put(HOME_COLUMN_CHECK, if (check) 1 else 0)
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun insertListItem(listItem: ListItem?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_UID, listItem.getUid())
        contentValues.put(HOME_COLUMN_LID, listItem.getLid())
        contentValues.put(HOME_COLUMN_CONTENT, listItem.getContent())
        contentValues.put(HOME_COLUMN_CHECK, if (listItem.getChecked()) 1 else 0)
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun deleteListItem(uid: String?): Int {
        val db = this.writableDatabase
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ", arrayOf(uid))
    }

    fun deleteAllListItem(lid: String?): Int {
        val db = this.writableDatabase
        return db.delete(HOME_TABLE_NAME,
                "lid = ? ", arrayOf(lid))
    }

    fun getAllListItemsAsList(lid: String?): MutableList<ListItem?>? {
        val listItems: MutableList<ListItem?> = ArrayList()
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME + " where lid=?", arrayOf(lid))
        res.moveToFirst()
        var a: ListItem
        while (!res.isAfterLast) {
            a = ListItem(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_LID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_CONTENT)),
                    res.getInt(res.getColumnIndex(HOME_COLUMN_CHECK)) == 1)
            listItems.add(a)
            res.moveToNext()
        }
        res.close()
        return listItems
    }

    fun get8ListItemsAsList(lid: String?): MutableList<ListItem?>? {
        val listItems: MutableList<ListItem?> = ArrayList()
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME + " where lid=? LIMIT 8", arrayOf(lid))
        res.moveToFirst()
        var a: ListItem
        while (!res.isAfterLast) {
            a = ListItem(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_LID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_CONTENT)),
                    res.getInt(res.getColumnIndex(HOME_COLUMN_CHECK)) == 1)
            listItems.add(a)
            res.moveToNext()
        }
        res.close()
        return listItems
    }

    fun updateActivity(listItem: ListItem?): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_CHECK, if (listItem.getChecked()) 1 else 0)
        contentValues.put(HOME_COLUMN_CONTENT, listItem.getContent())
        return db.update(HOME_TABLE_NAME, contentValues, "uid = ?", arrayOf(listItem.getUid()))
    }

    companion object {
        val DATABASE_NAME: String? = "listitem.db"
        val HOME_TABLE_NAME: String? = "listitem"
        val HOME_COLUMN_ID: String? = "id"
        val HOME_COLUMN_UID: String? = "uid" //uniqueid
        val HOME_COLUMN_LID: String? = "lid" //listId
        val HOME_COLUMN_AID: String? = "aid" //activityid
        val HOME_COLUMN_CONTENT: String? = "content"
        val HOME_COLUMN_CHECK: String? = "checker"
    }
}