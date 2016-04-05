package com.example.dell.filetransfer_bluetooth.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.dell.filetransfer_bluetooth.R;
import com.example.dell.filetransfer_bluetooth.utils.ReceiveDatabase;

public class ReceiveRecordFragment extends Fragment {
    public static final String TAG="ReceiveRecordFragment";
    ReceiveDatabase rd=null ;
    Cursor cursor=null;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.ui_receive, container, false);
        Log.i(TAG,"onCreateView");

        ListView lv = (ListView)view.findViewById(R.id.receiveListView);

        rd = new ReceiveDatabase(this.getContext(),"send,db3",2);
        cursor = rd.query();
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this.getContext(),R.layout.ui_receive_listview,cursor,
                new String[]{"phoneName","mTime","fileName"},
                new int[]{R.id.phonename,R.id.receivetime,R.id.filename},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                rd.delete(position);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        return view;
    }

    @Override
    public void onDestroy(){
        Log.i(TAG,"onDestroy");
        if(cursor!=null)
            cursor.close();
        if(rd!=null)
            rd.close();
    }
 }
