package com.spark.service.aidl;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class BatteryInfo implements Serializable, Parcelable{
    public static enum ChargerState {
        unknown, disCharger, chargering, chargedFull
    }	
    private static final long serialVersionUID = -6761008701448482831L;
    private int state;
    private int capacity;

    public BatteryInfo() {
		// TODO Auto-generated constructor stub
    	state = -1;
    	capacity = 0;
	}
    
    
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public boolean isChargering(){
    	if(state == 1 || state == 2){
    		return true;
    	}else{
    		return false;
    	}
    }

	public void readFromParcel(Parcel in) {
		state = in.readInt();
		capacity = in.readInt();
	}    
    
    
	private BatteryInfo(Parcel in) {
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
		dest.writeInt(state);
		dest.writeInt(capacity);		
	}
	
	public static final Parcelable.Creator<BatteryInfo> CREATOR = new Parcelable.Creator<BatteryInfo>() {
		public BatteryInfo createFromParcel(Parcel in) {
			return new BatteryInfo(in);
		}


		public BatteryInfo[] newArray(int size) {
			return new BatteryInfo[size];
		}
	};	
}
