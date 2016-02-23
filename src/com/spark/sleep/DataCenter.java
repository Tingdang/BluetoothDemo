package com.spark.sleep;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.spark.data.Data;
import com.spark.service.aidl.DictDay;
import com.spark.util.Trace;


public class DataCenter {
	private static final String TAG = DataCenter.class.getSimpleName();
	// 计算睡眠质量
	public static final int DEEP = 90 * 60;
	public static final int LIGHT = 300 * 60;

	public DBUtil dbUtil;
	public List<DictDay> m_days = new ArrayList<DictDay>();
	private int startTick = -1;
	private int m_devID;
	private int m_earliest_day;
	private int m_latest_day;
	
	// private long m_sleepRegion_begin;    //睡眠识别区域开始 从前一天0点开始的毫秒数
	// private long m_sleepRegion_end;      //睡眠识别区域结束 从前一天0点开始的毫秒数
	// private long m_sleepRegion_duration; //睡眠识别参考睡眠时间长度 单位毫秒
	
	public DataCenter(Context context)
	{
		m_devID = 1;
		m_earliest_day = 0;
		m_latest_day   = 0;
		dbUtil = new DBUtil(context);
	}
	
	public void SetDevID(int devID)
	{
		m_devID = devID;
		Clear();
	}
	public int GetDevID()
	{
		return m_devID;
	}
	public long TimeAlignToDay(long today)
	{
		Date tmp = new Date(today);
		tmp = DateUtils.getDayStartEnd(tmp).get(DateUtils.BEGIN);
		
		return tmp.getTime();
	}

	public final DictDay GetDay(String address, Date today)
	{
		if (m_devID == 0) {
			return null;
		}

		long dayStart = TimeAlignToDay(today.getTime());
		
		int dayStart1 = (int)(dayStart/1000L);
		if(dayStart1 < m_earliest_day || dayStart1 > m_latest_day){
			return null;
		}
		
		// 先看看缓存里面有没有，然后再从数据库里面取
		for (DictDay ttDay : m_days) {
			if (ttDay.m_time == dayStart) {
				return ttDay;
			}
		}
		
		DictDay tmpDay = dbUtil.queryDayData_ORG(address, today);
		if (tmpDay == null) {
			return null;
		}
		
		SleepDB2 sleepDB2 = dbUtil.queryDB2(address, today);
		if(sleepDB2 != null) {
			tmpDay.m_sleep_deep = sleepDB2.getDeepSleep();
			tmpDay.m_sleep_light = sleepDB2.getLightSleep();
			tmpDay.m_sleep_wakeup = sleepDB2.getSoberCnt();
			tmpDay.m_time_sleep = sleepDB2.getStartTime();
			tmpDay.m_time_getup = sleepDB2.getEndTime();
		}
		List<SleepDB1> sleepDB1s = dbUtil.queryDB1(address, today);
		for (SleepDB1 sleepDB1 : sleepDB1s) {
			if((sleepDB1.getDateFirst() >= tmpDay.m_time_sleep) && 
					(sleepDB1.getDateFirst() <= tmpDay.m_time_getup)) {
				if(sleepDB1.getDateLast() > tmpDay.m_time_getup) {
					sleepDB1.setDateLast(tmpDay.m_time_getup);
				}
				tmpDay.m_org_sleeps.add(sleepDB1);
			}
		}
		
		int deep = tmpDay.m_sleep_deep;
		int light = tmpDay.m_sleep_light;
		SleepQuality quality = SleepQuality.unkown;
		if((deep !=0) || (light != 0)) {
			if (deep > DEEP && light > LIGHT) {
				quality = SleepQuality.best;
			} else if (deep > DEEP && light < LIGHT) {
				quality = SleepQuality.good;
			} else if (deep < DEEP && light > LIGHT) {
				quality = SleepQuality.ordinary;
			} else if (deep < DEEP && light < LIGHT) {
				quality = SleepQuality.worst;
			} else {
				quality = SleepQuality.worst;
			}
		}
		
		tmpDay.m_day_sleep_quality = quality;
		tmpDay.m_time = dayStart;
		Trace.e(TAG, tmpDay.toString());
		m_days.add(tmpDay);
		return tmpDay;
	}
	

	public int GetEarliestDay(String address)
	{
//		if (m_earliest_day == 0) {
//			UpateValidTimeRange();
//		}
		if(m_earliest_day == 0) {
			m_earliest_day = dbUtil.queryEarliestDay_ORG(address);
		}
		return m_earliest_day;
	}

	public int GetLatestDay(String address)
	{
		if (m_latest_day == 0) {
			m_latest_day = dbUtil.queryLatestDay_ORG(address);
		}
		
		return m_latest_day;
	}

	public void Clear()
	{
		m_earliest_day = 0;
		m_latest_day = 0;
		m_days.clear();
	}
	
	private boolean updateSleepDB1(String address) {
		List<Data> datas;
		List<SleepDB1> sleepDB1s;
		boolean bSuccess = false;
//		try {
//			datas = dbHelper.readNewOrgData();
//			datas = SleepCounter.fillOrgData(datas);
//			sleepDB1s = SleepCounter.org2Range(datas);
//			bSuccess = dbHelper.updateNewDB1(sleepDB1s);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		
		datas = dbUtil.queryRawData_ORG(address, startTick);
		if(null == datas){
			return false;
		}
//		for (Data data : datas) {
//			Trace.e(TAG, data.toString());
//		}
		datas = SleepCounter.fillOrgData(datas);
		Trace.e(TAG, "fill Data sum" + datas.size());
		sleepDB1s = SleepCounter.org2Range(datas);
//		for (SleepDB1 sleepDB1 : sleepDB1s) {
//			Trace.e(TAG, sleepDB1.toString());
//		}
		bSuccess = dbUtil.insertDB1(address, sleepDB1s);
		return bSuccess;
	}

	private boolean updateSleepDB2(String address) {
		long prevTick;
		long nowTick = System.currentTimeMillis();
		long sum = 0;
		boolean bSuccess = false;
		List<SleepDB1> sleepDB1s = null;
		List<SleepDB2> sleepDB2s = new ArrayList<SleepDB2>();
		
		try {
//			prevTick = dbHelper.readPrevDayDB2();
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
//			String str = sdf.format(new Date(prevTick));
//			Date date = sdf.parse(str);
//			prevTick = date.getTime();
//			sum = (nowTick - prevTick) / DateUtils.DAY_SPAN;
//			for(int i = 0; i <= sum; i++) {
//				sleepDB1s = dbHelper.readNewDB1(new Date(prevTick + DateUtils.DAY_SPAN * i));
//				if(sleepDB1s.size() != 0) {
//					SleepDB2 sleepDB2 = SleepCounter.range2Valid(sleepDB1s);
//					if(sleepDB2 != null) {
//						sleepDB2s.add(sleepDB2);
//					}
//				}
//			}
//			bSuccess = dbHelper.updateNewDB2(sleepDB2s);
			
			prevTick = dbUtil.queryRawTick_DB2(address);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00",Locale.getDefault());
			String str = sdf.format(new Date(prevTick));
			Date date = sdf.parse(str);
			prevTick = date.getTime();
			sum = (nowTick - prevTick) / DateUtils.DAY_SPAN;
			Trace.e(TAG, "prev " + prevTick + ",now " + nowTick + ",sum " + sum);
			for(int i = 0; i <= sum; i++) {
				sleepDB1s = dbUtil.queryDB1(address, new Date(prevTick + DateUtils.DAY_SPAN * i));
				if(sleepDB1s.size() != 0) {
//					for (SleepDB1 sleepDB1 : sleepDB1s) {
//						Trace.e(TAG, sleepDB1.toString());
//					}
					SleepDB2 sleepDB2 = SleepCounter.range2Valid(sleepDB1s);
					if(sleepDB2 != null) {
						Trace.e(TAG, sleepDB2.toString());
						sleepDB2s.add(sleepDB2);
					}
				}
			}
			bSuccess = dbUtil.insertDB2(address, sleepDB2s);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bSuccess;
	}	
	
	public void deleteSleepDB1(String address) {
		dbUtil.deleteDB1(address);
	}
	
	public void deleteSleepDB2(String address) {
		dbUtil.deleteDB2(address);
	}	
	
	public void setStartTick(int startTick){
		this.startTick = startTick;
	}
		
	public void updateAfterFresh(String address){
	    System.gc();
		if(updateSleepDB1(address)) {
			updateSleepDB2(address);
		}	
		m_earliest_day = GetEarliestDay(address);
		m_latest_day = GetLatestDay(address);
		m_days.clear();
		System.gc();		
	}
}
