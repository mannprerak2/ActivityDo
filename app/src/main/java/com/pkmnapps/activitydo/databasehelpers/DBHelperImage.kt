package com.pkmnapps.activitydo.databasehelpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import com.pkmnapps.activitydo.dataclasses.ImageWidget
import org.junit.runner.RunWith
import java.io.File
import java.util.*

class DBHelperImage(val context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + HOME_TABLE_NAME +
                        " (id integer primary key, aid text, uid text, uri text) "
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS image")
        onCreate(db)
    }

    fun insertImage(uid: String?, aid: String?, uri: String?): Boolean {
        insertWidgetForSort(uid)
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_AID, aid)
        contentValues.put(HOME_COLUMN_UID, uid)
        contentValues.put(HOME_COLUMN_URI, uri)
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun insertImage(imageWidget: ImageWidget?): Boolean {
        insertWidgetForSort(imageWidget.getUid())
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_AID, imageWidget.getAid())
        contentValues.put(HOME_COLUMN_UID, imageWidget.getUid())
        contentValues.put(HOME_COLUMN_URI, imageWidget.getImageUri())
        db.insert(HOME_TABLE_NAME, null, contentValues)
        return true
    }

    fun updateAid(uid: String?, aid: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HOME_COLUMN_AID, aid)
        db.update(HOME_TABLE_NAME, contentValues, "uid = ?", arrayOf(uid))
    }

    fun deleteImage(uid: String?): Int {
        deleteWidgetinSort(uid)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "$uid.jpg")
        if (file.exists()) file.delete()
        val db = this.writableDatabase
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ", arrayOf(uid))
    }

    fun deleteAllImages(aid: String?) {
        for (i in getAllImagesAsList(aid)) {
            deleteImage(i.getUid())
        }
    }

    fun getAllImagesAsList(aid: String?): MutableList<ImageWidget?>? {
        val imageWidgets: MutableList<ImageWidget?> = ArrayList()
        val db = this.readableDatabase
        val res = db.rawQuery("select * from  " + HOME_TABLE_NAME + " where aid=?", arrayOf(aid))
        res.moveToFirst()
        var a: ImageWidget
        while (!res.isAfterLast) {
            a = ImageWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_URI)))
            imageWidgets.add(a)
            res.moveToNext()
        }
        res.close()
        return imageWidgets
    }

    private fun insertWidgetForSort(uid: String?) { //call this in every insert method
        DBHelperWidgets(context).insertWidget(uid, 0)
    }

    private fun deleteWidgetinSort(uid: String?) { //call this in every delete method
        DBHelperWidgets(context).deleteWidget(uid)
    }

    companion object {
        val DATABASE_NAME: String? = "image.db"
        val HOME_TABLE_NAME: String? = "image"
        val HOME_COLUMN_ID: String? = "id"
        val HOME_COLUMN_AID: String? = "aid" //activityid
        val HOME_COLUMN_UID: String? = "uid" //uniqueid
        val HOME_COLUMN_URI: String? = "uri"
    }
}