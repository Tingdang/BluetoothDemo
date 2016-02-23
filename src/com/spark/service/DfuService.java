/*************************************************************************************************************************************************
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ************************************************************************************************************************************************/

package com.spark.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.spark.data.DbHelper;
import com.spark.service.aidl.AIDLActivity;
import com.spark.service.aidl.BatteryInfo;
import com.spark.service.aidl.Device;
import com.spark.service.aidl.DictDay;
import com.spark.service.aidl.IBleService;
import com.spark.service.aidl.NiceAlarm;
import com.spark.service.aidl.Radiation;
import com.spark.sleep.DataCenter;
import com.spark.util.Constant;
import com.spark.util.IW202BLEProtocol;
import com.spark.util.Trace;

public class DfuService extends Service {
	private static final String TAG = DfuService.class.getSimpleName();
	public static final String ACTION_TRY_CONNECT = "no.nordicsemi.android.dfu.broadcast.ACTION_TRY_CONNECT";
	public static final String ACTION_TRY_DISCONNECT = "no.nordicsemi.android.dfu.broadcast.ACTION_TRY_DISCONNECT";
	public static final String EXTRA_ACTION = "no.nordicsemi.android.dfu.extra.EXTRA_ACTION";
	public static final String BROADCAST_ACTION = "no.nordicsemi.android.dfu.broadcast.BROADCAST_ACTION";
	public static final String BROADCAST_STATE = "no.nordicsemi.android.dfu.broadcast.BROADCAST_STATE";
	public static final String EXTRA_DEVICE_ADDRESS = "no.nordicsemi.android.dfu.extra.EXTRA_DEVICE_ADDRESS";
	public static final String EXTRA_AUTO_RECONNECT = "no.nordicsemi.android.dfu.extra.EXTRA_AUTO_RECONNECT";
	private static final String BLUETOOTH_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";  
    private static final String BLUETOOTH_ACTION = "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED";	
	private static final UUID SERVICE_UUID = new UUID(0x0000fff000001000l, 0x800000805f9b34fbl);
	private static final UUID WRITE_UUID = new UUID(0x0000fff100001000l, 0x800000805f9b34fbl);
	private static final UUID READ_UUID = new UUID(0x0000fff200001000l, 0x800000805f9b34fbl);
	private static final UUID CLIENT_CHARACTERISTIC_CONFIG = new UUID(0x0000290200001000l, 0x800000805f9b34fbl);

	private static final int ERROR_MASK = 0x1000;
	private static final int ERROR_SERVICE_DISCOVERY_NOT_STARTED = ERROR_MASK | 0x05;
	private static final int ERROR_CONNECTION_MASK = 0x4000;
	private static final int ERROR_CONNECTION_STATE_MASK = 0x8000;
	public static final int ACTION_ABORT = 2;
	public final static int STATE_DISCONNECTED = 0;
	public final static int STATE_CONNECTING = -1;
	public final static int STATE_CONNECTED = -2;
	public final static int STATE_CONNECTED_AND_READY = -3; // indicates that services were discovered
	public final static int STATE_DISCONNECTING = -4;
	public final static int STATE_CLOSED = -5;
	
	private final Object mLock = new Object();
	private BluetoothAdapter mBluetoothAdapter;
	protected BluetoothGatt gatt;
	private MyBinder myBinder;
	private String mDeviceAddress;	
	private boolean mAborted;
	private int mConnectionState;
	private int mError;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Trace.d(TAG, "onCreate");
		initialize();
		final IntentFilter actionFilter = makeDfuActionIntentFilter();
		registerReceiver(mDfuActionReceiver, actionFilter);		
		final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		registerReceiver(mConnectionStateBroadcastReceiver, filter);
		final IntentFilter bondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(mBondStateBroadcastReceiver, bondFilter);
		final IntentFilter conneFilter = new IntentFilter();
		conneFilter.addAction(BLUETOOTH_STATE_CHANGED);  
		conneFilter.addAction(BLUETOOTH_ACTION);
		registerReceiver(mConnecteBroadcastReceiver, conneFilter);
        myBinder = new MyBinder(this);
	}

	private final BroadcastReceiver mConnecteBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();  
            if (BLUETOOTH_STATE_CHANGED.equals(action) || BLUETOOTH_ACTION.equals(action))  
            {  
            	if(null != mBluetoothAdapter){
	            	switch (mBluetoothAdapter.getState())   
	                {  
		                case BluetoothAdapter.STATE_ON:  
		                    break;  
		                case BluetoothAdapter.STATE_TURNING_ON:  
		                    break;  
		                case BluetoothAdapter.STATE_OFF:  
		                    break;  
		                case BluetoothAdapter.STATE_TURNING_OFF:  
		        			if(null != myBinder){
		        				try {
									myBinder.disconnect();
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		        			}		                	
		                    break;  
	                }   	            		
            	}
            }  
		}
	};		
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mDfuActionReceiver);		
		unregisterReceiver(mConnectionStateBroadcastReceiver);
		unregisterReceiver(mBondStateBroadcastReceiver);
		unregisterReceiver(mConnecteBroadcastReceiver);  
	}
	
	private static IntentFilter makeDfuActionIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DfuService.BROADCAST_ACTION);
		return intentFilter;
	}	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Trace.d(TAG, "onBind");
		return myBinder;
	}	
	
	private final BroadcastReceiver mDfuActionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final int action = intent.getIntExtra(EXTRA_ACTION, 0);

			switch (action) {
				case ACTION_ABORT:
					mAborted = true;

					// Notify waiting thread
					synchronized (mLock) {
						mLock.notifyAll();
					}
					break;
			}
		}
	};	

	
	private final BroadcastReceiver mConnectionStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// Obtain the device and check it this is the one that we are connected to
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (!device.getAddress().equals(mDeviceAddress))
				return;

			final String action = intent.getAction();

			Trace.d(TAG, "Action received: " + action);
			mConnectionState = STATE_DISCONNECTED;		
			sendConnectionStateBroadcast();
			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}
	};

	private final BroadcastReceiver mBondStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// Obtain the device and check it this is the one that we are connected to
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (!device.getAddress().equals(mDeviceAddress))
				return;

			// Read bond state
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			if (bondState == BluetoothDevice.BOND_BONDING)
				return;

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}
	};

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			// Check whether an error occurred
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothGatt.STATE_CONNECTED) {
					Trace.i(TAG, "Connected to GATT server");
					mConnectionState = STATE_CONNECTED;
					sendConnectionStateBroadcast();
					
					try {
						synchronized (this) {
							Trace.d(TAG, "Waiting 1600 ms for a possible Service Changed indication...");
							wait(600);
						}
					} catch (InterruptedException e) {
						// Do nothing
					}	
					
					if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
						try {
							synchronized (this) {
								Trace.d(TAG, "Waiting 1600 ms for a possible Service Changed indication...");
								wait(1000);
							}
						} catch (InterruptedException e) {
							// Do nothing
						}
					}

					final boolean success = gatt.discoverServices();
					Trace.i(TAG, "Attempting to start service discovery... " + (success ? "succeed" : "failed"));

					if (!success) {
						mError = ERROR_SERVICE_DISCOVERY_NOT_STARTED;
					} else {
						return;
					}
				} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
					Trace.e(TAG, "Disconnected from GATT server");
					mConnectionState = STATE_DISCONNECTED;
					sendConnectionStateBroadcast();
				}
			} else {
				Trace.e(TAG, "Connection state change error: " + status + " newState: " + newState);
				mError = ERROR_CONNECTION_STATE_MASK | status;
			}

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}
		
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
			if (status != BluetoothGatt.GATT_SUCCESS) {
				Trace.e(TAG, "ReliableWrite Completed error: " + status);
				mError = ERROR_CONNECTION_MASK | status;
			}

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
        }	
        
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				Trace.e(TAG, "RemoteRssi read error: " + status);
				mError = ERROR_CONNECTION_MASK | status;
			}else if(null != myBinder){
				try {
					myBinder.getActivityCallback().RealRssi(rssi);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
        }        
        
        
		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Trace.i(TAG, "Services discovered");
				mConnectionState = STATE_CONNECTED_AND_READY;
				sendConnectionStateBroadcast();
			} else {
				Trace.e(TAG, "Service discovery error: " + status);
				mError = ERROR_CONNECTION_MASK | status;
			}

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}

		@Override
		public void onDescriptorRead(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				Trace.e(TAG, "Descriptor read error: " + status);
				mError = ERROR_CONNECTION_MASK | status;
			}

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}

		@Override
		public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			if (status != BluetoothGatt.GATT_SUCCESS){
				Trace.e(TAG, "Descriptor write error: " + status);
				mError = ERROR_CONNECTION_MASK | status;
			}

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}
		
		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			if (status != BluetoothGatt.GATT_SUCCESS){
				Trace.e(TAG, "Characteristic write error: " + status);
				mError = ERROR_CONNECTION_MASK | status;
			}

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}

		@Override
		public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			if (status != BluetoothGatt.GATT_SUCCESS)  {
				Trace.e(TAG, "Characteristic read error: " + status);
				mError = ERROR_CONNECTION_MASK | status;
			}

			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}

		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            IW202BLEProtocol.getInstance().mergePackage(myBinder, data);
			// Notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}
	};

	private boolean initialize() {
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if (bluetoothManager == null) {
			Trace.e(TAG, "Unable to initialize BluetoothManager.");
			return false;
		}

		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Trace.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}
	
	private void sendConnectionStateBroadcast() {
		final Intent broadcast = new Intent(BROADCAST_STATE);
		broadcast.putExtra(BROADCAST_STATE, mConnectionState);
		sendBroadcast(broadcast);
	}	

    public class MyBinder extends IBleService.Stub {
    	private AIDLActivity callback;
    	private Context context;
    	private DictDay realData;
    	private BatteryInfo realBattery;
    	private Radiation realRadiation;  
    	private int updatePercent = -1;
    	private SharedPreferences s;
    	public DbHelper<?> dbHelper;
    	public DataCenter center;
    	
    	public MyBinder(Context context) {
			// TODO Auto-generated constructor stub
    		this.context = context;
    		s = context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE);
    		dbHelper = new DbHelper(context);
    		center = new DataCenter(context);
		}
  	
    	
		@Override
		public boolean send(byte[] byte_send) throws RemoteException {
			// TODO Auto-generated method stub
			boolean retval = false;
			if(null == byte_send || byte_send.length == 0){
				return true;
			}
			
        	if(null != gatt){
        		BluetoothGattService bluetoothGattService = gatt.getService(SERVICE_UUID);
        		if(null != bluetoothGattService){
        			BluetoothGattCharacteristic writeCharacteristic = bluetoothGattService.getCharacteristic(WRITE_UUID);
        			if(null != writeCharacteristic){
        				gatt.setCharacteristicNotification(writeCharacteristic, true);        				
        				retval = writeCharacteristic.setValue(byte_send);
        				if(retval){
        					retval = gatt.writeCharacteristic(writeCharacteristic);  
        				}
        			}
        		}
        	}
        	
            return retval;			
		}    	

		@Override
		public boolean connect(String address) throws RemoteException {
			// TODO Auto-generated method stub
			Trace.i(TAG, "connect");
			boolean retval = false;
			mAborted = false;
			mDeviceAddress = address;
			mConnectionState = STATE_DISCONNECTED;
			mError = 0;
			
			if(null != gatt){
				disconnect();
				gatt = null;
			}

    		if (!mBluetoothAdapter.isEnabled()){
    			return retval;
    		}

    		mConnectionState = STATE_CONNECTING;
    		sendConnectionStateBroadcast();
    		Trace.i(TAG, "Connecting to the device...");
    		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
    		gatt = device.connectGatt(context, false, mGattCallback);
    		try {
    			synchronized (mLock) {
    				while (((mConnectionState == STATE_CONNECTING || mConnectionState == STATE_CONNECTED) && mError == 0 && !mAborted))
    					mLock.wait();
    			}
    		} catch (final InterruptedException e) {
    			Trace.e(TAG, "Sleeping interrupted", e);
    		}			
			
			if (gatt == null) {
				Trace.e(TAG, "Bluetooth adapter disabled");
				return retval;
			}
			if (mError > 0) { // error occurred
				final int error = mError & ~ERROR_CONNECTION_STATE_MASK;
				Trace.e(TAG, "An error occurred while connecting to the device:" + error);
				disconnect();
				return retval;
			}
			
			if (mAborted) {
				Trace.i(TAG, "Upload aborted");
				disconnect();
				return retval;
			}

			List<BluetoothGattService> list = gatt.getServices();
			if(null != list && list.size() > 0){
				BluetoothGattService tempService;
				for(int i = 0; i < list.size(); i++){
					tempService = list.get(i);
					Trace.e(TAG, "No."+i+"find UUID:" + tempService.getUuid().toString());
				}
			}else{
				Trace.e(TAG, "service does not exists on the device");
				disconnect();
				return retval;				
			}

			return true;
		}
		
		@Override
		public boolean localeInit()throws RemoteException{
			boolean retval = false;
			
        	if(null != gatt){
    			final BluetoothGattService dfuService = gatt.getService(SERVICE_UUID); // there was a case when the service was null. I don't know why
    			if (dfuService != null) {
    				final BluetoothGattCharacteristic writeCharacteristic = dfuService.getCharacteristic(WRITE_UUID);
    				final BluetoothGattCharacteristic readCharacteristic = dfuService.getCharacteristic(READ_UUID);
    				if (writeCharacteristic == null || readCharacteristic == null) {
    					return retval;
    				}
    				
    				retval = gatt.setCharacteristicNotification(readCharacteristic, true);
    				Trace.i(TAG, "setCharacteristicNotification(read) = " + retval);	
    		        BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
    		        if (descriptor != null) {
    		        	retval = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    		            Trace.i(TAG, "descriptor.setValue = " + retval);	
    		            retval = gatt.writeDescriptor(descriptor);
    		            Trace.i(TAG, "gatt.writeDescriptor = " + retval);
    		        }
    		        Trace.i(TAG, "find writeCharacteristic && readCharacteristic");	
    			}else{
    				return retval;				
    			}
        	}
            return retval;				
		}
		
		@Override
		public boolean readRssi()throws RemoteException{
			boolean retval = false;
			
        	if(null != gatt){
        		if(mConnectionState == STATE_CONNECTED_AND_READY){
        			retval = gatt.readRemoteRssi();
        		}
        	}
            return retval;	
		}
		
		@Override
		public void disconnect() throws RemoteException {
			// TODO Auto-generated method stub
			Trace.i(TAG, "disconnect");
			if (mConnectionState != STATE_DISCONNECTED) {
	    		if (mConnectionState == STATE_DISCONNECTED)
	    			return;
	    		mConnectionState = STATE_DISCONNECTING;
	    		sendConnectionStateBroadcast();
	    		Trace.i(TAG, "Disconnecting from the device...");
	    		if(null != gatt){
	    			gatt.disconnect();
	    		}

	    		try {
					synchronized (mLock) {
						while (mConnectionState != STATE_DISCONNECTED && mError == 0)
							mLock.wait();
					}
				} catch (final InterruptedException e) {
					Trace.e(TAG, "Sleeping interrupted", e);
				}
			}

			if(null != gatt){
				if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE) {
					try {
						final Method refresh = gatt.getClass().getMethod("refresh");
						if (refresh != null) {
							final boolean success = (Boolean) refresh.invoke(gatt);
							Trace.i(TAG, "Refreshing result: " + success);
						}
					} catch (Exception e) {
						Trace.e(TAG, "An exception occurred while refreshing device", e);
					}
				}
				Trace.i(TAG, "Cleaning up...");
				gatt.close();				
			}

			mConnectionState = STATE_CLOSED;
			gatt = null;
			sendConnectionStateBroadcast();
		}


		@Override
		public void registerCallBack(AIDLActivity callback)  {
			// TODO Auto-generated method stub
			Trace.i(TAG, "registerCallBack");
			this.callback = callback;
		}

		@Override
		public BatteryInfo getRealBatteryInfo()  {
			// TODO Auto-generated method stub
			Trace.i(TAG, "getRealBatteryInfo");
			return realBattery;
		}

		@Override
		public DictDay getRealDictDay()  {
			// TODO Auto-generated method stub
			Trace.i(TAG, "getRealDictDay");
			return realData;
		}

		@Override
		public Radiation getRealRadiation()  {
			// TODO Auto-generated method stub
			Trace.i(TAG, "getRealRadiation");
			return realRadiation;
		}	
		
		@Override
		public int getUpdatePercent() {
			// TODO Auto-generated method stub
			Trace.i(TAG, "getUpdatePercent");
			return updatePercent;
		}
		
		@Override
		public int getConnectionState(){
			Trace.i(TAG, "getConnectionState");
			return mConnectionState;
		}
		
		
		public void setRealBatteryInfo(BatteryInfo realBattery)  {
			// TODO Auto-generated method stub
			Trace.i(TAG, "setRealBatteryInfo");
			this.realBattery = realBattery;
		}

		public void setRealDictDay(DictDay realData)  {
			// TODO Auto-generated method stub
			Trace.i(TAG, "setRealDictDay");
			this.realData = realData;
		}

		public void setRealRadiation(Radiation realRadiation)  {
			// TODO Auto-generated method stub
			Trace.i(TAG, "setRealRadiation");
			this.realRadiation = realRadiation;
		}	
		
		public AIDLActivity getActivityCallback() {
			// TODO Auto-generated method stub
			return callback;
		}
		
		public void setUpdatePercent(int updatePercent){
			Trace.i(TAG, "setUpdatePercent");
			this.updatePercent = updatePercent;
		}
		
		public boolean synchronizationSuccess() throws RemoteException {
	    	Trace.i(TAG, "synchronizationSuccess");
	        return send(Constant.writeByte((byte) 0x90, new byte[]{2, 1}));
		}
		
		public boolean synchronizationFailure() throws RemoteException {
	        Trace.i(TAG, "synchronizationFailure");
	        return send(Constant.writeByte((byte) 0x90, new byte[]{2, 0}));
		}	
	    
	    private Object getAttribute(String key) {
	        if (!StringUtils.isEmpty(key) && StringUtils.equals(Constant.CURRENT_ACCOUNT, key)) {
	            return context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).getString(Constant.CURRENT_ACCOUNT, "");
	        } else {
	            String account = context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).getString(Constant.CURRENT_ACCOUNT, "");
	            if (!StringUtils.isEmpty(account)) key += account;
	            return s.getAll().get(key);
	        }
	    }	   

	    @Override
	    public void clear() {
	        s.edit().clear().commit();
	    }
	    
	    @Override
	    public String getStr(String key) {
	        String a = (String) getAttribute(key);
	        return a == null ? "" : a;
	    }
	    
	    @Override
	    public  void setStr(String key, String value) {
	        if (!StringUtils.isEmpty(key) && StringUtils.equals(Constant.CURRENT_ACCOUNT, key)) {
	            context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).edit().putString(Constant.CURRENT_ACCOUNT, (String) value).commit();
	        } else {
	            String account = context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).getString(Constant.CURRENT_ACCOUNT, "");
	            if (!StringUtils.isEmpty(account)) key += account;
	            s.edit().putString(key, value).commit();
	        }	    	
	    }	    
	    
	    @Override
	    public int getInt(String key) {
	        Integer integer = (Integer) getAttribute(key);
	        return integer == null ? 0 : integer;
	    }

	    @Override
	    public void setInt(String key, int value) {
            String account = context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).getString(Constant.CURRENT_ACCOUNT, "");
            if (!StringUtils.isEmpty(account)) key += account;
            s.edit().putInt(key, value).commit();    	
	    }	    
	    
	    @Override
	    public boolean getBool(String key) {
	        Boolean integer = (Boolean) getAttribute(key);
	        return null == integer ? false : integer;
	    }

	    @Override
	    public void setBool(String key, boolean value) {
            String account = context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).getString(Constant.CURRENT_ACCOUNT, "");
            if (!StringUtils.isEmpty(account)) key += account;	    	
	        s.edit().putBoolean(key, value).commit();
	    }
	    
	    @Override
	    public long getLong(String key) {
	        Long integer = (Long) getAttribute(key);
	        return integer == null ? 0 : integer;
	    }

	    @Override
	    public void setLong(String key, long value) {
            String account = context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).getString(Constant.CURRENT_ACCOUNT, "");
            if (!StringUtils.isEmpty(account)) key += account;	    	
	        s.edit().putLong(key, value).commit();
	    }

	    @Override
	    public void setFloat(String key, float value) {
            String account = context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).getString(Constant.CURRENT_ACCOUNT, "");
            if (!StringUtils.isEmpty(account)) key += account;	    	
	        s.edit().putFloat(key, value).commit();
	    }

		@Override
		public float getFloat(String key) throws RemoteException {
			// TODO Auto-generated method stub
			Float integer = (Float) getAttribute(key);
	        return integer == null ? 0f : integer;
		}

	    @Override
	    public void setByte(String key, byte value) {
            String account = context.getSharedPreferences(Constant.CURRENT_USER, Context.MODE_PRIVATE).getString(Constant.CURRENT_ACCOUNT, "");
            if (!StringUtils.isEmpty(account)) key += account;	    	
	        s.edit().putInt(key, value).commit();
	    }

		@Override
		public byte getByte(String key) throws RemoteException {
			// TODO Auto-generated method stub
			Byte integer = (Byte) getAttribute(key);
	        return integer == null ? 0 : integer;
		}

		@Override
		public Device dbgetDevice() throws RemoteException {
			// TODO Auto-generated method stub
			Device device = null;
			String account = getStr(Constant.CURRENT_ACCOUNT);
			String address = getStr(Constant.CURRENT_ADDRESS);
			try {
				device = dbHelper.getDevice(account, address);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return device;
		}
		
		@Override
		public NiceAlarm getNiceAlarm(int number) throws RemoteException {
			// TODO Auto-generated method stub
			NiceAlarm niceAlarms = null;
			String account = getStr(Constant.CURRENT_ACCOUNT);
			String address = getStr(Constant.CURRENT_ADDRESS);
			try {
				niceAlarms = dbHelper.getNiceAlarm(account, address, number);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return niceAlarms;
		}	
		
		@Override
		public void updateNiceAlarm(NiceAlarm niceAlarm) throws RemoteException{
			try {
				dbHelper.updateNiceAlarm(niceAlarm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		
		@Override
		public void resetSleepDataDataBase(){
			center.dbUtil.deleteDB();
			center.Clear();		
		}
	}  
}
