package com.spark.service.aidl;

import java.util.ArrayList;
import java.util.List;

import com.spark.sleep.SleepDB1;
import com.spark.sleep.SleepQuality;

import android.os.Parcel;
import android.os.Parcelable;

public class DictDay implements Parcelable{
	public int m_step;
	public int m_calorie;
	public int m_distance;
	public int m_duration; // 活动时长 单位：秒
	public int m_step_goal;
	public int m_sleep_deep;	// 单位：秒
	public int m_sleep_light;	// 单位：秒
	public int m_sleep_wakeup; // 单位：次
	public int m_step_hours[]; // 24小时运动步数
	public int m_org_steps[];   
	public long m_time; //当天起始
	public long m_time_sleep;
	public long m_time_getup;
			// 记步原始数据
	public List<SleepDB1> m_org_sleeps;	// 睡眠原始数据
	public SleepQuality  m_day_sleep_quality;
	
	public DictDay()
	{
		m_time = 0;
		m_step = 0;
		m_calorie = 0;
		m_distance = 0;
		m_duration = 0;
		m_step_hours = new int[24];
		for(int i = 0; i < 24; i++) {
			m_step_hours[i] = 0;
		}
		m_step_goal = 0;
		m_sleep_deep = 0;
		m_sleep_light = 0;
		m_sleep_wakeup = 0;
		m_time_sleep = 0;
		m_time_getup = 0;
		
		m_org_steps = new int[288];
		for(int i = 0; i < 288; i++) {
			m_org_steps[i] = 0;
		}
		m_org_sleeps = new ArrayList<SleepDB1>();
		m_day_sleep_quality = SleepQuality.unkown;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "DictDay{" +
				"m_time=" + m_time +
				", m_step=" + m_step +
				", m_calorie=" + m_calorie +
				", m_distance=" + m_distance +
				", m_duration=" + m_duration +
				", m_sleep_deep=" + m_sleep_deep +
				", m_sleep_light=" + m_sleep_light +
				", m_sleep_wakeup=" + m_sleep_wakeup +
				", m_time_sleep=" + m_time_sleep +
				", m_time_getup=" + m_time_getup +
				'}';
	}

	public void readFromParcel(Parcel in) {
		m_time = in.readLong();
		m_step = in.readInt();
		m_calorie = in.readInt();
		m_distance = in.readInt();
		m_duration = in.readInt();	
		in.readIntArray(m_step_hours);
		m_step_goal = in.readInt();
		m_sleep_deep = in.readInt();
		m_sleep_light = in.readInt();
		m_sleep_wakeup = in.readInt();		
		m_time_sleep = in.readLong();
		m_time_getup = in.readLong();
		in.readIntArray(m_org_steps);
	}    

    
	private DictDay(Parcel in) {
		readFromParcel(in);
	}    
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeLong(m_time);
		dest.writeInt(m_step);
		dest.writeInt(m_calorie);
		dest.writeInt(m_distance);
		dest.writeInt(m_duration);	
		dest.writeIntArray(m_step_hours);
		dest.writeInt(m_step_goal);
		dest.writeInt(m_sleep_deep);
		dest.writeInt(m_sleep_light);
		dest.writeInt(m_sleep_wakeup);		
		dest.writeLong(m_time_sleep);
		dest.writeLong(m_time_getup);
		dest.writeIntArray(m_org_steps);
	}
	
	public static final Parcelable.Creator<DictDay> CREATOR = new Parcelable.Creator<DictDay>() {
		public DictDay createFromParcel(Parcel in) {
			return new DictDay(in);
		}

		public DictDay[] newArray(int size) {
			return new DictDay[size];
		}
	};	
}
