package com.example.dell.filetransfer_bluetooth.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    View view;
    ListView lv=null;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ui_receive, container, false);
        Log.i(TAG, "onCreateView");
        lv = (ListView)view.findViewById(R.id.receiveListView);


        rd = new ReceiveDatabase(this.getContext(),"receive,db3",2);
        rd.setActivity(getActivity());
        cursor = rd.query();
        rd.setActivity(getActivity());
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this.getContext(),R.layout.ui_receive_listview,cursor,
                new String[]{"phoneName","mTime","fileName"},
                new int[]{R.id.phonename,R.id.receivetime,R.id.filename},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getContext()).setTitle("确认删除?").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rd.delete(position);
                        adapter.notifyDataSetChanged();
                        //((MainActivity)getActivity()).setNotifyChange();
                    }
                }).setNegativeButton("取消", null).show();

                return true;
            }
        });
        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        if(cursor!=null)
            cursor.close();
        if(rd!=null)
            rd.close();
    }
 }
