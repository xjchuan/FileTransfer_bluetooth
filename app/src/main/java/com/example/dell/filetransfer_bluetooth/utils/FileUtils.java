package com.example.dell.filetransfer_bluetooth.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by dell on 2016/3/29.
 */
public class FileUtils {
    public static String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor=null ;

            try {
                cursor = context.getContentResolver().query(uri, null,null, null, null);
                //int column_index = cursor.getColumnIndexOrThrow(projection[0]);
                if (cursor.moveToFirst()) {
                    return cursor.getString(1);
                }
            } catch (Exception e) {
                // Eat it
            }
            finally {
                if(cursor!=null)
                    cursor.close();
            }
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
