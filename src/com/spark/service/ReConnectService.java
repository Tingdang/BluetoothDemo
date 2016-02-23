package com.spark.service;

import org.apache.commons.lang3.StringUtils;

import com.spark.MessageUtil;
import com.spark.activity.DeviceSearchActivity;
import com.spark.service.aidl.IBleService;
import com.spark.util.Constant;
import com.spark.util.Trace;
import com.spark.util.Util;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

public class ReConnectService extends Service{
	private static final String TAG = ReConnectService.class.getSimpleName();
	private static final long LOOPER_START_DELAY_TIME = 100;
	private static Typeface typeface;
	private ServiceIml serviceIml;
	private IBleService mBleService;
	private Context context;
	private String address;
	private boolean isStopService,isInRange;
	private Handler handler = new Handler();
	private int intervals = 0;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

    private boolean isCanTry(){
    	if(!Util.isTopActivity(ReConnectService.this)){
    		Trace.d(TAG, "isTopActivity false");
    		return false;
    	}
    	
		if(null == serviceIml){
			Trace.d(TAG, "serviceIml null");
    		return false;			
		}    	
    	
		if(null == mBleService){
			Trace.d(TAG, "mBleService null");
			return false;
		}    	
		
    	try {
			address = mBleService.getStr(Constant.CURRENT_ADDRESS);
			intervals = mBleService.getInt(Constant.DEST_INTERVALS);
			if(StringUtils.isEmpty(address)){
				Trace.d(TAG, "address empty");
				return false;
			}
			
			if(intervals <= 0){
				Trace.d(TAG, "intervals = " + intervals);
				return false;
			}				
			
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			int connectionState = mBleService.getConnectionState();
	    	if(connectionState != DfuService.STATE_DISCONNECTED && connectionState != DfuService.STATE_CLOSED){
	    		Trace.i(TAG, "connectionState not STATE_DISCONNECTED or STATE_CLOSED");
	    		return false;
	    	} 			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		if(serviceIml.isScanning()){
			Trace.i(TAG, "isScanning true");
			return false;
		} 
		
		if(isInRange == false){
			Trace.i(TAG, "isInRange false");
			return false;
		} 		
		return true;
    }		
	
	
	private Runnable runnable = new Runnable() {
		public void run() {
			if(isStopService){
				return;
			}
			if(isCanTry()){
	    		try {
	    			Trace.i(TAG, "start connecting...");
	    			address = mBleService.getStr(Constant.CURRENT_ADDRESS);
            		if(!StringUtils.isEmpty(address)){
            			mBleService.connect(address);
            		}	                    
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			handler.postDelayed(this, intervals*1000); 
		}
	}; 		
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		isStopService = false;
		context = ReConnectService.this;
		serviceIml = ServiceIml.getInstance();
        typeface = Typeface.createFromAsset(getAssets(),
				"fonts/trebucbd.ttf");  		
		final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		serviceIml.setBluetoothManager(manager);
		serviceIml.setCallAfterStartService(new CallAfterStartService(){
			@Override
			public void runAfterStartService(IBleService mService) {
				// TODO Auto-generated method stub
				mBleService = mService;
				try {
					mService.setStr(Constant.CURRENT_ACCOUNT, "15220405520");
					mService.setStr(Constant.PWD,"7890");
					intervals = mBleService.getInt(Constant.DEST_INTERVALS);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            Message message = new Message();
	            message.what = MessageUtil.PROCESS_RUN;
				MessageUtil.sendMessage(message);						
			}});
		serviceIml.startDfuService(context);
		handler.postDelayed(runnable,LOOPER_START_DELAY_TIME);
		registerReceiver(mDfuUpdateReceiver, makeDfuUpdateIntentFilter());
	}
	
	public static Typeface getTypeface(){
		return typeface;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		isStopService = true;
		serviceIml.stopDfuService(this);
		unregisterReceiver(mDfuUpdateReceiver);
		super.onDestroy();
	}
	
	private final BroadcastReceiver mDfuUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// DFU is in progress or an error occurred
			final String action = intent.getAction();
			if (DfuService.BROADCAST_STATE.equals(action)) {
				int state = intent.getIntExtra(DfuService.BROADCAST_STATE, 1);
				switch (state) {
				case DfuService.STATE_DISCONNECTED: {
					Trace.i(TAG, "status:DISCONNECTED");
					isInRange = false;
					startScan();
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
					isInRange = false;
					startScan();
				}
					break;
				case DfuService.STATE_CLOSED: {
					Trace.i(TAG, "status:CLOSED");
					isInRange = false;
					startScan();
				}
					break;
				}	
			}
		}
	};	
	
	
	private static IntentFilter makeDfuUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DfuService.BROADCAST_STATE);
		return intentFilter;
	}	
	
	private final BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				final byte[] scanRecord) {
			if (device != null){
				if(serviceIml.isScanning()){
					if (device.getAddress().equals(address)) {
						Trace.i(TAG, "find device return");
						isInRange = true;
						stopLeScan();
					}				
				}				
			}
		}
	};
	
	private void startScan() {
    	if(Util.isForeground(ReConnectService.this,DeviceSearchActivity.class.getName())){
    		Trace.i(TAG, "1");
    		return;
    	}		
    	serviceIml.startLeScan(mLEScanCallback);
	}
	
	private void stopLeScan() {
		serviceIml.stopLeScan(mLEScanCallback);
	}	
}
