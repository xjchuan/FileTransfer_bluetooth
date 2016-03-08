package com.example.dell.filetransfer_bluetooth.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.dell.filetransfer_bluetooth.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class BluetoothService extends Service {

    private static final String TAG="BluetoothService";
    private static final UUID MY_UUID=UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> deviceArrayList; //附近的蓝牙设别列表
    private ArrayList<String> deviceNameList;///
    private ArrayAdapter arrayAdapter;

    private String bluetoothAddress;
    private BluetoothSocket bluetoothSocket;
    private Thread bluetoothDataReadThread;

    private Handler handler;

    public BluetoothService(){}

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG, "onCreate()");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceArrayList=new ArrayList<BluetoothDevice>();
        deviceNameList=new ArrayList<String>();
        arrayAdapter=new ArrayAdapter<String>(this,
                R.layout.bluetooth_device_list_item, deviceNameList);
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        turnOnBluetooth();
        startDiscovery();
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.i(TAG, "onUnbind()");
        disconnectToDevice();
        stopDiscovery();
        turnOffBluetooth();
        return super.onUnbind(intent);
    }

    public ArrayList<BluetoothDevice> getDeviceArrayList(){
        return deviceArrayList;
    }

    public ArrayAdapter getArrayAdapter(){
        return arrayAdapter;
    }

    public void setHandler(Handler handler){
        this.handler=handler;
    }

    public void setBluetoothAddress(String bluetoothAddress){
        this.bluetoothAddress=bluetoothAddress;
    }

    public String getBluetoothAddress(){
        return bluetoothAddress;
    }

    public void turnOnBluetooth(){
        Log.d(TAG, "turnOnBluetooth()");
        if(bluetoothAdapter==null){
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if(bluetoothAdapter.isEnabled()==false) {
            ///应修改提示
            //请求用户开启蓝牙。
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void turnOffBluetooth(){
        Log.d(TAG,"turnOffBluetooth()");
        if(bluetoothAdapter!=null && bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
        }
    }

    public void startDiscovery(){
        Log.d(TAG, "startDiscovery()");
        if(bluetoothAdapter.isEnabled() && bluetoothAdapter.isDiscovering())
        {
            deviceArrayList.clear();
            deviceNameList.clear();
            arrayAdapter.notifyDataSetChanged();

            registerReceiver(discoveryResult, new IntentFilter(
                    BluetoothDevice.ACTION_FOUND));
            bluetoothAdapter.startDiscovery();
        }
    }

    public void stopDiscovery(){
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            unregisterReceiver(discoveryResult);
        }

    }

    public void connectToDevice(){
        if(bluetoothAddress!=null){
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(this.bluetoothAddress);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothSocket.connect();
                        Log.d(TAG, "bluetoothSocket.connect()");
                        //donot close the socket
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        readData();
                    }
                }
            }).start();
        }
        else{
            Log.e(TAG, "bluetoothAddress==null.at connectToDevice()");
        }
    }

    public void connectToDevice(String bluetoothAddress){
        this.bluetoothAddress=bluetoothAddress;
        connectToDevice();
    }

    public void disconnectToDevice(){
        Log.i(TAG,"disconnectToDevice()");
        if(bluetoothDataReadThread!=null){
            bluetoothDataReadThread.interrupt();
            bluetoothDataReadThread=null;
        }
        ///
        /*if(bluetoothSocket!=null){
            if(bluetoothSocket.isConnected()==true){
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bluetoothSocket=null;
        }*/
    }

    public void readData(){
        //从蓝牙中读取数据
        if(handler!=null
                &&bluetoothSocket.isConnected()){
            bluetoothDataReadThread=new Thread(new BluetoothDataReadTask(bluetoothSocket,handler));
            bluetoothDataReadThread.start();
        }
        else{
            Log.e(TAG, "readData() error");
        }
    }



    public class MyBinder extends Binder {
        public BluetoothService getService(){
            return BluetoothService.this;
        }
    }

    private BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            Log.d(TAG,"discoeried bluetooth device name:"+name);

            if(name==null){
                name="no name";
            }
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            deviceArrayList.add(device);
            deviceNameList.add(name);
            arrayAdapter.notifyDataSetChanged();
        }
    };


    /**
     * 读取蓝牙数据的任务
     */
    private class BluetoothDataReadTask implements Runnable{
        private BluetoothDataReadTask(BluetoothSocket bluetoothSocket,Handler handler){

        }

        @Override
        public void run() {
            try {
                readData();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         * 从蓝牙Socket中读取数据
         * @throws IOException
         */
        private void readData() throws IOException {}


        private void sendErrorMessage(int what,String key,String value){
            Bundle bundle=new Bundle();
            bundle.putString(key, value);
            Message errorMessage=new Message();
            errorMessage.what=what;
            errorMessage.setData(bundle);
            handler.sendMessage(errorMessage);
        }
    }
}
