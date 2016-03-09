package com.example.dell.filetransfer_bluetooth.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BluetoothFragment extends Fragment {
    private static final String TAG="BluetoothFragment";

    private Intent intentToBluetootthService;
    private BluetoothService bluetoothService;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        intentToBluetootthService=new Intent(this.getContext(),BluetoothService.class);
    }

    private void getBluetoothAddressByDialog(){
        if(bluetoothService!=null){
            new AlertDialog
                    .Builder(getContext())
                    .setTitle("Bluetooth List")
                    .setAdapter(bluetoothService.getArrayAdapter(),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, final int which) {
                                    bluetoothService.stopDiscovery();
                                    bluetoothService.setBluetoothAddress(bluetoothService
                                            .getDeviceArrayList()
                                            .get(which)
                                            .getAddress());
                                    bluetoothService.connectToDevice();
                                }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface arg0) {
                            bluetoothService.stopDiscovery();
                        }
                    }).show();
        }
    }


    /*
     * 打开蓝牙
     */
    public void turnOnBluetooth(){
        Log.d(TAG, "turnOnBluetooth()");
        if(bluetoothAdapter==null){
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if(!bluetoothAdapter.isEnabled()) {
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);Log.i(TAG, "startActivityForResult ");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                bindBluetoothService();
                Log.i(TAG,"agree to open bluetooth ");
            }
            else
                Log.i(TAG,"cancel opening bluetooth");
        }
    }
    /**
     * 启动蓝牙等一系列工作
     */
    protected void startWork(){
        turnOnBluetooth();
    }

    protected  void bindBluetoothService(){
        this.getActivity().bindService(intentToBluetootthService, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    /**
     * 关闭蓝牙等一系列工作
     */
    protected void stopWork(){
        if(bluetoothService!=null) {
            this.getActivity().unbindService(serviceConnection);
            bluetoothService.turnOffBluetooth();
        }
    }

    /**
     * 更新用户界面
     * @param what 指明更新界面的哪一部分
     * @param bundle 附带更新界面的数据
     */
    protected abstract void updateUI(int what,Bundle bundle);




    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected()");
            bluetoothService=((BluetoothService.MyBinder)service).getService();
            bluetoothService.setHandler(handler);
            getBluetoothAddressByDialog();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG,"onServiceDisconnected()");
            bluetoothService=null;
        }
    };

    final Handler handler= new Handler() {
        public void handleMessage(Message msg) {
            Log.i(TAG,"handleMessage()");
            updateUI(msg.what,msg.getData());
        }
    };
}