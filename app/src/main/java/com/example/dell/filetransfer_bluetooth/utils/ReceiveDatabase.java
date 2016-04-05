package com.example.dell.filetransfer_bluetooth.utils;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by dell on 2016/4/5.
 */
public class ReceiveDatabase {
    MyDatabaseHelper db;
    public ReceiveDatabase(Context context, String name, int version){
        db = MyDatabaseHelper.getInstance(context, name, version);
    }

    public void insert( String phoneName,String mTime, String fileName){
        db.getWritableDatabase().execSQL("insert into receive values(null,?,?,?)",
                new String[]{phoneName,mTime,fileName});
    }
    public void delete(int id){
        db.getWritableDatabase().execSQL("delete from receive where id="+id);
    }
    public Cursor query(){
        return db.getReadableDatabase().rawQuery("select * from receive",null);
    }
    public void close(){
        db.close();
    }
}
