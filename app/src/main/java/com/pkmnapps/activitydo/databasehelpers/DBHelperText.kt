package com.pkmnapps.activitydo.databasehelpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pkmnapps.activitydo.dataclasses.SimpleTextWidget
import org.junit.runner.RunWith
import java.util.*

class DBHelperText(val context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + HOME_TABLE_NAME +
                        " (id integer primary key, aid text, uid text, head text, body text) "
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS text")
        onCreate(db)
    }

    fun insertText(uid: String?, aid: String?, head: String?, body: String?): Boolean {
        insertWidgetForSort(uid)
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_AID, aid)
        contentValues.put(HOME_COLUMN_UID, uid)
        contentValues.put(HOME_COLUMN_HEAD, head)
        contentValues.put(HOME_COLUMN_BODY, body)
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun insertText(simpleTextWidget: SimpleTextWidget?): Boolean {
        insertWidgetForSort(simpleTextWidget.getUid())
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_AID, simpleTextWidget.getAid())
        contentValues.put(HOME_COLUMN_UID, simpleTextWidget.getUid())
        contentValues.put(HOME_COLUMN_HEAD, simpleTextWidget.getHead())
        contentValues.put(HOME_COLUMN_BODY, simpleTextWidget.getBody())
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun deleteText(uid: String?): Int {
        deleteWidgetinSort(uid)
        val db = this.writableDatabase
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ", arrayOf(uid))
    }

    fun deleteAllTexts(aid: String?): Int {
        val db = this.writableDatabase
        return db.delete(HOME_TABLE_NAME,
                "aid = ? ", arrayOf(aid))
    }

    fun getTextWidget(uid: String?): SimpleTextWidget? {
        var s: SimpleTextWidget? = null
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME + " where uid=?", arrayOf(uid))
        res.moveToFirst()
        if (res.count != 0) {
            s = SimpleTextWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_HEAD)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_BODY)))
        }
        res.close()
        return s
    }

    fun getAllTextsAsList(aid: String?): MutableList<SimpleTextWidget?>? {
        val simpleTextWidgets: MutableList<SimpleTextWidget?> = ArrayList()
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME + " where aid=?", arrayOf(aid))
        res.moveToFirst()
        var a: SimpleTextWidget
        while (!res.isAfterLast) {
            a = SimpleTextWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_HEAD)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_BODY)))
            simpleTextWidgets.add(a)
            res.moveToNext()
        }
        res.close()
        return simpleTextWidgets
    }

    fun updateActivity(simpleTextWidget: SimpleTextWidget?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_HEAD, simpleTextWidget.getHead())
        contentValues.put(HOME_COLUMN_BODY, simpleTextWidget.getBody())
        db.update(HOME_TABLE_NAME, contentValues, "uid = ?", arrayOf(simpleTextWidget.getUid()))
    }

    fun updateHeadBody(uid: String?, head: String?, body: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_HEAD, head)
        contentValues.put(HOME_COLUMN_BODY, body)
        db.update(HOME_TABLE_NAME, contentValues, "uid = ?", arrayOf(uid))
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
        val DATABASE_NAME: String? = "text.db"
        val HOME_TABLE_NAME: String? = "text"
        val HOME_COLUMN_ID: String? = "id"
        val HOME_COLUMN_AID: String? = "aid" //activityid
        val HOME_COLUMN_UID: String? = "uid" //uniqueid
        val HOME_COLUMN_HEAD: String? = "head"
        val HOME_COLUMN_BODY: String? = "body"
    }
}