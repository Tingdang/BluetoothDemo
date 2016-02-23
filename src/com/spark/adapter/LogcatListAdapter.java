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
package com.spark.adapter;

import java.util.ArrayList;
import java.util.List;

import com.spark.bluetoothdemo.R;
import com.spark.service.aidl.LogcatInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * DeviceListAdapter class is list adapter for showing scanned Devices name,
 * address and RSSI image based on RSSI values.
 */
public class LogcatListAdapter extends BaseAdapter {
	final List<LogcatInfo> mListLogcatValues = new ArrayList<LogcatInfo>();
	final LayoutInflater inflater;
	
	public LogcatListAdapter(Context context) {
		// TODO Auto-generated constructor stub
		inflater = LayoutInflater.from(context);
	}
	
	public void addItem(LogcatInfo logcat){
		mListLogcatValues.add(0, logcat);
		notifyDataSetChanged();
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListLogcatValues.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListLogcatValues.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.logcat_list_row, parent, false);
			holder = new ViewHolder();
			holder.content = (TextView) convertView;
			convertView.setTag(holder);
		}else{	
			holder = (ViewHolder) convertView.getTag();
		}
		LogcatInfo item = (LogcatInfo) getItem(position);
		holder.content.setText(item.getText());
		holder.content.setTextColor(item.getColor());
		return convertView;
	}
	
	private class ViewHolder {
		private TextView content;
	}	
}
