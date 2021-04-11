package com.pkmnapps.activitydo.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pkmnapps.activitydo.dataclasses.SimpleTextWidget;

import java.util.ArrayList;
import java.util.List;

public class DBHelperText extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "text.db";

    public static final String HOME_TABLE_NAME = "text";

    public static final String HOME_COLUMN_ID = "id";
    public static final String HOME_COLUMN_AID = "aid";//activityid
    public static final String HOME_COLUMN_UID = "uid";//uniqueid
    public static final String HOME_COLUMN_HEAD = "head";
    public static final String HOME_COLUMN_BODY = "body";

    final Context context;
    public DBHelperText(Context context) {
        super(context, DATABASE_NAME , null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table "+HOME_TABLE_NAME +
                        " (id integer primary key, aid text, uid text, head text, body text) "
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS text");
        onCreate(db);
    }

    public boolean insertText (String uid, String aid, String head, String body) {
        insertWidgetForSort(uid);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_AID, aid);
        contentValues.put(HOME_COLUMN_UID, uid);
        contentValues.put(HOME_COLUMN_HEAD, head);
        contentValues.put(HOME_COLUMN_BODY, body);
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }
    public boolean insertText (SimpleTextWidget simpleTextWidget) {
        insertWidgetForSort(simpleTextWidget.getUid());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_AID, simpleTextWidget.getAid());
        contentValues.put(HOME_COLUMN_UID, simpleTextWidget.getUid());
        contentValues.put(HOME_COLUMN_HEAD, simpleTextWidget.getHead());
        contentValues.put(HOME_COLUMN_BODY, simpleTextWidget.getBody());
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }

    public int deleteText(String uid){
        deleteWidgetinSort(uid);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ",
                new String[] { uid });
    }
    public int deleteAllTexts(String aid){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HOME_TABLE_NAME,
                "aid = ? ",
                new String[] { aid });
    }
    public SimpleTextWidget getTextWidget(String uid){
        SimpleTextWidget s = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME +" where uid=?", new String[]{uid});
        res.moveToFirst();
        if(res.getCount()!=0) {
            s = new SimpleTextWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_HEAD)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_BODY)));
        }
        res.close();
        return s;
    }

    public List<SimpleTextWidget> getAllTextsAsList(String aid){
        List<SimpleTextWidget> simpleTextWidgets = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME +" where aid=?", new String[]{aid});
        res.moveToFirst();

        SimpleTextWidget a;
        while(!res.isAfterLast()){
            a = new SimpleTextWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_HEAD)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_BODY)));
            simpleTextWidgets.add(a);
            res.moveToNext();
        }
        res.close();
        return simpleTextWidgets;
    }

    public void updateActivity(SimpleTextWidget simpleTextWidget){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_HEAD, simpleTextWidget.getHead());
        contentValues.put(HOME_COLUMN_BODY, simpleTextWidget.getBody());
        db.update(HOME_TABLE_NAME,contentValues,"uid = ?", new String[]{simpleTextWidget.getUid()});
    }
    public void updateHeadBody(String uid, String head, String body){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_HEAD, head);
        contentValues.put(HOME_COLUMN_BODY, body);
        db.update(HOME_TABLE_NAME,contentValues,"uid = ?", new String[]{uid});
    }
    public void updateAid(String uid,String aid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_AID, aid);
        db.update(HOME_TABLE_NAME,contentValues,"uid = ?", new String[]{uid});
    }

    private void insertWidgetForSort(String uid){//call this in every insert method
        new DBHelperWidgets(context).insertWidget(uid,0);
    }
    private void deleteWidgetinSort(String uid){//call this in every delete method
        new DBHelperWidgets(context).deleteWidget(uid);
    }
}
