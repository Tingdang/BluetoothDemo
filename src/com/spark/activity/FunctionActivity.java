package com.spark.activity;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

import net.simonvt.numberpicker.NumberPicker;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.spark.MessageUtil;
import com.spark.UtilActivity;
import com.spark.adapter.LogcatListAdapter;
import com.spark.bluetoothdemo.R;
import com.spark.data.SedentaryInfo;
import com.spark.percent.PercentLinearLayout;
import com.spark.percent.PercentRelativeLayout;
import com.spark.service.DfuService;
import com.spark.service.aidl.BatteryInfo;
import com.spark.service.aidl.Device;
import com.spark.service.aidl.DictDay;
import com.spark.service.aidl.LogcatInfo;
import com.spark.service.aidl.NiceAlarm;
import com.spark.service.aidl.Radiation;
import com.spark.util.Constant;
import com.spark.util.CountDownUtils;
import com.spark.util.DisplayUtil;
import com.spark.util.Trace;
import com.spark.widget.CusFntButton;
import com.spark.widget.CustomDialog;
import com.zcw.togglebutton.ToggleButton;
import com.zcw.togglebutton.ToggleButton.OnToggleChanged;

public class FunctionActivity extends UtilActivity {
	private static final int INTERVALS_ITEMS = 11;
	private static final int STEPS_ITEMS = 30;
	private static final int MINUTE_ITEMS = 61;
	private static final int MSG_READ_RSSI = 1314;
	private CusFntButton synchronizationtime_bt;
	private CircularProgressButton synchronization_bt;
	private TextView tv_battery, tv_radiation, tv_steps, tv_rssi, tv_destSteps,
			tv_intervals, tv_deviceinfo, tv_timeperiod, tv_starttime,
			tv_overtime, tv_time, tv_period, tv_fetalmovement;
	private PercentRelativeLayout lay_deviceinfo, lay_destSteps, lay_intervals,
			lay_timeperiod, lay_starttime, lay_overtime, laypowerSaving,
			lay_time, lay_period, lay_awake;
	private ToggleButton powerSavingSwitch, enableOrDisableRadiation,
			enableOrDisableFetalMovement, enableOrDisableSedentary,
			enableOrDisableAlarm, enableOrDisableAwake;
	private CheckBox monday, tuesday, wednesday, thursday, friday, saturday,
			sunday;
	private PercentLinearLayout layconstant;
	private ScrollView mCustomScrollView;
	private View view_divider0, view_divider1, view_divider2, view_divider3;
	private ListView resultlist;
	private TextView tv_nodevice;
	private Dialog dialog;
	private NumberPicker numPicker, hours, min, AMOrPM;
	private DecimalFormat decimalFormat;
	private MyHandler handler;
	private boolean isConnnect, isUpdate, isReadRssi;
	private SedentaryInfo iSedentaryInfo;
	private int editTimeType;
	private String unit_step, unit_minute, unit_second, unit_dBm;
	private String[] steps, minutes, seconds;
	private LogcatListAdapter adapter;
	private NiceAlarm niceAlarms;
	private CountDownUtils iCountDownUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_function);
		DisplayUtil.initSystemBar(this);
		getViewId();
		init();
		setViewListener();
		loadData();
		displayData();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		super.init();
		isUpdate = false;
		isConnnect = true;
		editTimeType = -1;
		context = FunctionActivity.this;
		TAG = FunctionActivity.class.getSimpleName();
		handler = new MyHandler(this);
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		address = intent.getStringExtra("address");
		titleLay.setTitleSize(DisplayUtil.calYdpBy1920(context, 4));
		titleLay.setTitle("name:" + name + "\naddress:" + address);
		titleLay.setRightContent(R.string.disconnect);
		decimalFormat = new DecimalFormat();
		try {
			mBleService.setStr(Constant.CURRENT_ADDRESS, address);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		unit_dBm = getString(R.string.unit_dBm);
		unit_step = getString(R.string.unit_step);
		unit_minute = getString(R.string.unit_minute);
		unit_second = getString(R.string.unit_second);

		adapter = new LogcatListAdapter(context);
		resultlist.setAdapter(adapter);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		initSedentary();
		initDestSteps();
		initIntervals();		
	}

	
	
	
	private int getTargetSteps() {
		int content = 10000;
		try {
			content = mBleService.getInt(Constant.DEST_STEPS);
			if (content < Constant.MIN_TARGET_STEP
					|| content % Constant.MIN_TARGET_STEP != 0
					|| content > Constant.MAX_TARGET_STEP) {
				content = Constant.DEFAULT_TARGET_STEP;
				mBleService.setInt(Constant.DEST_STEPS,
						Constant.DEFAULT_TARGET_STEP);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return content;
	}

	private void initDestSteps() {
		int steps = getTargetSteps();
		tv_destSteps.setText(steps + unit_step);
	}

	private void initIntervals() {
		int intervals = 0;
		try {
			intervals = mBleService.getInt(Constant.DEST_INTERVALS);
			if (intervals == 0) {
				tv_intervals.setText(R.string.close_auto);
			} else {
				if (intervals < 0 || intervals > Constant.MAX_TARGET_INTERVALS) {
					intervals = 1;
					mBleService.setInt(Constant.DEST_INTERVALS, intervals);
				}
				tv_intervals.setText(intervals + unit_second);
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		try {
			int state = mBleService.getConnectionState();
			setStatus(state);
			if (state == DfuService.STATE_CONNECTED_AND_READY) {
				displayDataStep(mBleService.getRealDictDay());
				displayBleBattery(mBleService.getRealBatteryInfo());
				dispRadiationData(mBleService.getRealRadiation());
			} else {
				showMainInterface(false);
			}
			boolean enableRadiation = mBleService
					.getBool(Constant.ENABLE_OR_DISABLE_RADIATION);
			if (enableRadiation) {
				enableOrDisableRadiation.setToggleOn();
				laypowerSaving.setVisibility(View.VISIBLE);
				view_divider1.setVisibility(View.VISIBLE);
				boolean enablePowerSaving = mBleService
						.getBool(Constant.POWER_SAVING);
				if (enablePowerSaving) {
					powerSavingSwitch.setToggleOn();
				} else {
					powerSavingSwitch.setToggleOff();
				}
			} else {
				enableOrDisableRadiation.setToggleOff();
				laypowerSaving.setVisibility(View.GONE);
				view_divider1.setVisibility(View.GONE);
			}
			long lastTime = mBleService.getLong(Constant.ENABLE_FETALMOVEMENT);
			long currentTime = (new Date()).getTime();
			Trace.e(TAG, "onResume lastTime = " + lastTime + ", currentTime = " + currentTime);
			if (currentTime - lastTime >= 3600000L) {
				Trace.e(TAG, "onResume pass time ");
				enableOrDisableFetalMovement.setToggleOff();
				mBleService.setBool(Constant.ENABLE_OR_DISABLE_FETALMOVEMENT,
						false);
			} else {
				enableRadiation = mBleService
						.getBool(Constant.ENABLE_OR_DISABLE_FETALMOVEMENT);
				if (enableRadiation) {
					Trace.e(TAG, "onResume enable ");
					enableOrDisableFetalMovement.setToggleOn();
					if (null == iCountDownUtils) {
						Trace.e(TAG, "onResume new start");
						long leftTime = 3600000L - (currentTime - lastTime);
						iCountDownUtils = new CountDownUtils(leftTime, 1000, tv_fetalmovement);
						iCountDownUtils.start();
					}else{
						Trace.e(TAG, "onResume old start");
					}
				} else {
					Trace.e(TAG, "onResume disable ");
					enableOrDisableFetalMovement.setToggleOff();
					if (null != iCountDownUtils) {
						Trace.e(TAG, "onResume cancel ");
						iCountDownUtils.cancel();
					}
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		displayVersionInfo();
		MessageUtil.setCurHandler(handler);
		isReadRssi = true;
		new Thread(new RssiThread()).start();  		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MessageUtil.setCurHandler(null);
		isReadRssi = false;
	}

	@Override
	public void getViewId() {
		// TODO Auto-generated method stub
		super.getViewId();
		tv_nodevice = (TextView) findViewById(R.id.tv_nodevice);
		resultlist = (ListView) findViewById(R.id.resultlist);
		layconstant = (PercentLinearLayout) findViewById(R.id.layconstant);
		mCustomScrollView = (ScrollView) findViewById(R.id.mCustomScrollView);
		view_divider0 = (View) findViewById(R.id.view_divider0);
		view_divider1 = (View) findViewById(R.id.view_divider1);
		view_divider2 = (View) findViewById(R.id.view_divider2);
		view_divider3 = (View) findViewById(R.id.view_divider3);
		
		tv_rssi = (TextView) findViewById(R.id.tv_rssi);
		tv_time = (TextView) findViewById(R.id.tv_time);
		tv_steps = (TextView) findViewById(R.id.tv_steps);
		tv_period = (TextView) findViewById(R.id.tv_period);
		tv_battery = (TextView) findViewById(R.id.tv_battery);
		tv_overtime = (TextView) findViewById(R.id.tv_overtime);
		tv_radiation = (TextView) findViewById(R.id.tv_radiation);
		tv_intervals = (TextView) findViewById(R.id.tv_intervals);
		tv_destSteps = (TextView) findViewById(R.id.tv_destSteps);
		tv_starttime = (TextView) findViewById(R.id.tv_starttime);
		tv_deviceinfo = (TextView) findViewById(R.id.tv_deviceinfo);
		tv_timeperiod = (TextView) findViewById(R.id.tv_timeperiod);
		tv_fetalmovement = (TextView) findViewById(R.id.tv_fetalmovement);
		synchronization_bt = (CircularProgressButton) findViewById(R.id.synchronization_bt);
		synchronizationtime_bt = (CusFntButton) findViewById(R.id.synchronizationtime_bt);

		lay_time = (PercentRelativeLayout) findViewById(R.id.lay_time);
		lay_awake = (PercentRelativeLayout) findViewById(R.id.lay_awake);
		lay_period = (PercentRelativeLayout) findViewById(R.id.lay_period);
		lay_overtime = (PercentRelativeLayout) findViewById(R.id.lay_overtime);
		lay_intervals = (PercentRelativeLayout) findViewById(R.id.lay_intervals);
		lay_destSteps = (PercentRelativeLayout) findViewById(R.id.lay_destSteps);
		lay_starttime = (PercentRelativeLayout) findViewById(R.id.lay_starttime);
		lay_deviceinfo = (PercentRelativeLayout) findViewById(R.id.lay_deviceinfo);
		lay_timeperiod = (PercentRelativeLayout) findViewById(R.id.lay_timeperiod);
		laypowerSaving = (PercentRelativeLayout) findViewById(R.id.laypowerSaving);

		powerSavingSwitch = (ToggleButton) findViewById(R.id.powerSavingSwitch);
		enableOrDisableAwake = (ToggleButton) findViewById(R.id.enableOrDisableAwake);
		enableOrDisableAlarm = (ToggleButton) findViewById(R.id.enableOrDisableAlarm);
		enableOrDisableSedentary = (ToggleButton) findViewById(R.id.enableOrDisableSedentary);
		enableOrDisableRadiation = (ToggleButton) findViewById(R.id.enableOrDisableRadiation);
		enableOrDisableFetalMovement = (ToggleButton) findViewById(R.id.enableOrDisableFetalMovement);
	}

	private void showMainInterface(boolean isShow) {
		if (isShow) {
			view_divider0.setVisibility(View.VISIBLE);
			layconstant.setVisibility(View.VISIBLE);
			mCustomScrollView.setVisibility(View.VISIBLE);
			tv_nodevice.setVisibility(View.INVISIBLE);
		} else {
			view_divider0.setVisibility(View.INVISIBLE);
			layconstant.setVisibility(View.INVISIBLE);
			mCustomScrollView.setVisibility(View.INVISIBLE);
			tv_nodevice.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void back(View v) {
		// TODO Auto-generated method stub
		if (isProcessDialogOnShow()) {
			closeProcessDialog();
			return;
		}
		super.back(v);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		back(null);
	}

	private void resetScrollViewPosition(final boolean isToTop) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (isToTop) {
					mCustomScrollView.fullScroll(ScrollView.FOCUS_UP);
				} else {
					mCustomScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}

			}
		});
	}

	@Override
	public void setViewListener() {
		// TODO Auto-generated method stub
		synchronization_bt.setIndeterminateProgressMode(false);
		synchronization_bt.setOnClickListener(this);
		synchronizationtime_bt.setOnClickListener(this);
		lay_time.setOnClickListener(this);
		lay_period.setOnClickListener(this);
		lay_overtime.setOnClickListener(this);
		lay_intervals.setOnClickListener(this);
		lay_destSteps.setOnClickListener(this);
		lay_starttime.setOnClickListener(this);
		lay_deviceinfo.setOnClickListener(this);
		lay_timeperiod.setOnClickListener(this);

		enableOrDisableAwake.setOnToggleChanged(new OnToggleChanged() {

			@Override
			public void onToggle(boolean on) {
				// TODO Auto-generated method stub
				niceAlarms.setSmartEnabled(on);
				updateNiceAlarms();
			}
		});

		enableOrDisableAlarm.setOnToggleChanged(new OnToggleChanged() {

			@Override
			public void onToggle(boolean on) {
				// TODO Auto-generated method stub
				// startActivity(new Intent(context, AlarmActivity.class));
				if (on) {
					lay_time.setVisibility(View.VISIBLE);
					lay_awake.setVisibility(View.VISIBLE);
					lay_period.setVisibility(View.VISIBLE);
					view_divider3.setVisibility(View.VISIBLE);
					displayTime(niceAlarms.getTimeHour(),
							niceAlarms.getTimeMin());
					displayWeekday();
					niceAlarms.setEnabled(true);
					if (niceAlarms.isSmartEnabled()) {
						enableOrDisableAwake.setToggleOn();
					} else {
						enableOrDisableAwake.setToggleOff();
					}
					resetScrollViewPosition(false);
				} else {
					lay_time.setVisibility(View.GONE);
					lay_awake.setVisibility(View.GONE);
					lay_period.setVisibility(View.GONE);
					view_divider3.setVisibility(View.GONE);
					niceAlarms.setEnabled(false);
					resetScrollViewPosition(true);
				}
				updateNiceAlarms();
			}
		});

		powerSavingSwitch.setOnToggleChanged(new OnToggleChanged() {

			@Override
			public void onToggle(boolean on) {
				// TODO Auto-generated method stub
				try {
					if (on) {
						powerSavingSwitch.setToggleOn();
						serviceIml.enableRadiation(1);
						mBleService.setBool(Constant.POWER_SAVING, true);
					} else {
						powerSavingSwitch.setToggleOff();
						serviceIml.enableRadiation(2);
						mBleService.setBool(Constant.POWER_SAVING, false);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});

		enableOrDisableRadiation.setOnToggleChanged(new OnToggleChanged() {

			@Override
			public void onToggle(boolean on) {
				// TODO Auto-generated method stub
				try {
					if (on) {
						laypowerSaving.setVisibility(View.VISIBLE);
						view_divider1.setVisibility(View.VISIBLE);
						powerSavingSwitch.setToggleOn();
						mBleService.setBool(Constant.POWER_SAVING, true);
						mBleService.setBool(
								Constant.ENABLE_OR_DISABLE_RADIATION, true);
						serviceIml.enableRadiation(1);
					} else {
						tv_radiation.setText("n/A");
						laypowerSaving.setVisibility(View.GONE);
						view_divider1.setVisibility(View.GONE);
						serviceIml.enableRadiation(0);
						mBleService.setBool(
								Constant.ENABLE_OR_DISABLE_RADIATION, false);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});

		enableOrDisableFetalMovement.setOnToggleChanged(new OnToggleChanged() {

			@Override
			public void onToggle(boolean on) {
				// TODO Auto-generated method stub
				try {

					if (on) {
						serviceIml.enableFetalMovement((byte) 1);
						mBleService.setBool(
								Constant.ENABLE_OR_DISABLE_FETALMOVEMENT, true);
						mBleService.setLong(Constant.ENABLE_FETALMOVEMENT,
								(new Date()).getTime());
						Trace.e(TAG, "enableOrDisableFetalMovement new start");
						iCountDownUtils = new CountDownUtils(3600000L, 1000,
								tv_fetalmovement);
						iCountDownUtils.start();
					} else {
						Trace.e(TAG, "enableOrDisableFetalMovement disable");
						if (null != iCountDownUtils) {
							Trace.e(TAG, "enableOrDisableFetalMovement cancel");
							iCountDownUtils.cancel();
						}
						tv_fetalmovement.setText("");
						serviceIml.enableFetalMovement((byte) 0);
						mBleService.setLong(Constant.ENABLE_FETALMOVEMENT, 0);
						mBleService
								.setBool(
										Constant.ENABLE_OR_DISABLE_FETALMOVEMENT,
										false);
					}
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		enableOrDisableSedentary.setOnToggleChanged(new OnToggleChanged() {

			@Override
			public void onToggle(boolean on) {
				// TODO Auto-generated method stub
				iSedentaryInfo.setEnableSedentary(on);
				enableSedentaryAwake(on);
				if (on) {
					lay_timeperiod.setVisibility(View.VISIBLE);
					lay_starttime.setVisibility(View.VISIBLE);
					lay_overtime.setVisibility(View.VISIBLE);
					view_divider2.setVisibility(View.VISIBLE);
					resetScrollViewPosition(false);
				} else {
					lay_timeperiod.setVisibility(View.GONE);
					lay_starttime.setVisibility(View.GONE);
					lay_overtime.setVisibility(View.GONE);
					view_divider2.setVisibility(View.GONE);
					resetScrollViewPosition(true);
				}
			}
		});

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
				titleLay.startAnimation();
				new Thread() {
					@Override
					public void run() {
						try {
							if (isConnnect) {
								postNewLogcatInfo("disconnect", Color.RED);
								mBleService.disconnect();
							} else {
								postNewLogcatInfo("connect", Color.GREEN);
								mBleService.connect(address);
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		});
	}

	private void disableView(boolean enable){

		lay_time.setClickable(enable);
		lay_period.setClickable(enable);
		lay_overtime.setClickable(enable);
		lay_intervals.setClickable(enable);
		lay_destSteps.setClickable(enable);
		lay_starttime.setClickable(enable);
		lay_deviceinfo.setClickable(enable);
		lay_timeperiod.setClickable(enable);
		powerSavingSwitch.setClickable(enable);
		enableOrDisableAlarm.setClickable(enable);
		enableOrDisableAwake.setClickable(enable);
		synchronizationtime_bt.setClickable(enable);
		enableOrDisableRadiation.setClickable(enable);
		enableOrDisableSedentary.setClickable(enable);
		enableOrDisableFetalMovement.setClickable(enable);
		lay_time.setEnabled(enable);
		lay_period.setEnabled(enable);
		lay_overtime.setEnabled(enable);
		lay_intervals.setEnabled(enable);
		lay_destSteps.setEnabled(enable);
		lay_starttime.setEnabled(enable);
		lay_deviceinfo.setEnabled(enable);
		lay_timeperiod.setEnabled(enable);	
		powerSavingSwitch.setEnabled(enable);
		enableOrDisableAlarm.setEnabled(enable);
		enableOrDisableAwake.setEnabled(enable);
		synchronizationtime_bt.setEnabled(enable);
		enableOrDisableRadiation.setEnabled(enable);
		enableOrDisableSedentary.setEnabled(enable);
		enableOrDisableFetalMovement.setEnabled(enable);
	}
	
	
	@Override
	public void setStatus(int state) {
		super.setStatus(state);
		switch (state) {
		case DfuService.STATE_DISCONNECTED: {
			isConnnect = false;
			// connect_status.setText("status:DISCONNECTED");
			titleLay.setRightContent(R.string.connect);
			showMainInterface(false);
			titleLay.stopAnimation();
			if(isUpdate){
				setSynchFailed();
			}			
		}
			break;
		case DfuService.STATE_CONNECTING: {
			// connect_status.setText("status:CONNECTING");
		}
			break;
		case DfuService.STATE_CONNECTED: {
			isConnnect = true;
			// connect_status.setText("status:CONNECTED");
		}
			break;
		case DfuService.STATE_CONNECTED_AND_READY: {
			isConnnect = true;
			// connect_status.setText("status:CONNECTED_AND_READY");
			titleLay.setRightContent(R.string.disconnect);
			showMainInterface(true);
			titleLay.stopAnimation();
		}
			break;
		case DfuService.STATE_DISCONNECTING: {
			// connect_status.setText("status:DISCONNECTING");
			showMainInterface(false);
			if(isUpdate){
				setSynchFailed();
			}			
		}
			break;
		case DfuService.STATE_CLOSED: {
			isConnnect = false;
			// connect_status.setText("status:CLOSED");
			titleLay.setRightContent(R.string.connect);
			showMainInterface(false);
			if(isUpdate){
				setSynchFailed();
			}			
		}
			break;
		}
	}

	private void displayRssi(int rssi){
		tv_rssi.setText(rssi + unit_dBm);
	}
	
	private void displayDataStep(DictDay data) {
		if (null != data) {
			tv_steps.setText(data.m_step + unit_step);
			int steps = getTargetSteps();
			if (data.m_step >= steps) {
				tv_steps.setTextColor(getResources()
						.getColor(R.color.bt_update));
			} else {
				tv_steps.setTextColor(Color.YELLOW);
			}
		} else {
			tv_steps.setText("n/A");
		}
	}

	public void readRssi(){
		try {
			mBleService.readRssi();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void displayBleBattery(BatteryInfo batteryInfo) {
		if (null != batteryInfo) {
			int soc = batteryInfo.getCapacity();
			tv_battery.setText(soc + getString(R.string.chargingUnit));
			tv_battery.setTextColor(Color.WHITE);
			if (soc <= 20) {
				tv_battery.setTextColor(Color.RED);
			}
			if (soc >= 80) {
				tv_battery.setTextColor(Color.GREEN);
			}
		} else {
			tv_battery.setText("n/A");
		}
	}

	private void dispRadiationData(Radiation radiation) {
		try {
			boolean enableRadiation = mBleService
					.getBool(Constant.ENABLE_OR_DISABLE_RADIATION);
			addNewLogcatInfo("dispRadiationData get ENABLE_OR_DISABLE_RADIATION "+enableRadiation, Color.WHITE);
			if (!enableRadiation) {
				tv_radiation.setText("n/A");
				return;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (null != radiation) {
			tv_radiation.setText(decimalFormat.format(radiation.getRadiation())
					+ getString(R.string.unit_mG));
		} else {
			tv_radiation.setText("n/A");
		}
	}

	private void displayVersionInfo() {
		try {
			addNewLogcatInfo("displayVersionInfo dbgetDevice", Color.WHITE);
			Device device = mBleService.dbgetDevice();
			if (null != device) {
				tv_deviceinfo.setText("Ver" + device.getFirmwareVersion());
			} else {
				tv_deviceinfo.setText(R.string.clickfor);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static class MyHandler extends Handler {
		private WeakReference<FunctionActivity> activityWeakReference;// =new
																		// WeakReference<Activity>();

		public MyHandler(FunctionActivity activity) {
			this.activityWeakReference = new WeakReference<FunctionActivity>(
					activity);
		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			FunctionActivity activity = activityWeakReference.get();
			if (null != activity && !activity.isFinishing()) {
				switch (msg.what) {
				case MSG_READ_RSSI:{
					activity.readRssi();
				}break;
				
				case MessageUtil.BATTERYINFO: {
					BatteryInfo para = (BatteryInfo) (msg.obj);
					activity.displayBleBattery(para);
				}
					break;
				case MessageUtil.RADIATION_DATA: {
					Radiation para = (Radiation) (msg.obj);
					activity.dispRadiationData(para);
				}
					break;
				case MessageUtil.ACTION_REAL_TIME_STEP_DATA: {
					DictDay para = (DictDay) (msg.obj);
					activity.displayDataStep(para);
				}
					break;
					
				case MessageUtil.RSSI_DATA: {
					activity.displayRssi(msg.arg1);
				}
					break;					
					
				case MessageUtil.FIRMWAREVERSIONS_BACK: {
					activity.displayVersionInfo();
				}
					break;
				case MessageUtil.UPDATE_BACK: {
					activity.displaySynchStatus((Boolean) msg.obj, msg.arg1);
				}
					break;
				case MessageUtil.CLOSE_FETALMOVEMENT: {
					activity.close_fetalmovement();
				}
					break;
				case MessageUtil.CHECK_TIMEOUT: {
					activity.checkSynchTimeout();
				}
				case MessageUtil.LOGCAT_INFO: {
					activity.addNewLogcatInfo((String) msg.obj, msg.arg1);
				}				
				}
			}
		}
	}

	private void addNewLogcatInfo(String msg, int color){
		adapter.addItem(new LogcatInfo(color, msg));
	}
	
	private void postNewLogcatInfo(String msg, int color){
            Message message = new Message();
            message.what = MessageUtil.LOGCAT_INFO;
            message.obj = msg;
            message.arg1 = color;
            handler.sendMessage(message);
	}	
	
	private void setSynchFailed(){
		addNewLogcatInfo("setSynchFailed", Color.RED);
		isUpdate = false;
		disableView(!isUpdate);
		synchronization_bt.setProgress(-1);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				synchronization_bt.setText(R.string.synchronizationStr);
			}
		}, 3000);		
	}
	
	private void checkSynchTimeout() {
		if (synchronization_bt.getProgress() < 1) {
			addNewLogcatInfo("checkSynchTimeout timeout", Color.RED);
			setSynchFailed();
		}
	}

	private void close_fetalmovement() {
		Trace.e(TAG, "close_fetalmovement");
		iCountDownUtils.cancel();
		tv_fetalmovement.setText("");
		enableOrDisableFetalMovement.setToggleOff();
		try {
			serviceIml.enableFetalMovement((byte) 0);
			addNewLogcatInfo("close_fetalmovement", Color.WHITE);
			mBleService.setLong(Constant.ENABLE_FETALMOVEMENT, 0);
			mBleService
					.setBool(Constant.ENABLE_OR_DISABLE_FETALMOVEMENT, false);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void displaySynchStatus(Boolean result, int progress) {
		if (result) {
			synchronization_bt.setProgress(progress);
			if (100 == progress) {
				isUpdate = false;
				disableView(!isUpdate);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						synchronization_bt.setText(R.string.synchronizationStr);
					}
				}, 3000);
			}
		} else {
			setSynchFailed();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.synchronizationtime_bt:
			try {
				serviceIml.synchronizationTime();
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case R.id.synchronization_bt:
			if(!isUpdate){
				try {
					isUpdate = true;
					disableView(!isUpdate);
					handler.sendEmptyMessageDelayed(MessageUtil.CHECK_TIMEOUT, 10000);
					serviceIml.synchronization();
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
			}break;

		case R.id.lay_deviceinfo:
			try {
				serviceIml.getDeviceInfo();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.lay_intervals: {
			displaySecondsSettings(null);
		}
			break;
		case R.id.lay_destSteps:
			displayStepsSettings(null);
			break;
		case R.id.lay_timeperiod:
			displayEditNotification(null);
			break;
		case R.id.lay_starttime:
			editTimeType = 0;
			displayTime(v);
			break;
		case R.id.lay_overtime:
			editTimeType = 1;
			displayTime(v);
			break;
		case R.id.lay_time:
			editTimeType = 2;
			displayTime(v);
			break;
		case R.id.lay_period:
			displayWeek(v);
			break;
		}
	}

	/**************************** 久坐提醒相关函数 start ***********************/
	private void sendSettingToDevice() {
		try {
			byte[] info = new byte[7];
			info[0] = 0x09;
			boolean enableSedentary = iSedentaryInfo.isEnableSedentary();
			if (enableSedentary) {
				info[1] = (byte) (iSedentaryInfo.getTimeLen() & 0xff);
			} else {
				info[1] = 0;
			}

			info[2] = (byte) (iSedentaryInfo.getTimeLen() >> 8);
			info[3] = (byte) iSedentaryInfo.getStart_hour();
			info[4] = (byte) iSedentaryInfo.getStart_min();
			info[5] = (byte) iSedentaryInfo.getEnd_hour();
			info[6] = (byte) iSedentaryInfo.getEnd_min();
			addNewLogcatInfo("sendSettingToDevice setSedentaryInfo", Color.WHITE);
			serviceIml.setSedentaryInfo(info);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		saveSedentary();
	}

	private boolean isValidLength(int timeLen) {
		boolean enableSedentary = iSedentaryInfo.isEnableSedentary();
		if (enableSedentary) {
			int start = iSedentaryInfo.getStart_hour() * 60
					+ iSedentaryInfo.getStart_min();
			int end = iSedentaryInfo.getEnd_hour() * 60
					+ iSedentaryInfo.getEnd_min();
			if (timeLen >= end - start) {
				Toast.makeText(context, R.string.beyondPeriod,
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

	private boolean isValidEnd(int hour, int minute) {
		boolean enableSedentary = iSedentaryInfo.isEnableSedentary();
		if (enableSedentary) {
			int timeLen = iSedentaryInfo.getTimeLen();
			int start = iSedentaryInfo.getStart_hour() * 60
					+ iSedentaryInfo.getStart_min();
			int end = hour * 60 + minute;
			if (start >= end) {
				// 起始时间不能晚于结束时间
				Toast.makeText(context, R.string.nolater, Toast.LENGTH_SHORT)
						.show();
				return false;
			}

			if (timeLen >= end - start) {
				Toast.makeText(context, R.string.beyondPeriod,
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

	private boolean isValidStart(int hour, int minute) {
		boolean enableSedentary = iSedentaryInfo.isEnableSedentary();
		if (enableSedentary) {
			int timeLen = iSedentaryInfo.getTimeLen();
			int start = hour * 60 + minute;
			int end = iSedentaryInfo.getEnd_hour() * 60
					+ iSedentaryInfo.getEnd_min();
			if (start >= end) {
				Toast.makeText(context, R.string.nolater, Toast.LENGTH_SHORT)
						.show();
				return false;
			}

			if (timeLen >= end - start) {
				Toast.makeText(context, R.string.beyondPeriod,
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

	private void setTime(int hour, int minute) {
		switch (editTimeType) {
		case 0: {
			if (!isValidStart(hour, minute)) {
				return;
			}
			iSedentaryInfo.setStart_hour(hour);
			iSedentaryInfo.setStart_min(minute);
			String srart = String.format(Locale.getDefault(), "%02d:%02d",
					hour, minute);
			tv_starttime.setText(srart);
			sendSettingToDevice();
		}
			break;
		case 1: {
			if (!isValidEnd(hour, minute)) {
				return;
			}
			iSedentaryInfo.setEnd_hour(hour);
			iSedentaryInfo.setEnd_min(minute);
			String end = String.format(Locale.getDefault(), "%02d:%02d", hour,
					minute);
			tv_overtime.setText(end);
			sendSettingToDevice();
		}
			break;
		case 2: {
			niceAlarms.setTimeHour(hour);
			niceAlarms.setTimeMin(minute);
			String end = String.format(Locale.getDefault(), "%02d:%02d", hour,
					minute);
			tv_time.setText(end);
			updateNiceAlarms();
		}
			break;
		}
	}

	private void setTimeperiod(int timeLen) {
		if (!isValidLength(timeLen)) {
			return;
		}
		iSedentaryInfo.setTimeLen(timeLen);
		tv_timeperiod.setText(timeLen + unit_minute);
		sendSettingToDevice();
	}

	private void enableSedentaryAwake(boolean enable) {
		lay_timeperiod.setEnabled(enable);
		lay_starttime.setEnabled(enable);
		lay_overtime.setEnabled(enable);
		if (enable) {
			tv_timeperiod.setTextColor(getResources().getColor(R.color.text2));
			tv_starttime.setTextColor(getResources().getColor(R.color.text2));
			tv_overtime.setTextColor(getResources().getColor(R.color.text2));
		} else {
			tv_timeperiod.setTextColor(getResources().getColor(
					R.color.textGray3));
			tv_starttime.setTextColor(getResources()
					.getColor(R.color.textGray3));
			tv_overtime
					.setTextColor(getResources().getColor(R.color.textGray3));
		}
		int minute = iSedentaryInfo.getTimeLen() & 0xff;
		tv_timeperiod.setText(minute + unit_minute);
		int startH = iSedentaryInfo.getStart_hour() & 0x1f;
		int startM = iSedentaryInfo.getStart_min() & 0x3f;
		String srart = String.format(Locale.getDefault(), "%02d:%02d", startH,
				startM);
		tv_starttime.setText(srart);
		int endtH = iSedentaryInfo.getEnd_hour() & 0x1f;
		int endM = iSedentaryInfo.getEnd_min() & 0x3f;
		String end = String.format(Locale.getDefault(), "%02d:%02d", endtH,
				endM);
		tv_overtime.setText(end);
		sendSettingToDevice();
	}

	private void initSedentary() {
		long content = 0;
		try {
			content = mBleService.getLong(Constant.SEDENTARY_NOTIFICATION);
			addNewLogcatInfo("initSedentary SEDENTARY_NOTIFICATION "+content, Color.WHITE);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int endM = (int) (content & 0x3f);
		if (endM < 0 || endM > 59) {
			endM = 30;
		}
		content = content >> 6;
		int endtH = (int) (content & 0x1f);
		if (endtH <= 0 || endtH > 23) {
			endtH = 20;
		}
		content = content >> 5;
		int startM = (int) (content & 0x3f);
		if (startM < 0 || startM > 59) {
			startM = 0;
		}
		content = content >> 6;
		int startH = (int) (content & 0x1f);
		if (startH <= 0 || startH > 23) {
			startH = 14;
		}
		content = content >> 5;
		int timeLen = (int) (content & 0xff);
		if (timeLen <= 0 || timeLen > 180) {
			timeLen = 30;
		}
		content = (content >> 8) & 0x01;
		boolean enableSedentary;
		if (content == 1) {
			enableSedentary = true;
		} else {
			enableSedentary = false;
		}
		iSedentaryInfo = new SedentaryInfo();
		iSedentaryInfo.setTimeLen(timeLen);
		iSedentaryInfo.setStart_hour(startH);
		iSedentaryInfo.setStart_min(startM);
		iSedentaryInfo.setEnd_hour(endtH);
		iSedentaryInfo.setEnd_min(endM);
		saveSedentary();
		if (enableSedentary) {
			enableOrDisableSedentary.setToggleOn();
		} else {
			enableOrDisableSedentary.setToggleOff();
		}
		enableSedentaryAwake(enableSedentary);
		if (enableSedentary) {
			lay_timeperiod.setVisibility(View.VISIBLE);
			lay_starttime.setVisibility(View.VISIBLE);
			lay_overtime.setVisibility(View.VISIBLE);
			view_divider2.setVisibility(View.VISIBLE);
		} else {
			lay_timeperiod.setVisibility(View.GONE);
			lay_starttime.setVisibility(View.GONE);
			lay_overtime.setVisibility(View.GONE);
			view_divider2.setVisibility(View.GONE);
		}
	}

	private void saveSedentary() {
		long content = 0;
		boolean enableSedentary = iSedentaryInfo.isEnableSedentary();
		if (enableSedentary == true) {
			content = 1;
		}
		content = content << 8;
		content += iSedentaryInfo.getTimeLen() & 0xff;
		content = content << 5;
		content += iSedentaryInfo.getStart_hour() & 0x1f;
		content = content << 6;
		content += iSedentaryInfo.getStart_min() & 0x3f;
		content = content << 5;
		content += iSedentaryInfo.getEnd_hour() & 0x1f;
		content = content << 6;
		content += iSedentaryInfo.getEnd_min() & 0x3f;
		try {
			addNewLogcatInfo("saveSedentary SEDENTARY_NOTIFICATION "+content, Color.WHITE);
			mBleService.setLong(Constant.SEDENTARY_NOTIFICATION, content);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**************************** 久坐提醒相关函数 end ***********************/
	private void displayTime(View view) {
		dialog = new Dialog(context, R.style.mystyle);
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_edit_time, (ViewGroup) null);
		dialog.setContentView(v);
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		dialog.getWindow().setLayout(metrics.widthPixels,
				android.view.WindowManager.LayoutParams.WRAP_CONTENT);
		hours = (NumberPicker) dialog.findViewById(R.id.hours);
		min = (NumberPicker) dialog.findViewById(R.id.min);
		AMOrPM = (NumberPicker) dialog.findViewById(R.id.AMOrPM);

		hours.setMaxValue(12);
		hours.setMinValue(1);

		min.setMaxValue(59);
		min.setMinValue(0);

		AMOrPM.setDisplayedValues(new String[] { "am", "pm" });
		AMOrPM.setMinValue(0);
		AMOrPM.setMaxValue(1);
		int hour = 0, minute = 0;
		switch (editTimeType) {
		case 0: {
			hour = iSedentaryInfo.getStart_hour();
			minute = iSedentaryInfo.getStart_min();
		}
			break;
		case 1: {
			hour = iSedentaryInfo.getEnd_hour();
			minute = iSedentaryInfo.getEnd_min();
		}
			break;
		case 2: {
			hour = niceAlarms.getTimeHour();
			minute = niceAlarms.getTimeMin();
		}
			break;
		}

		if (hour < 13) {
			hours.setValue(hour);
			min.setValue(minute);
			AMOrPM.setValue(0);
		} else {
			hours.setValue(hour - 12);
			min.setValue(minute);
			AMOrPM.setValue(1);
		}
		dialog.findViewById(R.id.selected).setOnClickListener(saveEditTime);
		dialog.show();
	}

	private View.OnClickListener saveEditTime = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int value1 = hours.getValue();
			int value2 = min.getValue();
			int value3 = AMOrPM.getValue();
			if (value3 == 0 && value1 == 12) {
				value1 = 0;
			} else if (value3 == 1 && value1 != 12) {
				value1 += 12;
			}
			setTime(value1, value2);
			dialog.dismiss();
		}
	};

	private void initDialog() {
		dialog = new Dialog(context, R.style.mystyle);
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_numberpicker,
				(ViewGroup) null);
		dialog.setContentView(v);
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		dialog.getWindow().setLayout(metrics.widthPixels,
				android.view.WindowManager.LayoutParams.WRAP_CONTENT);
		numPicker = (NumberPicker) dialog.findViewById(R.id.numPicker);
	}

	private void initMinutes() {
		minutes = new String[MINUTE_ITEMS];
		for (int i = 0; i < MINUTE_ITEMS; i++) {
			minutes[i] = (30 + i) + unit_minute;
		}
	}

	private void displayEditNotification(View view) {
		initMinutes();
		initDialog();
		numPicker.setDisplayedValues(minutes);
		numPicker.setMinValue(0);
		numPicker.setMaxValue(minutes.length - 1);

		int timeLen = iSedentaryInfo.getTimeLen();
		int val = 0;
		for (int i = 0; i < minutes.length; i++) {
			if (timeLen == 30 + i) {
				val = i;
				break;
			}
		}
		numPicker.setValue(val);
		dialog.findViewById(R.id.selected).setOnClickListener(saveMinute);
		dialog.show();
	}

	private View.OnClickListener saveMinute = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null != minutes) {
				String value = minutes[numPicker.getValue()];
				String temp = value.substring(0, value.length() - 2);
				setTimeperiod(Integer.valueOf(temp).intValue());
				dialog.dismiss();
			}
		}
	};

	private void initSeconds() {
		seconds = new String[INTERVALS_ITEMS];
		for (int i = 1, j = 1; i <= Constant.MAX_TARGET_INTERVALS; i++, j++) {
			seconds[j] = i + unit_second;
		}
		seconds[0] = getString(R.string.close_auto);
	}

	private void displaySecondsSettings(View view) {
		initSeconds();
		initDialog();
		numPicker.setDisplayedValues(seconds);
		numPicker.setMinValue(0);
		numPicker.setMaxValue(seconds.length - 1);
		
		int val = 0;
		try {
			int intervals = mBleService.getInt(Constant.DEST_INTERVALS);
			for (int i = 0; i <= seconds.length; i++) {
				if (intervals == i) {
					val = i;
					break;
				}
			}			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		numPicker.setValue(val);
		dialog.findViewById(R.id.selected).setOnClickListener(saveSeconds);
		dialog.show();
	}

	private OnClickListener saveSeconds = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null != seconds) {
				if (numPicker.getValue() > 0) {
					String value = seconds[numPicker.getValue()];
					String temp = value.substring(0, value.length() - 1);
					int content = Integer.valueOf(temp).intValue();
					try {
						addNewLogcatInfo("saveSeconds DEST_INTERVALS "+content, Color.WHITE);
						mBleService.setInt(Constant.DEST_INTERVALS, content);
						tv_intervals.setText(content + unit_second);
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					try {
						addNewLogcatInfo("saveSeconds DEST_INTERVALS 0", Color.WHITE);
						mBleService.setInt(Constant.DEST_INTERVALS, 0);
						tv_intervals.setText(R.string.close_auto);
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				dialog.dismiss();
			}
		}
	};

	private void initSteps() {
		steps = new String[STEPS_ITEMS];
		for (int i = Constant.MIN_TARGET_STEP, j = 0; i <= Constant.MAX_TARGET_STEP; i += Constant.MIN_TARGET_STEP, j++) {
			steps[j] = i + unit_step;
		}
	}

	private void displayStepsSettings(View view) {
		initSteps();
		initDialog();
		numPicker.setDisplayedValues(steps);
		numPicker.setMinValue(0);
		numPicker.setMaxValue(steps.length - 1);

		long content = getTargetSteps();
		int val = 0;
		for (int i = 1; i <= steps.length; i++) {
			if (content == i * Constant.MIN_TARGET_STEP) {
				val = i - 1;
				break;
			}
		}
		numPicker.setValue(val);
		dialog.findViewById(R.id.selected).setOnClickListener(saveSteps);
		dialog.show();
	}

	private OnClickListener saveSteps = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null != steps) {
				String value = steps[numPicker.getValue()];
				String temp = value.substring(0, value.length() - 1);
				int content = Integer.valueOf(temp).intValue();
				try {
					addNewLogcatInfo("saveSteps DEST_STEPS", Color.WHITE);
					mBleService.setInt(Constant.DEST_STEPS, content);
					tv_destSteps.setText(content + unit_step);
					addNewLogcatInfo("saveSteps setTarget", Color.WHITE);
					serviceIml.setTarget(content);
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dialog.dismiss();
			}
		}
	};

	private void showAlertDialog(View view) {

		CustomDialog.Builder builder = new CustomDialog.Builder(context);
		builder.setMessage(R.string.hint_synch);
		builder.setTitle(R.string.title_alert);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							serviceIml.resetDevice();
							mBleService.resetSleepDataDataBase();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						dialog.dismiss();
					}
				});
//		builder.setNegativeButton(android.R.string.cancel,
//				new android.content.DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});

		builder.create().show();
	}

	public void loadData() {
		try {
			addNewLogcatInfo("loadData getNiceAlarm(1)", Color.WHITE);
			niceAlarms = mBleService.getNiceAlarm(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayData() {
		if (null == niceAlarms) {
			niceAlarms = new NiceAlarm();
			niceAlarms.setEnabled(false);
			niceAlarms.setNumber(1);
			niceAlarms.setRepetition(0x7f);
			niceAlarms.setTimeHour(12);
			niceAlarms.setTimeMin(0);
			niceAlarms.setDeviceAddress(address);
			try {
				addNewLogcatInfo("displayData get account", Color.WHITE);
				account = mBleService.getStr(Constant.CURRENT_ACCOUNT);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			niceAlarms.setAccount(account);
		}

		if (niceAlarms.isEnabled()) {
			enableOrDisableAlarm.setToggleOn();
			lay_time.setVisibility(View.VISIBLE);
			lay_awake.setVisibility(View.VISIBLE);
			lay_period.setVisibility(View.VISIBLE);
			view_divider3.setVisibility(View.VISIBLE);
			displayTime(niceAlarms.getTimeHour(), niceAlarms.getTimeMin());
			displayWeekday();
			if (niceAlarms.isSmartEnabled()) {
				enableOrDisableAwake.setToggleOn();
			} else {
				enableOrDisableAwake.setToggleOff();
			}
		} else {
			enableOrDisableAlarm.setToggleOff();
			lay_time.setVisibility(View.GONE);
			lay_awake.setVisibility(View.GONE);
			lay_period.setVisibility(View.GONE);
			view_divider3.setVisibility(View.GONE);
		}
	}

	private void displayTime(int hours, int min) {
		String end = String
				.format(Locale.getDefault(), "%02d:%02d", hours, min);
		tv_time.setText(end);
	}

	private void displayWeekday() {
		tv_period.setText(getShowDayStr());
	}

	private String getShowDayStr() {
		String dayTxt = "";
		byte b = (byte) niceAlarms.getRepetition();
		if ((b & 0x7f) == 0x7f) {
			return getString(R.string.everyday);
		}
		if ((b & 0x3e) == 0x3e) {
			return getString(R.string.workday);
		}

		if ((b & 2) == 2) {
			dayTxt += (dayTxt.length() > 0 ? "," : "")
					+ getString(R.string.monday);
		}
		if ((b & 4) == 4) {
			dayTxt += (dayTxt.length() > 0 ? "," : "")
					+ getString(R.string.tuesday);
		}
		if ((b & 8) == 8) {
			dayTxt += (dayTxt.length() > 0 ? "," : "")
					+ getString(R.string.wednesday);
		}
		if ((b & 16) == 16) {
			dayTxt += (dayTxt.length() > 0 ? "," : "")
					+ getString(R.string.thursday);
		}
		if ((b & 32) == 32) {
			dayTxt += (dayTxt.length() > 0 ? "," : "")
					+ getString(R.string.friday);
		}
		if ((b & 64) == 64) {
			dayTxt += (dayTxt.length() > 0 ? "," : "")
					+ getString(R.string.saturday);
		}
		if ((b & 1) == 1) {
			dayTxt += (dayTxt.length() > 0 ? "," : "")
					+ getString(R.string.sunday);
		}
		return dayTxt;
	}

	private void displayWeek(View view) {
		dialog = new Dialog(context, R.style.mystyle);
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater
				.inflate(R.layout.dialog_select_week, (ViewGroup) null);
		dialog.setContentView(v);
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		dialog.getWindow().setLayout(metrics.widthPixels,
				android.view.WindowManager.LayoutParams.WRAP_CONTENT);
		monday = (CheckBox) dialog.findViewById(R.id.monday);
		tuesday = (CheckBox) dialog.findViewById(R.id.tuesday);
		wednesday = (CheckBox) dialog.findViewById(R.id.wednesday);
		thursday = (CheckBox) dialog.findViewById(R.id.thursday);
		friday = (CheckBox) dialog.findViewById(R.id.friday);
		saturday = (CheckBox) dialog.findViewById(R.id.saturday);
		sunday = (CheckBox) dialog.findViewById(R.id.sunday);

		monday.setChecked(false);
		tuesday.setChecked(false);
		wednesday.setChecked(false);
		thursday.setChecked(false);
		friday.setChecked(false);
		saturday.setChecked(false);
		sunday.setChecked(false);

		byte b = (byte) niceAlarms.getRepetition();
		if ((b & 1) == 1) {
			sunday.setChecked(true);
		}
		if ((b & 2) == 2) {
			monday.setChecked(true);
		}
		if ((b & 4) == 4) {
			tuesday.setChecked(true);
		}
		if ((b & 8) == 8) {
			wednesday.setChecked(true);
		}
		if ((b & 16) == 16) {
			thursday.setChecked(true);
		}
		if ((b & 32) == 32) {
			friday.setChecked(true);
		}
		if ((b & 64) == 64) {
			saturday.setChecked(true);
		}

		dialog.findViewById(R.id.selected).setOnClickListener(saveEditWeek);
		dialog.show();
	}

	private View.OnClickListener saveEditWeek = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			byte b = 0;

			if (sunday.isChecked()) {
				b |= 0x01;
			}
			if (monday.isChecked()) {
				b |= 0x02;
			}
			if (tuesday.isChecked()) {
				b |= 0x04;
			}
			if (wednesday.isChecked()) {
				b |= 0x08;
			}
			if (thursday.isChecked()) {
				b |= 0x10;
			}
			if (friday.isChecked()) {
				b |= 0x20;
			}
			if (saturday.isChecked()) {
				b |= 0x40;
			}
			niceAlarms.setRepetition(b);
			addNewLogcatInfo("saveEditWeek updateNiceAlarm", Color.WHITE);
			updateNiceAlarms();
			displayWeekday();
			dialog.dismiss();
		}
	};

	private void updateNiceAlarms() {
		byte[] info;
		byte enable, smartEnabled, repetition, timeHour, timeMin;
		repetition = (byte) niceAlarms.getRepetition();
		timeHour = (byte) niceAlarms.getTimeHour();
		timeMin = (byte) niceAlarms.getTimeMin();
		if (repetition == 0) {
			niceAlarms.setEnabled(false);
		} else {
			niceAlarms.setEnabled(true);
		}
		if (niceAlarms.isEnabled()) {
			enable = 1;
		} else {
			enable = 0;
		}
		if (niceAlarms.isSmartEnabled()) {
			smartEnabled = 1;
		} else {
			smartEnabled = 0;
		}

		try {
			info = new byte[] { 2, 1, repetition, timeHour, timeMin, enable,
					smartEnabled };
			serviceIml.enableAlarm(info);
			mBleService.updateNiceAlarm(niceAlarms);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public class RssiThread implements Runnable {  
	    @Override  
	    public void run() {  
	        // TODO Auto-generated method stub  
	        while (isReadRssi) {  
	            try {  
	                Thread.sleep(2000);// 线程暂停1秒，单位毫秒  
	                Message message = new Message();  
	                message.what = MSG_READ_RSSI;  
	                handler.sendMessage(message);// 发送消息  
	            } catch (InterruptedException e) {  
	                // TODO Auto-generated catch block  
	                e.printStackTrace();  
	            }  
	        }  
	    }  
	}  	
}
