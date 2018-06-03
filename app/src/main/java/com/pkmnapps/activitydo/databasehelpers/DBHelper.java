package com.pkmnapps.activitydo.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pkmnapps.activitydo.dataclasses.ActivityData;
import com.pkmnapps.activitydo.dataclasses.ListWidget;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "activity.db";


    public static final String HOME_TABLE_NAME = "home";

    public static final String HOME_COLUMN_ID = "id";
    public static final String HOME_COLUMN_NAME = "name";
    public static final String HOME_COLUMN_COLOR = "color";
    public static final String HOME_COLUMN_PINNED = "pinned";
    public static final String HOME_COLUMN_SORT = "sort";
    Context context;
    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table "+HOME_TABLE_NAME +
                        " (id integer primary key, name text, color text, pinned integer, sort integer) "
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS pinnedevents");
        onCreate(db);
    }

    public boolean insertActivity (String id, String name, String descp, String color, String image, int pinned, int sort) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_ID, id);
        contentValues.put(HOME_COLUMN_NAME, name);
        contentValues.put(HOME_COLUMN_COLOR, color);
        contentValues.put(HOME_COLUMN_PINNED, pinned);
        contentValues.put(HOME_COLUMN_SORT, sort);
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }
    public boolean insertActivity (ActivityData activityData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_ID, activityData.getId());
        contentValues.put(HOME_COLUMN_NAME, activityData.getName());
        contentValues.put(HOME_COLUMN_COLOR, activityData.getColor());
        contentValues.put(HOME_COLUMN_PINNED, activityData.getPinned());
        contentValues.put(HOME_COLUMN_SORT, activityData.getSortOrder());
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }

    public int deleteActivity(String id){
        //delete stuff from all databases
        new DBHelperText(context).deleteAllTexts(id);
        new DBHelperImage(context).deleteAllImages(id);//also deletes files in storage
        new DBHelperList(context).deleteAllLists(id);//also deletes from list-items database

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HOME_TABLE_NAME,
                "id = ? ",
                new String[] { id });
    }

    public List<ActivityData> getAllActivitiesAsList(){
        List<ActivityData> activityDataList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME, null );
        res.moveToFirst();

        ActivityData a;
        while(!res.isAfterLast()){
            int sort = res.getInt(res.getColumnIndex(HOME_COLUMN_SORT));
            a = new ActivityData(res.getString(res.getColumnIndex(HOME_COLUMN_ID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_NAME)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_COLOR)),
                    res.getInt(res.getColumnIndex(HOME_COLUMN_PINNED)) == 1,
                    sort);
            activityDataList.add(0,a);
            res.moveToNext();
        }
        //sorting this list
        Boolean swap;
        for(int i=0;i<activityDataList.size()-1;i++){
            swap = false;
            for(int j=0;j<activityDataList.size()-1;j++){
                if(activityDataList.get(j).getSortOrder()>activityDataList.get(j+1).getSortOrder()){
                    swap = true;
                    ActivityData temp = activityDataList.get(j);
                    activityDataList.set(j,activityDataList.get(j+1));
                    activityDataList.set(j+1,temp);
                }
            }
            if(!swap)
                break;
        }
        res.close();
        return activityDataList;
    }
    public void pinActivity(String id,int bool1or0){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("pinned",bool1or0);
        db.update(HOME_TABLE_NAME,contentValues,"id = ?",new String[]{id});
    }
    public void updateActivity(String id ,String name, String descp, String color, String image, int pinned){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_NAME, name);
        contentValues.put(HOME_COLUMN_COLOR, color);
        contentValues.put(HOME_COLUMN_PINNED, pinned);
        db.update(HOME_TABLE_NAME,contentValues,"id = ?", new String[]{id});
    }
    public void updateActivity(ActivityData activityData){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_NAME, activityData.getName());
        contentValues.put(HOME_COLUMN_COLOR, activityData.getColor());
        contentValues.put(HOME_COLUMN_PINNED, activityData.getPinned());
        db.update(HOME_TABLE_NAME,contentValues,"id = ?", new String[]{activityData.getId()});
    }
    public void updateSort(String id, int sort){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_SORT, sort);
        db.update(HOME_TABLE_NAME,contentValues,"id = ?", new String[]{id});
    }
    public void updateAllSortOrders(List<ActivityData> activityDataList){
        SQLiteDatabase db = this.getWritableDatabase();
        for(ActivityData a:activityDataList){
            updateSort(a.getId(),activityDataList.indexOf(a));
        }
    }


}
