package com.spark.data;

import java.io.Serializable;

public class SedentaryInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean enableSedentary;
	private int TimeLen;
	private int start_hour;
	private int start_min;
	private int end_hour;
	private int end_min;
	public SedentaryInfo() {
		// TODO Auto-generated constructor stub
	}
	public int getTimeLen() {
		return TimeLen;
	}
	public void setTimeLen(int timeLen) {
		TimeLen = timeLen;
	}
	public int getStart_hour() {
		return start_hour;
	}
	public void setStart_hour(int start_hour) {
		this.start_hour = start_hour;
	}
	public int getStart_min() {
		return start_min;
	}
	public void setStart_min(int start_min) {
		this.start_min = start_min;
	}
	public int getEnd_hour() {
		return end_hour;
	}
	public void setEnd_hour(int end_hour) {
		this.end_hour = end_hour;
	}
	public int getEnd_min() {
		return end_min;
	}
	public void setEnd_min(int end_min) {
		this.end_min = end_min;
	}
	public boolean isEnableSedentary() {
		return enableSedentary;
	}
	public void setEnableSedentary(boolean enableSedentary) {
		this.enableSedentary = enableSedentary;
	}
}
