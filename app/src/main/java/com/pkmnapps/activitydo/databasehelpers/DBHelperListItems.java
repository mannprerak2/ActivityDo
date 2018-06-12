package com.pkmnapps.activitydo.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pkmnapps.activitydo.dataclasses.ListItem;
import com.pkmnapps.activitydo.dataclasses.ListWidget;

import java.util.ArrayList;
import java.util.List;

public class DBHelperListItems extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "listitem.db";

    public static final String HOME_TABLE_NAME = "listitem";

    public static final String HOME_COLUMN_ID = "id";
    public static final String HOME_COLUMN_UID = "uid";//uniqueid
    public static final String HOME_COLUMN_LID = "lid";//listId
    public static final String HOME_COLUMN_AID = "aid";//activityid
    public static final String HOME_COLUMN_CONTENT = "content";
    public static final String HOME_COLUMN_CHECK = "checker";

    public DBHelperListItems(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table "+HOME_TABLE_NAME +
                        " (id integer primary key, uid text, lid text, content text, checker integer) "
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS listitem");
        onCreate(db);
    }

    public boolean insertListItem (String uid, String lid, String content, Boolean check) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_UID, uid);
        contentValues.put(HOME_COLUMN_LID, lid);
        contentValues.put(HOME_COLUMN_CONTENT, content);
        contentValues.put(HOME_COLUMN_CHECK, check?1:0);
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }
    public boolean insertListItem (ListItem listItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_UID, listItem.getUid());
        contentValues.put(HOME_COLUMN_LID, listItem.getLid());
        contentValues.put(HOME_COLUMN_CONTENT, listItem.getContent());
        contentValues.put(HOME_COLUMN_CHECK, listItem.getChecked()?1:0);
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }

    public int deleteListItem(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ",
                new String[] { uid });
    }
    public int deleteAllListItem(String lid){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HOME_TABLE_NAME,
                "lid = ? ",
                new String[] { lid });
    }

    public List<ListItem> getAllListItemsAsList(String lid){
        List<ListItem> listItems = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME +" where lid=?", new String[]{lid});
        res.moveToFirst();

        ListItem a;
        while(!res.isAfterLast()){
            a = new ListItem(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_LID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_CONTENT)),
                    res.getInt(res.getColumnIndex(HOME_COLUMN_CHECK)) == 1);
            listItems.add(a);
            res.moveToNext();
        }
        res.close();
        return listItems;
    }
    public List<ListItem> get8ListItemsAsList(String lid){
        List<ListItem> listItems = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME +" where lid=? LIMIT 8", new String[]{lid});
        res.moveToFirst();

        ListItem a;
        while(!res.isAfterLast()){
            a = new ListItem(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_LID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_CONTENT)),
                    res.getInt(res.getColumnIndex(HOME_COLUMN_CHECK)) == 1);
            listItems.add(a);
            res.moveToNext();
        }
        res.close();
        return listItems;
    }

    public int updateActivity(ListItem listItem){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_CHECK, listItem.getChecked()?1:0);
        contentValues.put(HOME_COLUMN_CONTENT, listItem.getContent());
        return db.update(HOME_TABLE_NAME,contentValues,"uid = ?", new String[]{listItem.getUid()});
    }
}