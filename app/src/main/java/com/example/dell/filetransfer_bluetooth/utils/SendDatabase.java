package com.example.dell.filetransfer_bluetooth.utils;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by dell on 2016/4/5.
 */
public class SendDatabase {
    MyDatabaseHelper db;
    public SendDatabase(Context context, String name, int version){
        db = MyDatabaseHelper.getInstance(context, name, version);
    }

    public void insert( String phoneName,String mTime, String fileName){
        db.getWritableDatabase().execSQL("insert into send values(null,?,?,?)",
                new String[]{phoneName,mTime,fileName});
    }
    public void delete(int id){
        db.getWritableDatabase().execSQL("delete from send where _id="+id);
    }
    public Cursor query(){
        return db.getReadableDatabase().rawQuery("select * from send",null);
    }
    public void close(){
        db.close();
    }
}
