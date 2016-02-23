package com.spark.service.aidl;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.spark.data.Table;

import java.io.Serializable;

/**
 * Created by WENFUMAN on 2014/12/19.
 */


@DatabaseTable(tableName = "t_nicealarm")
public class NiceAlarm implements Serializable,Table, Parcelable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6607771263629768510L;


	@DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private String account;//账号
    @DatabaseField(columnName = "address")
    private String address;//Ble设备地址
    @DatabaseField
    private boolean enabled;//使能
    @DatabaseField(columnName = "smart_enabled")
    private boolean smartEnabled;//使能
    @DatabaseField(columnName = "number")
    private int number;

    @DatabaseField(columnName = "repetition")
    private int repetition;//重复

    @DatabaseField(columnName = "time_min")
    private int timeMin;//

    @DatabaseField(columnName = "time_hour")
    private int timeHour;//
    
    public boolean isSmartEnabled() {
        return smartEnabled;
    }

    public void setSmartEnabled(boolean smartEnabled) {
        this.smartEnabled = smartEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "NiceAlarm{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", deviceAddress='" + address + '\'' +
                ", enabled=" + enabled +
                ", smartEnabled=" + smartEnabled +
                ", number=" + number +
                ", repetition=" + repetition +
                ", timeMin=" + timeMin +
                ", timeHour=" + timeHour +
                '}';
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
    public String getDeviceAddress() {
        return address;
    }
    public void setDeviceAddress(String deviceAddress) {
        this.address = deviceAddress;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public int getRepetition() {
        return repetition;
    }
    public void setRepetition(int repetition) {
        this.repetition = repetition;
    }
    public int getTimeMin() {
        return timeMin;
    }
    public void setTimeMin(int timeMin) {
        this.timeMin = timeMin;
    }
    public int getTimeHour() {
        return timeHour;
    }
    public void setTimeHour(int timeHour) {
        this.timeHour = timeHour;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

	public void readFromParcel(Parcel in) {
		id = in.readLong();
		account = in.readString();
		address = in.readString();
		if(in.readInt() == 1){
			enabled = true;
		}else{
			enabled = false;
		}
		if(in.readInt() == 1){
			smartEnabled = true;
		}else{
			smartEnabled = false;
		}
		number = in.readInt();
		repetition = in.readInt();
		timeMin = in.readInt();
		timeHour = in.readInt();
	}    
    
	public NiceAlarm() {
		// TODO Auto-generated constructor stub
	}
    
	private NiceAlarm(Parcel in) {
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
		dest.writeLong(id);
		dest.writeString(account);
		dest.writeString(address);
		if(enabled){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
		}
		if(smartEnabled){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
		}		
		dest.writeInt(number);
		dest.writeInt(repetition);
		dest.writeInt(timeMin);
		dest.writeInt(timeHour);			
	}
	
	public static final Parcelable.Creator<NiceAlarm> CREATOR = new Parcelable.Creator<NiceAlarm>() {
		public NiceAlarm createFromParcel(Parcel in) {
			return new NiceAlarm(in);
		}

		public NiceAlarm[] newArray(int size) {
			return new NiceAlarm[size];
		}
	};
}
