package com.example.dell.filetransfer_bluetooth.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dell.filetransfer_bluetooth.R;
import com.example.dell.filetransfer_bluetooth.bluetooth.BluetoothFragment;


public class TransfereFragment extends BluetoothFragment {
    public static final String TAG="TransferFragment";


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        Log.i(TAG, "onCreateView()");
        return null;
    }

    @Override
    public void updateUI(int what,Bundle bundle){

    }

}
