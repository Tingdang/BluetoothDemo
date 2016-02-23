package com.spark.activity;

import java.util.Set;

import com.spark.UtilActivity;
import com.spark.adapter.DeviceListAdapter;
import com.spark.bluetoothdemo.R;
import com.spark.data.ExtendedBluetoothDevice;
import com.spark.service.DfuService;
import com.spark.service.ReConnectService;
import com.spark.service.ScannerServiceParser;
import com.spark.util.DisplayUtil;
import com.spark.util.Trace;
import com.spark.util.process;
import com.spark.util.scanListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class DeviceSearchActivity extends UtilActivity{
	private final static int SCAN_DURATION = 5000;
	private static final boolean DEVICE_IS_BONDED = true;
	private final static boolean DEVICE_NOT_BONDED = false;
	public static final int NO_RSSI = -1000;
	private ListView listView;
	private DeviceListAdapter mAdapter;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devicesearch);
		DisplayUtil.initSystemBar(this);
		getViewId();
		init();
		setViewListener();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		super.init();
		context = DeviceSearchActivity.this;
		TAG = DeviceSearchActivity.class.getSimpleName();
		handler = new Handler();
		mAdapter = new DeviceListAdapter(context, mBleService);
		initProcessDialog();
        mAdapter.setProcess(new process(){

			@Override
			public void show() {
				// TODO Auto-generated method stub
				showProcessDialog();
		}});		
		listView.setAdapter(mAdapter);
		addBondedDevices();
		titleLay.setTitleSize(DisplayUtil.calYdpBy1920(context, 8));
		serviceIml.startLeScan(mLEScanCallback);	
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		serviceIml.setScanListener(new scanListener(){

			@Override
			public void start() {
				// TODO Auto-generated method stub
				titleLay.startAnimation();
				titleLay.setRightContent(R.string.stopScan);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						serviceIml.stopLeScan(mLEScanCallback);
					}
				}, SCAN_DURATION);				
			}

			@Override
			public void stop() {
				// TODO Auto-generated method stub
				titleLay.stopAnimation();
				titleLay.setRightContent(R.string.scan);				
			}

			@Override
			public void result() {
				// TODO Auto-generated method stub
				
			}});
		
		try {
			setStatus(mBleService.getConnectionState());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	
		serviceIml.enbleBLE();
	}

    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	serviceIml.setScanListener(null);
    }
	
	

	@Override
	public void getViewId() {
		// TODO Auto-generated method stub
		super.getViewId();
		listView = (ListView) findViewById(android.R.id.list);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		back(null);
	}

	@Override
	public void back(View v) {
		if(isProcessDialogOnShow()){
			Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
			abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
			sendBroadcast(abortAction);		
			closeProcessDialog();
			return;
		}
		if (serviceIml.isScanning()) {
			serviceIml.stopLeScan(mLEScanCallback);
			return;
		}
		
		serviceIml.disableBLE();
        Intent intent = new Intent(context, ReConnectService.class);
		stopService(intent); 	
		super.back(v);
	}

	@Override
	public void setViewListener() {
		// TODO Auto-generated method stub
		titleLay.setClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				back(v);
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (serviceIml.isScanning()) {
					serviceIml.stopLeScan(mLEScanCallback);
				} else {
					serviceIml.startLeScan(mLEScanCallback);
				}
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(mAdapter.isClickConnectedDevice(position)){
					ExtendedBluetoothDevice d = (ExtendedBluetoothDevice)mAdapter.getItem(position);
					try {
						if(!mBleService.localeInit()){
							showToast(R.string.nonedevice);
						}else{
							Intent intent = new Intent(context, FunctionActivity.class);
							intent.putExtra("name", d.name);
							intent.putExtra("address", d.device.getAddress());
							startActivity(intent);						
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					showToast(R.string.noconnect);
				}
			}});		
	}
	
	private void showToast(final int messageResId) {
		Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
	}
	
	private final BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				final byte[] scanRecord) {
			if (device != null) {
				updateScannedDevice(device, rssi);
				try {
					if (ScannerServiceParser.decodeDeviceAdvData(scanRecord,
							null, false)) {
						// On some devices device.getName() is always null. We
						// have to parse the name manually :(
						// This bug has been found on Sony Xperia Z1 (C6903)
						// with Android 4.3.
						// https://devzone.nordicsemi.com/index.php/cannot-see-device-name-in-sony-z1
						addScannedDevice(device,
								ScannerServiceParser
										.decodeDeviceName(scanRecord), rssi,
								DEVICE_NOT_BONDED);
					} else {
						Trace.e(TAG, "decodeDeviceAdvData false");
					}
				} catch (Exception e) {
					Trace.e(TAG,
							"Invalid data in Advertisement packet "
									+ e.toString());
				}
			} else {
				Trace.e(TAG, "device null");
			}
		}
	};

	/**
	 * if scanned device already in the list then update it otherwise add as a
	 * new device.
	 */
	private void updateScannedDevice(final BluetoothDevice device,
			final int rssi) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAdapter.updateRssiOfBondedDevice(device.getAddress(), rssi);
			}
		});
	}

	/**
	 * if scanned device already in the list then update it otherwise add as a
	 * new device
	 */
	private void addScannedDevice(final BluetoothDevice device,
			final String name, final int rssi, final boolean isBonded) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Trace.e(TAG, "addOrUpdateDevice :" + device.getAddress());
				mAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(device,
						name, rssi, isBonded));
			}
		});
	}

	private void addBondedDevices() {
		final Set<BluetoothDevice> devices = serviceIml.getBondedDevices();
		for (BluetoothDevice device : devices) {
			mAdapter.addBondedDevice(new ExtendedBluetoothDevice(device, device
					.getName(), NO_RSSI, DEVICE_IS_BONDED));
		}
	}

	@Override
	public void setStatus(int state){
		switch (state) {
		case DfuService.STATE_DISCONNECTED: {
			mAdapter.setDisConnectedDevice();
			closeProcessDialog();
		}
			break;
		case DfuService.STATE_CONNECTING: {
			showProcessDialog();
		}
			break;

		case DfuService.STATE_CONNECTED_AND_READY: {
			mAdapter.setConnectedDevice();
			closeProcessDialog();
		}
			break;
		case DfuService.STATE_DISCONNECTING: {
			showProcessDialog();
		}
			break;
		case DfuService.STATE_CLOSED: {
			closeProcessDialog();
		}
			break;
		}		
	}	


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
}
