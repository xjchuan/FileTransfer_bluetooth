package com.example.dell.filetransfer_bluetooth.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dell on 2016/4/5.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
    static Context c;
    static  String n;
    static int v;

    //sql language
    final String Create_Table_Receive = "create table receive(_id integer primary key autoincrement , phoneName," +
            "mTime,fileName)";
    final String Create_Table_Send = "create table send(_id integer primary key autoincrement , phoneName," +
            "mTime,fileName)";

    private MyDatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);

    }
    private static class SingletonHolder {

        private final static MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(c,n,v);
    }

    public static MyDatabaseHelper getInstance(Context context, String name, int version){
        c=context;
        n = name;
        v = version;
        return SingletonHolder.myDatabaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Create_Table_Receive);
        db.execSQL(Create_Table_Send);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
