package com.spark.sleep;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.spark.util.Constant;
import com.spark.util.Util;

@DatabaseTable(tableName = "t_sleepDB2")
public class SleepDB2 {
	@DatabaseField(generatedId = true)
	private long id;
	@DatabaseField(columnName = "start_time")
	private long startTime;
	@DatabaseField(columnName = "end_time")
	private long endTime;
	@DatabaseField(columnName = "deep_sleep")
	private int deepSleep;
	@DatabaseField(columnName = "light_sleep")
	private int lightSleep;
	@DatabaseField(columnName = "sober_cnt")
	private int soberCnt;
	@DatabaseField(columnName = "isUserSelf")
	private boolean isUserSelf;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public int getDeepSleep() {
		return deepSleep;
	}
	public void setDeepSleep(int deepSleep) {
		this.deepSleep = deepSleep;
	}
	public int getLightSleep() {
		return lightSleep;
	}
	public void setLightSleep(int lightSleep) {
		this.lightSleep = lightSleep;
	}
	public int getSoberCnt() {
		return soberCnt;
	}
	public void setSoberCnt(int soberCnt) {
		this.soberCnt = soberCnt;
	}
	public boolean getUserSelf() {
		return isUserSelf;
	}
	public void setUserSelf(boolean isUserSelf) {
		this.isUserSelf = isUserSelf;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "sleepDB2{" +
				"id=" + id +
				", start_time=" + Util.DateToString(new Date(startTime * 1000l), Constant.FORMAT_M_D_H_M) +
				", end_time=" + Util.DateToString(new Date(endTime * 1000l), Constant.FORMAT_M_D_H_M) +
				", deep_sleep=" + deepSleep +
				", light_sleep=" + lightSleep +
				", sober_cnt=" + soberCnt +
				'}';
	}

}
