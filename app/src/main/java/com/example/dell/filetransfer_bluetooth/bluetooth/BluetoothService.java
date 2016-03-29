package com.example.dell.filetransfer_bluetooth.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
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
import java.util.Scanner;
import java.util.Set;
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
    private String bluetoothName;
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
        //turnOnBluetooth();
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

    public void setBluetoothAddressAndName(String bluetoothAddress,String bluetoothName){
        this.bluetoothAddress=bluetoothAddress;
        this.bluetoothName = bluetoothName;
    }

    public String getBluetoothAddress(){
        return bluetoothAddress;
    }



    public void turnOffBluetooth(){
        Log.d(TAG,"turnOffBluetooth()");
        if(bluetoothAdapter!=null && bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
        }
    }

    public void startDiscovery(){
        if(bluetoothAdapter.isEnabled())
        {
            deviceArrayList.clear();
            deviceNameList.clear();
            arrayAdapter.notifyDataSetChanged();

            registerReceiver(discoveryResult, new IntentFilter(
                    BluetoothDevice.ACTION_FOUND));

            //使蓝牙设备可见，方便配对
            Intent in=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);


            bluetoothAdapter.startDiscovery();

            // 监听以前绑定过的设备
            listenpairedDevice();
            Log.i(TAG,"startDiscovery");
        }
        else
            Log.i(TAG,"Failed to startDiscovery");
    }

    public void stopDiscovery(){
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            unregisterReceiver(discoveryResult);
            Log.i(TAG, "stopDiscovery");
        }

    }
    private void  listenpairedDevice(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                waitingForConnect(device.getName());
            }
        }
    }
    public void waitingForConnect(final String name){
            try {
                final BluetoothServerSocket serverSocket
                        = bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, MY_UUID);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                            Log.i("TAG","waiting for accept  "+name);
                            try {
                                bluetoothSocket = serverSocket.accept(100*1000);
                                handler.sendEmptyMessage(R.integer.change_buttonUnused);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                }).start();

            }
            catch (IOException e) {
                e.printStackTrace();
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "bluetoothSocket.connecting()");
                    try {
                        while(!bluetoothSocket.isConnected()){
                            bluetoothSocket.connect();
                            handler.sendEmptyMessage(R.integer.change_buttonUnused);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "bluetoothSocket.connect failed");
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
            Log.d(TAG,"discovery bluetooth device name:"+name);

            if(name==null){
                name="no name";
            }
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            deviceArrayList.add(device);
            deviceNameList.add(name);
            arrayAdapter.notifyDataSetChanged();
            waitingForConnect(name);
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
