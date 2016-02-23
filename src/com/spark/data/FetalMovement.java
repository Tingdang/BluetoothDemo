/*
 * Copyright (c) 2015. Guangdong Spark Technology Co.,Ltd.
 *     Author: WENFUMAN
 *     Email: hehufuman@163.com
 *     Mobile phone: +86 18033432336
 */

package com.spark.data;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2015/7/16.
 */
@DatabaseTable(tableName = "fetal_movement")
public class FetalMovement implements Serializable,Table{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(columnName = "date_time")
    private int dateTime;
    @DatabaseField
    private int type;    
    @DatabaseField
    private String address;
    @DatabaseField
    private String account;
    @DatabaseField(columnName = "time_flag")
    private String timeFlag;
    @DatabaseField(columnName = "date_flag")
    private String dateFlag;
    
    @Override
    public String toString() {
        return "FetalMovement{" +
                "id=" + id  + '\'' +
                ", dateTime=" + dateTime + '\'' +
                ", type='" + type + '\'' +
                ", address='" + address + '\'' +
                ", account='" + account + '\'' +
                ", timeFlag='" + timeFlag + '\'' +
                ", dateFlag='" + dateFlag + '\'' +
                '}';
    }

	public int getDateTime() {
		return dateTime;
	}

	public void setDateTime(int dateTime) {
		this.dateTime = dateTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getTimeFlag() {
		return timeFlag;
	}

	public void setTimeFlag(String timeFlag) {
		this.timeFlag = timeFlag;
	}

	public String getDateFlag() {
		return dateFlag;
	}

	public void setDateFlag(String dateFlag) {
		this.dateFlag = dateFlag;
	}

	@Override
	public void setId(long id) {
		// TODO Auto-generated method stub
		this.id = id;
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return id;
	}
    
    public final static int start = 1;
    public final static int normal = 0;
    public final static int over = -1;
}
