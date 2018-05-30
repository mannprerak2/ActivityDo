package com.pkmnapps.activitydo.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pkmnapps.activitydo.dataclasses.ListWidget;

import java.util.ArrayList;
import java.util.List;

public class DBHelperList extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "list.db";

    public static final String HOME_TABLE_NAME = "list";

    public static final String HOME_COLUMN_ID = "id";
    public static final String HOME_COLUMN_AID = "aid";//activityid
    public static final String HOME_COLUMN_UID = "uid";//uniqueid
    public static final String HOME_COLUMN_HEAD = "head";

    Context context;

    public DBHelperList(Context context) {
        super(context, DATABASE_NAME , null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table "+HOME_TABLE_NAME +
                        " (id integer primary key, aid text, uid text, head text) "
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS list");
        onCreate(db);
    }

    public boolean insertList (String uid, String aid, String head) {
        insertWidgetForSort(uid);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_AID, aid);
        contentValues.put(HOME_COLUMN_UID, uid);
        contentValues.put(HOME_COLUMN_HEAD, head);
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }
    public boolean insertList (ListWidget listWidget) {
        insertWidgetForSort(listWidget.getUid());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_AID, listWidget.getAid());
        contentValues.put(HOME_COLUMN_UID, listWidget.getUid());
        contentValues.put(HOME_COLUMN_HEAD, listWidget.getHead());
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }

    public int deleteList(String uid){
        deleteWidgetinSort(uid);
        //also delete from second database
        new DBHelperListItems(context).deleteAllListItem(uid);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ",
                new String[] { uid });
    }
    public void deleteAllLists(String aid){
        for(ListWidget l:getAllListAsList(aid)){
            deleteList(l.getUid());
        }
    }

    public ListWidget getListWidget(String lid){
        ListWidget listWidget;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME +" where uid=?", new String[]{lid});
        res.moveToFirst();
            listWidget = new ListWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_HEAD)));
        res.close();
        return listWidget;
    }

    public List<ListWidget> getAllListAsList(String aid){
        List<ListWidget> listWidgets = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME +" where aid=?", new String[]{aid});
        res.moveToFirst();

        ListWidget a;
        while(!res.isAfterLast()){
            a = new ListWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_HEAD)));
            listWidgets.add(a);
            res.moveToNext();
        }
        res.close();
        return listWidgets;
    }

    public void updateActivity(ListWidget listWidget){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_HEAD, listWidget.getHead());
        db.update(HOME_TABLE_NAME,contentValues,"uid = ?", new String[]{listWidget.getUid()});
    }
    public void updateHead(String lid, String head){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_HEAD, head);
        db.update(HOME_TABLE_NAME,contentValues,"uid = ?", new String[]{lid});
    }

    private void insertWidgetForSort(String uid){//call this in every insert method
        new DBHelperWidgets(context).insertWidget(uid,0);
    }
    private void deleteWidgetinSort(String uid){//call this in every delete method
        new DBHelperWidgets(context).deleteWidget(uid);
    }
}