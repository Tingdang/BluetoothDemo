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
 * Created by wenhehu on 15/7/1.
 */

@DatabaseTable(tableName = "t_schedule")
public class Schedule implements Serializable,Table{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String toString() {
        return "Schedule{" +
                "account='" + account + '\'' +
                ", id=" + id +
                ", value=" + value +
                ", dateFlag=" + dateFlag +
                ", type=" + type +
                '}';
    }
            @DatabaseField
    private String account;
    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private float value;

    @DatabaseField(columnName = "date_flag")
    private int dateFlag;//日期标示

    @DatabaseField(columnName = "type")
    private Type type;//日期标示

    public int getDateFlag() {
        return dateFlag;
    }

    public void setDateFlag(int dateFlag) {
        this.dateFlag = dateFlag;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public enum Type{
        weight,//体重
        abdominalCircumference,//腹围
        uterineHeight,//宫高
        pregnantCheck,//产检
        birthPackage,//产包准备
    }

	@Override
	public void setId(long id) {
		// TODO Auto-generated method stub
		this.id = id;
	}
}
