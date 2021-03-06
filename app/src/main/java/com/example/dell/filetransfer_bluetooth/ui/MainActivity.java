package com.example.dell.filetransfer_bluetooth.ui;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TabWidget;

import com.example.dell.filetransfer_bluetooth.R;

public class MainActivity extends BaseTabActivity {

    private String TAG="MainActivity";

    private TabWidget mTabWidget;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_main);
        initViews();
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG, "onPause()");

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    protected TabWidget getTabWidget() {
        return mTabWidget;
    }

    @Override
    protected ViewPager getViewPager() {
        return mViewPager;
    }

    protected void initViews(){
        mTabWidget = (TabWidget) findViewById(R.id.tabWidget_bodyfit);
        mViewPager = (ViewPager) findViewById(R.id.viewpager_bodyfit);
        mTabWidget.setStripEnabled(false);

        Fragment targetFragment=new SendRecordFragment();
        Fragment exerciseFragment=new TransfereFragment();
        Fragment profileFragment=new ReceiveRecordFragment();

        LayoutInflater inflater=getLayoutInflater();
        Resources resources=getResources();

        //使用setImageResource就不会变形，用setBackgroundResource就会
        ImageView targetTab=(ImageView)inflater.inflate(R.layout.layout_tab,mTabWidget,false);
        targetTab.setImageResource(R.drawable.send_seletor);

        ImageView exerciseTab=(ImageView)inflater.inflate(R.layout.layout_tab,mTabWidget,false);
        exerciseTab.setImageResource(R.drawable.transfer_seletor);

        ImageView profileTab=(ImageView)inflater.inflate(R.layout.layout_tab,mTabWidget,false);
        profileTab.setImageResource(R.drawable.receive_seletor);

        addTab(targetTab, targetFragment, "TARGET");
        addTab(exerciseTab,exerciseFragment,"EXERCISE");
        addTab(profileTab, profileFragment, "PROFILE");

        setCurrentTab(1);
    }

}
