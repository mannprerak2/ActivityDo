package com.pkmnapps.activitydo.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pkmnapps.activitydo.dataclasses.Widget;

import java.util.ArrayList;
import java.util.List;

public class DBHelperWidgets extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "widget.db";

    public static final String HOME_TABLE_NAME = "widget";

    public static final String HOME_COLUMN_ID = "id";
    public static final String HOME_COLUMN_UID = "uid";//uniqueid
    public static final String HOME_COLUMN_SORT = "sort";//order
    Context context;
    public DBHelperWidgets(Context context) {
        super(context, DATABASE_NAME , null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table "+HOME_TABLE_NAME +
                        " (id integer primary key, aid text, uid text, sort integer) "
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS widget");
        onCreate(db);
    }

    public boolean insertWidget (String uid, int sortOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_UID, uid);
        contentValues.put(HOME_COLUMN_SORT, sortOrder);
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }
    public int getSortValue(String uid){
        int sort;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME +" where uid=?", new String[]{uid});
        res.moveToFirst();
            sort = res.getInt(res.getColumnIndex(HOME_COLUMN_SORT));
        res.close();
        return sort;
    }
    private void updateSort(String uid,int sortOrder){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_SORT, sortOrder);
        db.update(HOME_TABLE_NAME,contentValues,"uid = ?", new String[]{uid});
    }

    public int deleteWidget(String uid){//call from all databases when deleting something
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ",
                new String[] { uid });
    }
    public void updateAllWidgetSortOrders(List<Widget> widgets){//call when deleting and when adding new widgets
        SQLiteDatabase db = this.getWritableDatabase();
        for(Widget a:widgets){
            updateSort(a.getUid(),widgets.indexOf(a));
        }
    }

}