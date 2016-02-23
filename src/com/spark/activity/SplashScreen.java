/*
 * Copyright (c) 2015. Guangdong Spark Technology Co.,Ltd.
 *     Author: WENFUMAN
 *     Email: hehufuman@163.com
 *     Mobile phone: +86 18033432336
 */

package com.spark.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.spark.MessageUtil;
import com.spark.bluetoothdemo.R;
import com.spark.service.ReConnectService;
import com.spark.util.DisplayUtil;


public class SplashScreen extends Activity {
    public static final String TAG = SplashScreen.class.getSimpleName();
    private ImageView iv_onlogin;
    private Animation operatingAnim;
    private MyHandler handler;
    private Context context;
    
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	stopAnimation();
    	super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.splash_screen);  
    	DisplayUtil.initSystemBar(this);
    	isBLESupported();	
        getViewId();
        init();
    }
    
    private void startAnimation(){
    	if (operatingAnim != null) {  
    		iv_onlogin.startAnimation(operatingAnim); 
    		iv_onlogin.setVisibility(View.VISIBLE);
    	}     	
    }
    
    private void stopAnimation(){
    	iv_onlogin.clearAnimation();  
    	iv_onlogin.setVisibility(View.INVISIBLE); 
    }    


    public void getViewId() {
    	iv_onlogin = (ImageView)findViewById(R.id.iv_onlogin);
    }


	public void init() {
		// TODO Auto-generated method stub
		startAnimation();
		context = SplashScreen.this;
		handler = new MyHandler(this); 
		MessageUtil.setCurHandler(handler);
        Intent intent = new Intent(context, ReConnectService.class);
		startService(intent); 	
	}
	
	private void isBLESupported() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.no_ble, Toast.LENGTH_SHORT).show();
			Intent newIntent = new Intent(); 
			setResult(RESULT_OK, newIntent);			
			finish();
		}
	}	
	
    private static class MyHandler extends Handler {
        private WeakReference<SplashScreen> activityWeakReference;//=new WeakReference<Activity>();

        public MyHandler(SplashScreen activity) {
            this.activityWeakReference = new WeakReference<SplashScreen>(activity);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplashScreen activity = activityWeakReference.get();
            if (null != activity && !activity.isFinishing()) {
                switch (msg.what) {	 
                case MessageUtil.PROCESS_RUN:{
                	activity.goWhere();
                }break;
                }
            }
        }
    }
    
    
    private void goWhere(){
    	startActivity(new Intent(context, DeviceSearchActivity.class));
    	stopAnimation();
    	finish();   	
    }
}
