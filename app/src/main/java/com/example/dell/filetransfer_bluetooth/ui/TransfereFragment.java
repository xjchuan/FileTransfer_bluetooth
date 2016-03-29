package com.example.dell.filetransfer_bluetooth.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import com.example.dell.filetransfer_bluetooth.R;
import com.example.dell.filetransfer_bluetooth.bluetooth.BluetoothFragment;


public class TransfereFragment extends BluetoothFragment {
    public static final String TAG="TransferFragment";

    ToggleButton toggleButton;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        Log.i(TAG, "onCreateView()");
        final View view = inflater.inflate(R.layout.transfer, container, false);

        //设置button的作用
        toggleButton = (ToggleButton)view.findViewById(R.id.bluetooth_open);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.bluetooth_open:
                        if(((ToggleButton)v).isChecked()){
                            startWork();
                        }
                        else{
                            stopWork();
                        }
                        break;
                }
            }
        });
        return view;
    }

    @Override
    public void updateUI(int what,Bundle bundle){
        if(what == R.integer.change_buttontext) {
            toggleButton.setText(R.string.button_unclick);
            toggleButton.setChecked(false);
        }
        else if (what == R.integer.change_buttonUnused)
            toggleButton.setClickable(false);
            toggleButton.setText(R.string.button_connected);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        stopWork();
    }

}
