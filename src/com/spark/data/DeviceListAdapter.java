/*
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
 */
package com.spark.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.spark.activity.DeviceSearchActivity;
import com.spark.bluetoothdemo.R;
import com.spark.data.ExtendedBluetoothDevice;
import com.spark.service.DfuService;
import com.spark.service.aidl.IBleService;
import com.spark.util.process;

/**
 * DeviceListAdapter class is list adapter for showing scanned Devices name,
 * address and RSSI image based on RSSI values.
 */
public class DeviceListAdapter extends BaseAdapter {
//	private static final String TAG = DeviceListAdapter.class.getSimpleName();
	private static final int TYPE_TITLE = 0;
	private static final int TYPE_ITEM = 1;
	private static final int TYPE_EMPTY = 2;

	private final ArrayList<ExtendedBluetoothDevice> mListBondedValues = new ArrayList<ExtendedBluetoothDevice>();
	private final ArrayList<ExtendedBluetoothDevice> mListValues = new ArrayList<ExtendedBluetoothDevice>();
	private final ExtendedBluetoothDevice.AddressComparator comparator = new ExtendedBluetoothDevice.AddressComparator();
	private final Context mContext;
	private HashMap<String, Boolean> SelStates = new HashMap<String, Boolean>();
	private String curSel,connectSel;
	private int selColor;
	private boolean isAutoReConnect;
	private IBleService mService;
	private process iprocess;
	
	public DeviceListAdapter(Context context, IBleService service) {
		curSel = null;
		connectSel = null;
		isAutoReConnect = false;
		this.mService = service;
		this.mContext = context;
		selColor = mContext.getResources().getColor(R.color.secondFace);
		freshAutoReconnect();
	}

	public boolean isClickConnectedDevice(int position){
		Object ob = getItem(position);
		if(null == ob || !(ob instanceof ExtendedBluetoothDevice)){
			return false;
		}
		ExtendedBluetoothDevice d = (ExtendedBluetoothDevice)ob;
		if(d.device.getAddress().equals(connectSel)){
			return true;
		}
		return false;
	}
	
	public void setProcess(process iprocess){
		this.iprocess = iprocess;
	}
	
    public void setConnectedDevice(){
    	connectSel = curSel;
    	notifyDataSetChanged();
    }
    
    public void setDisConnectedDevice(){
    	connectSel = null;
    	notifyDataSetChanged();
    }	
	
    public void setAutoReconnect(boolean isAutoReConnect){
    	this.isAutoReConnect = isAutoReConnect;
    	 freshAutoReconnect();
    }
    
    private void freshAutoReconnect(){
    	int type;
    	ExtendedBluetoothDevice device;
    	for(int i = 0; i < getCount(); i++){
    		type = getItemViewType(i);
    		if(type == TYPE_ITEM){
    			device = (ExtendedBluetoothDevice) getItem(i);
    			device.setAutoReconnect(isAutoReConnect);
    		}
    	}
    	
    	if(isAutoReConnect == true && curSel != null){
    		if(!curSel.equals(connectSel)){
				final ExtendedBluetoothDevice d = (ExtendedBluetoothDevice)getItem(curSel);
				if(null != d){
					final Intent service = new Intent(mContext, DfuService.class);
					service.setAction(DfuService.ACTION_TRY_CONNECT);
					service.putExtra(DfuService.EXTRA_DEVICE_ADDRESS, d.device.getAddress());
					service.putExtra(DfuService.EXTRA_AUTO_RECONNECT, isAutoReConnect);
					mContext.startService(service);	 					
				}
    		}
    	}
    }
    
    
	public boolean addBondedDevice(ExtendedBluetoothDevice device) {
		if (null != device) {
			mListBondedValues.add(device);
			notifyDataSetChanged();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Looks for the device with the same address as given one in the list of
	 * bonded devices. If the device has been found it updates its RSSI value.
	 * 
	 * @param address
	 *            the device address
	 * @param rssi
	 *            the RSSI of the scanned device
	 */
	public void updateRssiOfBondedDevice(String address, int rssi) {
		comparator.address = address;
		final int indexInBonded = mListBondedValues.indexOf(comparator);
		if (indexInBonded >= 0) {
			ExtendedBluetoothDevice previousDevice = mListBondedValues
					.get(indexInBonded);
			previousDevice.rssi = rssi;
			notifyDataSetChanged();
		}
	}

	/**
	 * If such device exists on the bonded device list, this method does
	 * nothing. If not then the device is updated (rssi value) or added.
	 * 
	 * @param device
	 *            the device to be added or updated
	 */
	public boolean addOrUpdateDevice(ExtendedBluetoothDevice device) {
		final boolean indexInBonded = mListBondedValues.contains(device);
		if (indexInBonded) {
			return false;
		}

		final int indexInNotBonded = mListValues.indexOf(device);
		if (indexInNotBonded >= 0) {
			ExtendedBluetoothDevice previousDevice = mListValues
					.get(indexInNotBonded);
			previousDevice.rssi = device.rssi;
			notifyDataSetChanged();
			return true;
		}
		mListValues.add(device);
		notifyDataSetChanged();
		return true;
	}

	public void clearDevices() {
		mListValues.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		final int bondedCount = mListBondedValues.size() + 1; // 1 for the title
		final int availableCount = mListValues.isEmpty() ? 2 : mListValues
				.size() + 1; // 1 for title, 1 for empty text
		if (bondedCount == 1)
			return availableCount;
		return bondedCount + availableCount;
	}

	
	public Object getItem(String address) {
		comparator.address = address;
		final int indexInNotBonded = mListValues.indexOf(comparator);
		if (indexInNotBonded >= 0) {
			return mListBondedValues.get(indexInNotBonded);
		}else{		
			return null;
		}	
	}
	
	
	@Override
	public Object getItem(int position) {
		final int bondedCount = mListBondedValues.size() + 1; // 1 for the title
		if (mListBondedValues.isEmpty()) {
			if (position == 0)
				return R.string.scanner_subtitle__not_bonded;
			else
				return mListValues.get(position - 1);
		} else {
			if (position == 0)
				return R.string.scanner_subtitle_bonded;
			if (position < bondedCount)
				return mListBondedValues.get(position - 1);
			if (position == bondedCount)
				return R.string.scanner_subtitle__not_bonded;
			return mListValues.get(position - bondedCount - 1);
		}
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == TYPE_ITEM;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0)
			return TYPE_TITLE;

		if (!mListBondedValues.isEmpty()
				&& position == mListBondedValues.size() + 1)
			return TYPE_TITLE;

		if (position == getCount() - 1 && mListValues.isEmpty())
			return TYPE_EMPTY;

		return TYPE_ITEM;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View oldView, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		final int type = getItemViewType(position);

		View view = oldView;
		switch (type) {
		case TYPE_EMPTY:
			if (view == null) {
				view = inflater.inflate(R.layout.device_list_empty, parent,
						false);	
			}
			break;
		case TYPE_TITLE:
			if (view == null) {
				view = inflater.inflate(R.layout.device_list_title, parent,
						false);
			}
			final TextView title = (TextView) view;
			title.setTextColor(Color.BLACK);
			title.setText((Integer) getItem(position));
			break;
		default:
			if (view == null) {
				view = inflater
						.inflate(R.layout.device_list_row, parent, false);
				final ViewHolder holder = new ViewHolder();
				holder.name = (TextView) view.findViewById(R.id.name);
				holder.address = (TextView) view.findViewById(R.id.address);
				holder.rssi = (ImageView) view.findViewById(R.id.rssi);
				holder.status = (ImageView) view.findViewById(R.id.status);
				holder.rb_state = (CheckBox) view
						.findViewById(R.id.rb_light);
				view.setTag(holder);
			}
			final ExtendedBluetoothDevice bledevice = (ExtendedBluetoothDevice) getItem(position);
			final String curAddress = bledevice.device.getAddress();
			final ViewHolder holder = (ViewHolder) view.getTag();
			final String name = bledevice.name;
			holder.name.setText(name != null ? name : mContext
					.getString(R.string.not_available));
			holder.address.setText(curAddress);
			if (!bledevice.isBonded || bledevice.rssi != DeviceSearchActivity.NO_RSSI) {
				final int rssiPercent = (int) (100.0f * (127.0f + bledevice.rssi) / (127.0f + 20.0f));
				holder.rssi.setImageLevel(rssiPercent);
				holder.rssi.setVisibility(View.VISIBLE);
			} else {
				holder.rssi.setVisibility(View.GONE);
			}

			if(curAddress.equals(connectSel)){
				holder.status.setBackgroundResource(R.drawable.connected);
				holder.name.setTextColor(selColor);
				holder.address.setTextColor(selColor);
			}else{
				holder.status.setBackgroundResource(R.drawable.disconnect);
				holder.name.setTextColor(Color.BLACK);
				holder.address.setTextColor(Color.BLACK);
			}			
			
			holder.rb_state.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					// 重置，确保最多只有一项被选中
					for (String key : SelStates.keySet()) {
						SelStates.put(key, false);
					}
					
					if(curAddress.equals(curSel)){
						holder.rb_state.setChecked(false);
						curSel = null; 
						SelStates.put(String.valueOf(position),
								false);
						
						if(curAddress.equals(connectSel)){
							if(null != iprocess){
								iprocess.show();
							}
							try {
								mService.disconnect();
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}							
						}
					}else{
						if(!StringUtils.isEmpty(connectSel)){
							connectSel = null;
						}
						holder.rb_state.setChecked(true);
						curSel = curAddress; 
						SelStates.put(String.valueOf(position),
								true);
						if(null != iprocess){
							iprocess.show();
						}
				        new Thread() {
				            @Override
				            public void run() {
				                try {
									try {
										mService.connect(curSel);
									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
				                } catch (Exception e) {
				                    e.printStackTrace();
				                }
				            }
				        }.start();						
					}
					notifyDataSetChanged();
				}
			});

			if (SelStates.get(String.valueOf(position)) == null
					|| SelStates.get(String.valueOf(position)) == false) {
				SelStates.put(String.valueOf(position), false);
				holder.rb_state.setChecked(false);
			} else {
				holder.rb_state.setChecked(true);
			}

			break;
		}

		return view;
	}
	
	private class ViewHolder {
		private ImageView status;
		private TextView name;
		private TextView address;
		private ImageView rssi;
		private CheckBox rb_state;
	}
}
