package com.example.dell.filetransfer_bluetooth.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.dell.filetransfer_bluetooth.R;
import com.example.dell.filetransfer_bluetooth.bluetooth.BluetoothFragment;


public class TransfereFragment extends BluetoothFragment {
    public static final String TAG="TransferFragment";

    Button bluetooth_button;

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
        bluetooth_button = (Button)view.findViewById(R.id.bluetooth_button);
        bluetooth_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth_button.setText(R.string.button_click);
                bluetooth_button.setClickable(false);

                startWork();
            }
        });
        return view;
    }

    @Override
    public void updateUI(int what,Bundle bundle){
        if(what == R.integer.change_buttontext) {
            bluetooth_button.setText(R.string.button_unclick);
            bluetooth_button.setClickable(true);
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        stopWork();
    }

}
