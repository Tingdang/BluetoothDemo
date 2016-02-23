package com.spark.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by WENFUMAN on 2014/11/24.
 */
@DatabaseTable(tableName = "t_data")
public class Data implements Serializable,Table {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7198446180865687404L;

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private String account;//账号
    @DatabaseField(columnName = "address")
    private String address;//Ble设备地址
    @DatabaseField
    private float distance;//距离

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @DatabaseField
    private int steps;//步数
    @DatabaseField
    private long duration;//持续时间|运动时长
    @DatabaseField
    private int sleep;//睡眠时间
    @DatabaseField
    private int calorie;//卡路里
    @DatabaseField(columnName = "date_time")
    private int dateTime;//日期时间,dataType = DataType.DATE_TIME
    @DatabaseField(columnName = "date_flag")
    private String dateFlag;

    public Data() {
		// TODO Auto-generated constructor stub
	}
    
    public Data(int dateTime) {
    	this.sleep = 0;
    	this.dateTime = dateTime;
    }
    
    public Data(int dateTime, int state) {
    	this.sleep = state;
    	this.dateTime = dateTime;
    }
    
    public String getTimeFlag() {
        return timeFlag;
    }

    public void setTimeFlag(String timeFlag) {
        this.timeFlag = timeFlag;
    }

    @DatabaseField(columnName = "time_flag")
    private String timeFlag;


    public String getDateFlag() {
        return dateFlag;
    }

    public void setDateFlag(String dateFlag) {
        this.dateFlag = dateFlag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getCalorie() {
        return calorie;
    }

    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public int getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "Data{" +
        "distance=" + distance +
        ", steps=" + steps +
        ", duration=" + duration +
        ", sleep=" + sleep +
        ", calorie=" + calorie +
        ", dateTime=" + dateTime +
        '}';
    }

    public void setDateTime(int dateTime) {
        this.dateTime = dateTime;
    }
}
