package com.example.dell.filetransfer_bluetooth.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.dell.filetransfer_bluetooth.R;
import com.example.dell.filetransfer_bluetooth.bluetooth.BluetoothFragment;
import com.example.dell.filetransfer_bluetooth.utils.FileUtils;


public class TransfereFragment extends BluetoothFragment {
    public static final String TAG="TransferFragment";

    ToggleButton toggleButton;
    Button button;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        Log.i(TAG, "onCreateView()");
        final View view = inflater.inflate(R.layout.transfer, container, false);

        button = (Button)view.findViewById(R.id.selectPicture);
        button.setEnabled(false);
        toggleButton = (ToggleButton)view.findViewById(R.id.bluetooth_open);

        button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    showFileChooser();
                }
        });
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
            button.setEnabled(false);
            toggleButton.setEnabled(true);
            toggleButton.setText(R.string.button_unclick);
            toggleButton.setChecked(false);

            Context c= getContext();
            if(c!=null)
                Toast.makeText(c, "连接已断开", Toast.LENGTH_SHORT).show();
        }
        else if (what == R.integer.change_buttonUnused)
            toggleButton.setEnabled(false);
            toggleButton.setText(R.string.button_connected);
            button.setEnabled(true);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        stopWork();
    }


    //-------------------------------
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this.getContext(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }


    //选择的结果

    public void onActivityResult(int requestCode, int resultCode, Intent data)  {
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this.getContext(), uri);
                    Log.i(TAG, "uri "+uri+" path"+path);

                    //通知service开始传输数据
                    if(bluetoothService!=null){
                        bluetoothService.startTransferFile(uri,getContext());
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}



