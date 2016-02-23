package com.spark;

import com.spark.bluetoothdemo.R;
import com.spark.service.DfuService;
import com.spark.service.ServiceIml;
import com.spark.service.aidl.IBleService;
import com.spark.util.Trace;
import com.spark.widget.TitleViewLayout;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public abstract class UtilActivity extends FragmentActivity implements OnClickListener, IUtilView {
	protected String TAG;
	protected TitleViewLayout titleLay;
	protected IBleService mBleService;
	protected ServiceIml serviceIml;
	protected Context context;
	protected String address,account;
	protected Dialog progressDialog;
	private ImageView iv_onlogin;
	private Animation operatingAnim;
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		serviceIml = ServiceIml.getInstance();
		mBleService = ServiceIml.getServiceBinder();
	}
	
	@Override
	public void getViewId() {
		// TODO Auto-generated method stub
		titleLay = (TitleViewLayout) findViewById(R.id.titleLay);
	}

	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		registerReceiver(mDfuUpdateReceiver, makeDfuUpdateIntentFilter());
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(mDfuUpdateReceiver);
	}
	
	
	private static IntentFilter makeDfuUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DfuService.BROADCAST_STATE);
		return intentFilter;
	}	
	
	private final BroadcastReceiver mDfuUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// DFU is in progress or an error occurred
			final String action = intent.getAction();
			if (DfuService.BROADCAST_STATE.equals(action)) {
				int state = intent.getIntExtra(DfuService.BROADCAST_STATE, 1);
				setStatus(state);
			}
		}
	};


	@Override
	public void back(View v) {
		// TODO Auto-generated method stub
		finish();
	}
	
	
	@Override
	public void setStatus(int state){
		switch (state) {
		case DfuService.STATE_DISCONNECTED: {
			Trace.i(TAG, "status:DISCONNECTED");
		}
			break;
		case DfuService.STATE_CONNECTING: {
			Trace.i(TAG, "status:CONNECTING");
		}
			break;
		case DfuService.STATE_CONNECTED: {
			Trace.i(TAG, "status:CONNECTED");
		}
			break;
		case DfuService.STATE_CONNECTED_AND_READY: {
			Trace.i(TAG, "status:CONNECTED_AND_READY");
			
		}
			break;
		case DfuService.STATE_DISCONNECTING: {
			Trace.i(TAG, "status:DISCONNECTING");
		}
			break;
		case DfuService.STATE_CLOSED: {
			Trace.i(TAG, "status:CLOSED");
		}
			break;
		}		
	}
	
	protected void initProcessDialog() {
		progressDialog = new Dialog(this, R.style.mystyle);
		progressDialog.getWindow().setGravity(Gravity.CENTER);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);		
		progressDialog.setContentView(getLayoutInflater().inflate(
				R.layout.dialog_process, (ViewGroup) null));
		iv_onlogin = (ImageView) (progressDialog.findViewById(R.id.iv_onlogin));
        operatingAnim = AnimationUtils.loadAnimation(context, R.anim.tryconnect);  
        LinearInterpolator lin = new LinearInterpolator();  
        operatingAnim.setInterpolator(lin); 
	}	

	
	protected void closeProcessDialog(){
		if(null != iv_onlogin){
			iv_onlogin.clearAnimation();  
		}
		if(null != progressDialog && progressDialog.isShowing()){
			progressDialog.dismiss();
		}
	}	
	
	protected void showProcessDialog(){
		if(!isProcessDialogOnShow()){
			iv_onlogin.startAnimation(operatingAnim);
			progressDialog.show();			
		}
	}	
	
	protected boolean isProcessDialogOnShow(){
		if(null != progressDialog && progressDialog.isShowing()){
			return true;
		}else{
			return false;
		}
	}
}
