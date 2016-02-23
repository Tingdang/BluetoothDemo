package com.spark.service.aidl;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.spark.data.Table;

import java.io.Serializable;

/**
 * Created by WENFUMAN on 2014/11/24.
 */
@DatabaseTable(tableName = "t_radiation")
public class Radiation implements Serializable,Table, Parcelable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -7875908922549348224L;
    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(indexName = "idx_rad")
    private String account;//账号
    @DatabaseField(columnName = "address")
    private String address;//Ble设备地址
    @DatabaseField(columnName = "date_flag", indexName = "idx_rad")
    private String dateFlag;
    @DatabaseField(columnName = "time_flag", indexName = "idx_rad")
    private String timeFlag;
    @DatabaseField(columnName = "number", indexName = "idx_rad")
    private int number;
    @DatabaseField(columnName = "level", indexName = "idx_rad")
    private int level;
    @DatabaseField(columnName = "sum", indexName = "idx_rad")
    private int sum;
    @DatabaseField
    private float radiation;//持续时间|运动时长
    @DatabaseField(columnName = "date_time")
    private int dateTime;//日期时间,dataType = DataType.DATE_TIME    
    
    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTimeFlag() {
        return timeFlag;
    }

    public void setTimeFlag(String timeFlag) {
        this.timeFlag = timeFlag;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateFlag() {
        return dateFlag;
    }

    public void setDateFlag(String dateFlag) {
        this.dateFlag = dateFlag;
    }

    public float getRadiation() {
        return radiation;
    }

    public int getDateTime() {
        return dateTime;
    }

    public void setDateTime(int dateTime) {
        this.dateTime = dateTime;
    }

    public void setRadiation(float radiation) {
        this.radiation = radiation;
    }

    public RadiationState getRadiationState() {
    	if(level == 0){
    		return RadiationState.none;
    	}else if(level == 1){
    		return RadiationState.slight;
    	}else if(level == 2){
    		return RadiationState.moderate;
    	}else{
            if (radiation < 0.1) {
            	return RadiationState.none;
            }    		
    		return RadiationState.severity;
    	}
    	
//        if (radiation < 0.1) {
//            return RadiationState.none;
//        } else if ((radiation >= 0.1) && (radiation < 0.4)) {
//            return RadiationState.slight;
//        } else if ((radiation >= 0.4) && (radiation < 4.5)) {
//            return RadiationState.moderate;
//        } else {
//            return RadiationState.severity;
//        }
    }

    public enum RadiationState {
        none,
        slight,
        moderate,
        severity
    }

    @Override
    public String toString() {
        return "Radiation{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", address='" + address + '\'' +
                ", dateFlag='" + dateFlag + '\'' +
                ", timeFlag='" + timeFlag + '\'' +
                ", radiation=" + radiation +
                ", dateTime=" + dateTime +
                '}';
    }
    
	public Radiation() {
	} 
    
    
	public void readFromParcel(Parcel in) {
		id = in.readLong();
		account = in.readString();
		address = in.readString();
		dateFlag = in.readString();
		timeFlag = in.readString();	
		sum = in.readInt();
		level = in.readInt();
		number = in.readInt();	
		dateTime = in.readInt();
		radiation = in .readFloat();
	}    
    
    
	private Radiation(Parcel in) {
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
		dest.writeString(dateFlag);
		dest.writeString(timeFlag);
		dest.writeInt(sum);
		dest.writeInt(level);
		dest.writeInt(number);
		dest.writeInt(dateTime);
		dest.writeFloat(radiation);
	}
	
	public static final Parcelable.Creator<Radiation> CREATOR = new Parcelable.Creator<Radiation>() {
		public Radiation createFromParcel(Parcel in) {
			return new Radiation(in);
		}

		public Radiation[] newArray(int size) {
			return new Radiation[size];
		}
	};	    
}
