package com.spark.sleep;

public class SleepRecord {
	private int Tick;
	private int SleepValue;
	
	public SleepRecord(int tick, int value) {
		// TODO 自动生成的构造函数存根
		this.Tick = tick;
		this.SleepValue = value;
	}
	
	public void setTick(int tick) {
		this.Tick = tick;
	}
	public void setSleepValue(int value) {
		this.SleepValue |= value;
	}
	public int getTick() {
		return this.Tick;
	}
	public int getSleepValue() {
		return this.SleepValue;
	}
}
