package com.example.dell.filetransfer_bluetooth.bluetooth;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.dell.filetransfer_bluetooth.R;
import com.example.dell.filetransfer_bluetooth.utils.FileUtils;
import com.example.dell.filetransfer_bluetooth.utils.ReceiveDatabase;
import com.example.dell.filetransfer_bluetooth.utils.SendDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;

    private Handler handler;
    private Context context;
    Activity activity;
    public static final String IDentifier = "***abcdefghijklmnopqrstuvwxyz***";
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
        /*registerSendingBroadcastReceiver();
        registerAcceptedBroadcastReceiver();*/
        return new MyBinder();
    }

    public void setActivity(Activity activity){
        this.activity = activity;
    }
    @Override
    public boolean onUnbind(Intent intent){
        Log.i(TAG, "onUnbind()");
        disconnectToDevice();
        stopDiscovery();
        /*unRegisterAcceptedBroadcastReceiver();
        unRegisterSendingBroadcastReceiver();*/
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
    public void setContext(Context c){
        this.context=c;
    }

    public void setBluetoothAddressAndNameAndDevice(BluetoothDevice d){
        this.bluetoothAddress=d.getAddress();
        this.bluetoothName = d.getName();
        this.bluetoothDevice = d;
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
                waitingForConnect(device.getName(),device);
            }
        }
    }
    public void waitingForConnect(final String name, final BluetoothDevice device){
        try {
            final BluetoothServerSocket serverSocket
                    = bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, MY_UUID);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("TAG", "waiting for accept  "+name);
                    try {
                        bluetoothSocket = serverSocket.accept(100*1000);
                        handler.sendEmptyMessage(R.integer.change_buttonUnused);
                        new heartPackage().start();
                        bluetoothDevice = device;
                        bluetoothName=name;
                        //等待读取数据
                        new Thread(new BluetoothDataReadTask()).start();
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
                        //等待读取数据
                        new Thread(new BluetoothDataReadTask()).start();
                        new heartPackage().start();
                    } catch (IOException e) {
                        Log.e(TAG, "bluetoothSocket connect failed");
                    }
                }
            }).start();
        }
        else{
            Log.e(TAG, "bluetoothAddress==null.at connectToDevice()");
        }
    }


    public void disconnectToDevice(){
        Log.i(TAG,"disconnectToDevice()");
        if(bluetoothSocket!=null){
            if(bluetoothSocket.isConnected()){
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bluetoothSocket=null;
        }
    }

   /* public void readData(){
        //从蓝牙中读取数据
        if(handler!=null
                &&bluetoothSocket.isConnected()){
            bluetoothDataReadThread=new Thread(new BluetoothDataReadTask(bluetoothSocket,handler));
            bluetoothDataReadThread.start();
        }
        else{
            Log.e(TAG, "readData() error");
        }
    }*/



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

            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(name==null || deviceArrayList.contains(device)){
                return;
            }
            deviceArrayList.add(device);
            deviceNameList.add(name);
            arrayAdapter.notifyDataSetChanged();
            waitingForConnect(name,device);
        }
    };



    /**
     * 读取蓝牙数据的任务
     */
    private class BluetoothDataReadTask implements Runnable {

        @Override
        public void run() {
            InputStream dis = null;
            ReceiveDatabase rd = null;
            String sb = new String();
            try {
                Log.i(TAG, "get inputStream");

                dis = bluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ( bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    sb="";
                    while ((length = dis.read(buffer,0,buffer.length)) > 1)
                        sb+=(new String(buffer,0,length, "UTF-8"));
                    if(!sb.equals("")) {
                        Log.i(TAG,"getText "+sb);
                        if (!sb.startsWith(IDentifier)) {
                            rd = new ReceiveDatabase(context, "receive,db3", 2);
                            rd.setActivity(activity);
                            final ReceiveDatabase finalRd = rd;
                            final String finalSb = sb;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finalRd.insert(bluetoothName,
                                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                                            finalSb);
                                }
                            });

                            rd.close();
                        }
                        else{
                            Message msg = new Message();
                            msg.what = R.integer.chatlog_add;
                            Bundle b = new Bundle();
                            sb=sb.substring(IDentifier.length());
                            b.putString("chat",bluetoothName+": "+sb);
                            msg.setData(b);
                            handler.sendMessage(msg);
                        }
                    }
                    Thread.sleep(500);
                }

            } catch (IOException e) {
                Log.e(TAG, "DataInputStream is closed!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(rd!=null)
                    rd.close();
/*
                if (dis != null)
                    try {
                        dis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
            }

        }
    }


    /**
     * 发送蓝牙数据的任务
     */
    private class BluetoothDataWriteTask extends Thread {

        String s;

        BluetoothDataWriteTask() {
        }

        BluetoothDataWriteTask(String s) {
            this.s = s;
        }

        @Override
        public void run() {
            OutputStream dos = null;
            SendDatabase sd=null;
            try {
                if(bluetoothSocket!=null) {

                    dos = bluetoothSocket.getOutputStream();
                    byte[] buffer = s.getBytes();
                    dos.write(buffer);
                    dos.flush();
                    Log.i(TAG, "sending content: " + s);


                }
                sd = new SendDatabase(context, "send,db3", 2);
                sd.setActivity(activity);
                final SendDatabase finalSd = sd;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        finalSd.insert(bluetoothName,
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                                s);
                    }
                });
                sd.close();
            } catch (IOException e) {
                Log.e(TAG, "DataOutputStream is closed!");
            } finally {
                if(sd!=null)
                    sd.close();
                /*if (dos != null)
                    try {
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
            }

        }
    }

    private void sendErrorMessage(int what,String key,String value){
        Bundle bundle=new Bundle();
        bundle.putString(key, value);
        Message errorMessage=new Message();
        errorMessage.what=what;
        errorMessage.setData(bundle);
        handler.sendMessage(errorMessage);
    }


    /////
    public void startTransferFile(Uri uri,Context c){
        new DownTask(c).execute(uri);
    }
    /**
     * 异步任务，传输文件
     */
    class DownTask extends AsyncTask<Uri,Integer, String> {
        ProgressDialog pdialog;
        String fileName;
        Context context;
        DownTask(Context context){
            this.context = context;
        }

        @Override
        protected String doInBackground(Uri ...params) {

            Uri uri = params[0];

            String path = FileUtils.getPath(context, uri);
            if(path==null)
                path = "noName";
            fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
            Log.i(TAG, uri + " " + "fileName  " + fileName);


            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("*/*");
            if(Build.VERSION.SDK_INT < 11){
                sharingIntent.setPackage("com.android.bluetooth");
            }else{
                sharingIntent.setComponent(new ComponentName("com.android.bluetooth",
                        "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
            }
            sharingIntent.setPackage("com.android.bluetooth");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(sharingIntent);



/*
            try {
                DataOutputStream dos = new DataOutputStream(bluetoothSocket.getOutputStream());
                File file = new File("/storage/emulated/0/Pictures/Screenshots/Screenshot_2016-03-03-11-17-18-1.png");
                FileInputStream fis = new FileInputStream(file);
                byte []sendBytes = new byte[1024];
                int length,hasTranfer=0;
                while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                    dos.write(sendBytes, 0, length);
                    publishProgress(++hasTranfer);
                    Log.i(TAG,hasTranfer+" "+ + length);
                }
                dos.flush();Log.i(TAG,"Transfer complete");
                fis.close();
                dos.close();
            } catch (IOException e) {
                Log.e(TAG, "Transfer IOException");
                e.printStackTrace();
            }*/
            return null;
        }

       @Override
        protected  void onPostExecute(String result){
            new BluetoothDataWriteTask(fileName).start();
           // pdialog.dismiss();
        }
/*
        @Override
        protected  void onPreExecute(){
            pdialog = new ProgressDialog(context);
            pdialog.setTitle("传输中");
            pdialog.setMessage("waiting...");
            pdialog.setCancelable(false);
            pdialog.setMax(100);
            pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pdialog.setIndeterminate(false);
            pdialog.show();
        }

        @Override
        protected  void onProgressUpdate(Integer...value){
            pdialog.setProgress(value[0]);
        }*/
    }


    /**
     *
     * 心跳包，监听socket是否断开连接
     */
    class heartPackage extends Thread{

        @Override
        public void run() {
            boolean isConnected = true;
            while(isConnected && bluetoothSocket!= null) {

                try {
                    bluetoothSocket.getOutputStream().write(new byte[]{1});
                    sleep(1000);
                    //Log.i(TAG, "heartPackage is working..");
                } catch (IOException e) {
                    isConnected = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(handler != null)
                handler.sendEmptyMessage(R.integer.change_buttontext);
        }
    }

/*

    */
/**
     * 监听接受文件广播
     *//*

    private void registerAcceptedBroadcastReceiver() {
        accptedbroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG,"transfer file");
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(accptedbroadcastReceiver, filter);

    }
    private void unRegisterAcceptedBroadcastReceiver(){
        if(accptedbroadcastReceiver != null)
            unregisterReceiver(accptedbroadcastReceiver);
    }

    */
/**
     * 监听发送文件广播
     *//*

    private void registerSendingBroadcastReceiver() {
        sendedbroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int handoverStatus = intent.getIntExtra("android.btopp.intent.extra.BT_OPP_TRANSFER_STATUS",1);
                //0表示传输成功，1表示失败
                if (handoverStatus == 0) {
                    Log.i(TAG,"transfer successfully!");
                }
                else{
                    Log.i(TAG,"transfer rejected!");

                }
            }
        };
        IntentFilter filter = new IntentFilter("android.btopp.intent.action.BT_OPP_TRANSFER_DONE");
        registerReceiver(sendedbroadcastReceiver, filter);

    }
    private void unRegisterSendingBroadcastReceiver(){
        if(sendedbroadcastReceiver != null)
            unregisterReceiver(sendedbroadcastReceiver);
    }
*/

    //发送文字
    public void sendText(final String s){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream dos = null;
                SendDatabase sd=null;
                try {
                    if(bluetoothSocket!=null) {

                        dos = bluetoothSocket.getOutputStream();
                        byte[] buffer = (IDentifier+s).getBytes();
                        dos.write(buffer);
                        dos.flush();
                        Log.i(TAG, "sending userText: " + s);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "DataOutputStream is closed!");
                }
            }
        }).start();
    }
}
