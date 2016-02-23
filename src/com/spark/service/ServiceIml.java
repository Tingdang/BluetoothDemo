package com.spark.service;

import java.util.Calendar;
import java.util.Set;

import com.spark.MessageUtil;
import com.spark.service.aidl.AIDLActivity;
import com.spark.service.aidl.BatteryInfo;
import com.spark.service.aidl.DictDay;
import com.spark.service.aidl.IBleService;
import com.spark.service.aidl.NiceAlarm;
import com.spark.service.aidl.Radiation;
import com.spark.util.Constant;
import com.spark.util.Trace;
import com.spark.util.scanListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

public class ServiceIml {
	private static final String TAG = ServiceIml.class.getSimpleName();
    private BluetoothManager iBluetoothManager;
    private BluetoothGatt iBluetoothGatt;   
    private static ServiceIml serviceIml;
    private static IBleService mService;
    private CallAfterStartService iCallAfterStartService;
	private scanListener iScanListener;
	private boolean isScanning;
	
	private AIDLActivity mCallback = new AIDLActivity.Stub() {

		@Override
		public void RealBatteryInfo(BatteryInfo para) throws RemoteException {
			// TODO Auto-generated method stub
            Message message = new Message();
            message.what = MessageUtil.BATTERYINFO;
            message.obj = para;			
			MessageUtil.sendMessage(message);		
		}

		@Override
		public void RealDictDay(DictDay para) throws RemoteException {
			// TODO Auto-generated method stub
            Message message = new Message();
            message.what = MessageUtil.ACTION_REAL_TIME_STEP_DATA;
            message.obj = para;			
			MessageUtil.sendMessage(message);	
		}

		@Override
		public void RealRadiation(Radiation para) throws RemoteException {
			// TODO Auto-generated method stub
            Message message = new Message();
            message.what = MessageUtil.RADIATION_DATA;
            message.obj = para;			
			MessageUtil.sendMessage(message);	
		}

		@Override
		public void RealUpdatePercent(int para, boolean isSuccess) throws RemoteException {
			// TODO Auto-generated method stub
            Message message = new Message();
            message.what = MessageUtil.UPDATE_BACK;
            message.obj = isSuccess;
            message.arg1 = para;		
			MessageUtil.sendMessage(message);			
		}

		@Override
		public void RealMessage(int para) throws RemoteException {
			// TODO Auto-generated method stub
            Message message = new Message();
            message.what = para;	
			MessageUtil.sendMessage(message);					
		}

		@Override
		public void RealNiceAlarm(NiceAlarm para) throws RemoteException {
			// TODO Auto-generated method stub
            Message message = new Message();
            message.what = MessageUtil.NICEALARM_DATA;
            message.obj = para;	
			MessageUtil.sendMessage(message);				
		}

		@Override
		public void RealRssi(int Rssi) throws RemoteException {
			// TODO Auto-generated method stub
            Message message = new Message();
            message.what = MessageUtil.RSSI_DATA;
            message.arg1 = Rssi;	
			MessageUtil.sendMessage(message);				
		}
	};	    
    
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Trace.e(TAG, "connect service");
			mService = IBleService.Stub.asInterface(service);
			try {
				mService.registerCallBack(mCallback);
			} catch (RemoteException e) {

			}			
			if(null != iCallAfterStartService){
				iCallAfterStartService.runAfterStartService(mService);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			Trace.e(TAG, "disconnect service");
			mService = null;
		}
	};
	
    private ServiceIml() {
    	isScanning = false;
    }

    public static ServiceIml getInstance() {
        if (serviceIml == null) {
            serviceIml = new ServiceIml();
        }
        return serviceIml;
    }    

    public static IBleService getServiceBinder(){
    	return mService;
    }
    
    public void startDfuService(Context context){
		Bundle args = new Bundle();  
        Intent intent = new Intent(context, DfuService.class);  
        intent.putExtras(args);  
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);  
        context.startService(intent);  	    	
    }
    
    public void stopDfuService(Context context){
    	context.unbindService(mConnection);
    }
    
    public void setCallAfterStartService(CallAfterStartService iCallAfterStartService){
    	this.iCallAfterStartService = iCallAfterStartService;
    }
    
	public boolean synchronizationTime() throws RemoteException {
		// TODO Auto-generated method stub
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        byte[] data = new byte[]{1, (byte) year, (byte) (year >> 8),
                (byte) month, (byte) day, (byte) hour, (byte) minute,
                (byte) second};
        Trace.i(TAG, "synchronizationTime");
        return mService.send(Constant.writeByte((byte) 0x84, data));
	}

	public boolean synchronization() throws RemoteException {
		// TODO Auto-generated method stub
        byte[] data = new byte[]{1};
        Trace.i(TAG, "synchronization");
        return mService.send(Constant.writeByte((byte) 0x90, data));
	}

	public boolean getDeviceInfo() throws RemoteException {
		// TODO Auto-generated method stub
        byte[] data = new byte[]{1};
        Trace.i(TAG, "getDeviceInfo");
        return mService.send(Constant.writeByte((byte) 0xF0, data));
	}

	public boolean getDeviceV() throws RemoteException {
		// TODO Auto-generated method stub
        byte[] data = new byte[]{2};
        Trace.i(TAG, "getDeviceV");
        return mService.send(Constant.writeByte((byte) 0xF0, data));
	}

	public boolean getNiceAlarm() throws RemoteException {
		// TODO Auto-generated method stub
        byte[] data = new byte[]{(byte) 4};
        Trace.i(TAG, "getNiceAlarm");
        return mService.send(Constant.writeByte((byte) 0x84, data));
	}

	public boolean setTarget(int step) throws RemoteException {
		// TODO Auto-generated method stub
    	byte[] data = new byte[]{2, 1, 
        		(byte)(step&0xFF),
        		(byte)((step>>8)&0xFF), 
        		(byte)((step>>16)&0xFF),
                (byte)((step>>24)&0xFF)};
        Trace.i(TAG, "setTarget");
        return mService.send(Constant.writeByte((byte) 0x81, data));
	}

	public boolean readRealTimeStep() throws RemoteException {
		// TODO Auto-generated method stub
		byte[] data = new byte[]{3};
		Trace.i(TAG, "readRealTimeStep");
		return mService.send(Constant.writeByte((byte) 0x81, data));
	}

	public boolean enableRadiation(int enable) throws RemoteException {
		// TODO Auto-generated method stub
    	byte[] data = new byte[]{2, (byte) enable};
        Trace.i(TAG, "enableRadiation:" + enable);
        return mService.send(Constant.writeByte((byte) 0x91, data));
	}

	public boolean enableAlarm(byte[] by) throws RemoteException {
		// TODO Auto-generated method stub
    	Trace.i(TAG, "enableAlarm");
        return mService.send(Constant.writeByte((byte) 0x84, by));
	}


	public boolean rebootForUpdate() throws RemoteException {
		// TODO Auto-generated method stub
    	Trace.i(TAG, "rebootForUpdate");
    	return mService.send(new byte[]{(byte) 0xF2,0x03,0x01,0x00,0x0A});
	}

	public boolean requestBind() throws RemoteException {
		// TODO Auto-generated method stub
    	Trace.i(TAG, "requestBind");
        return mService.send(Constant.writeByte((byte) 0xE0, new byte[]{2}));
	}

	public boolean queryBatteryInfo() throws RemoteException {
		// TODO Auto-generated method stub
    	Trace.i(TAG, "queryBatteryInfo");
        return mService.send(Constant.writeByte((byte) 0xF1, new byte[]{2}));
	}

	public boolean resetDevice() throws RemoteException {
		// TODO Auto-generated method stub
    	Trace.i(TAG, "resetDevice");
        return mService.send(Constant.writeByte((byte) 0xB0, new byte[]{4}));
	}

	public boolean setSedentaryInfo(byte[] info) throws RemoteException {
		// TODO Auto-generated method stub
    	Trace.i(TAG, "setSedentaryInfo");
        return mService.send(Constant.writeByte((byte) 0xB0, info));
	}

	public boolean querySedentaryInfo() throws RemoteException {
		// TODO Auto-generated method stub
    	Trace.i(TAG, "querySedentaryInfo");
        return mService.send(Constant.writeByte((byte) 0xB0, new byte[]{0x0a}));
	}

	public boolean enableFetalMovement(byte enable) throws RemoteException {
		// TODO Auto-generated method stub
    	Trace.i(TAG, "enableFetalMovement");
        return mService.send(Constant.writeByte((byte) 0xA0, new byte[]{1, enable}));
	}

	public boolean setRadiationMode(byte mode) throws RemoteException {
		// TODO Auto-generated method stub
		Trace.i(TAG, "setRadiationMode mode = " + mode);
		return mService.send(Constant.writeByte((byte) 0x92, new byte[]{2, mode}));
	}    

//    public BluetoothDevice getBluetoothDevice() {
//        return bluetoothGatt.getConnectedDevices().get(0);
//    }

//  public List<BluetoothGattService> getSupportedGattServices() {
//      if (bluetoothGatt == null)
//          return null;
//
//      return bluetoothGatt.getServices();
//  }
	
    public void setBluetoothManager(BluetoothManager bluetoothManager) {
    	iBluetoothManager = bluetoothManager;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
    	iBluetoothGatt = bluetoothGatt;
    }

    public BluetoothGatt getBluetoothGatt() {
        return iBluetoothGatt;
    }

    public BluetoothManager getBluetoothManager() {
        return iBluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return (null != getBluetoothManager()) ? getBluetoothManager()
                .getAdapter() : null;
    }
    
    private BluetoothAdapter mBluetoothAdapter;
    
    public Boolean isBLEEnabled() {
		mBluetoothAdapter = getBluetoothAdapter();
		if(null == mBluetoothAdapter){
			return null;
		}
		
		return mBluetoothAdapter.isEnabled();
	}

    public void disableBLE() {
    	if(isBLEEnabled() == true){
    		mBluetoothAdapter.disable();
    	}
	}

    public void enbleBLE() {
    	if(isBLEEnabled() == false){
    		mBluetoothAdapter.enable();
    	}    	
	}	
    
    public void stopLeScan(BluetoothAdapter.LeScanCallback mLEScanCallback){
		if (isScanning) {
			mBluetoothAdapter = getBluetoothAdapter();
			if(null == mBluetoothAdapter){
				return;
			}			
			mBluetoothAdapter.stopLeScan(mLEScanCallback); 
			isScanning = false;
    		if(null != iScanListener){
    			iScanListener.stop();
    		}			
		}    	
    }
    
    public void startLeScan(BluetoothAdapter.LeScanCallback mLEScanCallback){
    	if(isBLEEnabled() && isScanning == false){
    		mBluetoothAdapter = getBluetoothAdapter();
			if(null == mBluetoothAdapter){
				return;
			}	    		
    		mBluetoothAdapter.startLeScan(mLEScanCallback);  
    		isScanning = true;
    		if(null != iScanListener){
    			iScanListener.start();
    		}
    	}
    }  
    
    public Set<BluetoothDevice> getBondedDevices(){
    	mBluetoothAdapter = getBluetoothAdapter();
		if(null == mBluetoothAdapter){
			return null;
		}	    	
    	return mBluetoothAdapter.getBondedDevices();
    }
    
    public boolean isScanning(){
    	return isScanning;
    }
    
    public void setScanListener(scanListener iScanListener){
    	this.iScanListener = iScanListener;
    }
}
