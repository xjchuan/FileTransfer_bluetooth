package com.example.dell.filetransfer_bluetooth.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.dell.filetransfer_bluetooth.ui.MainActivity;

/**
 * Created by dell on 2016/4/5.
 */
public class ReceiveDatabase {
    final String TAG = "ReceiveDatabase";
    MyDatabaseHelper db;
    Activity activity;
    public ReceiveDatabase(Context context, String name, int version){
        db = MyDatabaseHelper.getInstance(context, name, version);
    }

    public void setActivity(Activity a){
        activity = a;
    }
    private void setNotifyChange(){
        if(activity!=null)
            ((MainActivity)activity).setNotifyChange();
    }
    public void insert( String phoneName,String mTime, String fileName){
        db.getWritableDatabase().execSQL("insert into receive values(null,?,?,?)",
                new String[]{phoneName, mTime, fileName});
        setNotifyChange();
    }
    public void delete(int position){
        db.getWritableDatabase().execSQL("delete from receive where _id = (select _id from receive limit " + position + ",1)");
        setNotifyChange();
    }
    public Cursor query(){
        return db.getReadableDatabase().rawQuery("select * from receive",null);
    }
    public void close(){
        db.close();
    }
}
