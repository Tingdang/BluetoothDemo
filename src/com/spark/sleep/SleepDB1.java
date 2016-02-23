package com.spark.sleep;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.spark.util.Constant;
import com.spark.util.Util;

@DatabaseTable(tableName = "t_sleepDB1")
public class SleepDB1 {
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	private long id;
	@DatabaseField(columnName = "start_time")
	private long dateFirst;
	@DatabaseField(columnName = "end_time")
	private long dateLast;
	@DatabaseField(columnName = "type")
	private SleepState state;

	public SleepDB1() {
		// TODO 自动生成的构造函数存根
	}
	
	public SleepDB1(long tempLightSleepFirst, long tempLightSleepLast, SleepState lightSleep) {
		this.dateFirst = tempLightSleepFirst;
		this.dateLast = tempLightSleepLast;
		this.state = lightSleep;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getDateFirst() {
		return dateFirst;
	}

	public void setDateFirst(long dateFirst) {
		this.dateFirst = dateFirst;
	}

	public long getDateLast() {
		return dateLast;
	}

	public void setDateLast(long dateLast) {
		this.dateLast = dateLast;
	}

	public SleepState getState() {
		return state;
	}

	public void setState(SleepState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "sleepDB1{" +
				"id=" + id +
				", dateFirst=" + Util.DateToString(new Date(dateFirst * 1000l), Constant.FORMAT_M_D_H_M) +
				", dateLast=" + Util.DateToString(new Date(dateLast * 1000l), Constant.FORMAT_M_D_H_M) +
				", state=" + state +
				'}';
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(this.id == ((SleepDB1)o).getId()) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return (int)this.id;
	}
}
