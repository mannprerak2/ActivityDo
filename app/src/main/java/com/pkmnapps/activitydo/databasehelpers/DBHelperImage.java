package com.pkmnapps.activitydo.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.pkmnapps.activitydo.TaskActivity;
import com.pkmnapps.activitydo.dataclasses.ImageWidget;
import com.pkmnapps.activitydo.dataclasses.SimpleTextWidget;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DBHelperImage extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "image.db";

    public static final String HOME_TABLE_NAME = "image";

    public static final String HOME_COLUMN_ID = "id";
    public static final String HOME_COLUMN_AID = "aid";//activityid
    public static final String HOME_COLUMN_UID = "uid";//uniqueid
    public static final String HOME_COLUMN_URI = "uri";
    Context context;
    public DBHelperImage(Context context) {
        super(context, DATABASE_NAME , null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table "+HOME_TABLE_NAME +
                        " (id integer primary key, aid text, uid text, uri text) "
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS image");
        onCreate(db);
    }

    public boolean insertImage (String uid, String aid, String uri) {
        insertWidgetForSort(uid);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_AID, aid);
        contentValues.put(HOME_COLUMN_UID, uid);
        contentValues.put(HOME_COLUMN_URI, uri);
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }
    public boolean insertImage (ImageWidget imageWidget) {
        insertWidgetForSort(imageWidget.getUid());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOME_COLUMN_AID, imageWidget.getAid());
        contentValues.put(HOME_COLUMN_UID, imageWidget.getUid());
        contentValues.put(HOME_COLUMN_URI, imageWidget.getImageUri());
        db.insert(HOME_TABLE_NAME, null, contentValues);
        return true;
    }

    public int deleteImage(String uid){
        deleteWidgetinSort(uid);
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),(uid+".jpg"));
        if(file.exists())
            file.delete();
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HOME_TABLE_NAME,
                "uid = ? ",
                new String[] { uid });
    }

    public void deleteAllImages(String aid){
        for (ImageWidget i:getAllImagesAsList(aid)){
            deleteImage(i.getUid());
        }
    }

    public List<ImageWidget> getAllImagesAsList(String aid){
        List<ImageWidget> imageWidgets = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from  "+HOME_TABLE_NAME +" where aid=?", new String[]{aid});
        res.moveToFirst();

        ImageWidget a;
        while(!res.isAfterLast()){
            a = new ImageWidget(res.getString(res.getColumnIndex(HOME_COLUMN_UID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_AID)),
                    res.getString(res.getColumnIndex(HOME_COLUMN_URI)));

            imageWidgets.add(a);
            res.moveToNext();
        }
        res.close();
        return imageWidgets;
    }

    private void insertWidgetForSort(String uid){//call this in every insert method
        new DBHelperWidgets(context).insertWidget(uid,0);
    }
    private void deleteWidgetinSort(String uid){//call this in every delete method
        new DBHelperWidgets(context).deleteWidget(uid);
    }

}