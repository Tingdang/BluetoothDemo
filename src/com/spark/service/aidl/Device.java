package com.spark.service.aidl;


import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.spark.data.Table;

import java.io.Serializable;

/**
 * Created by Administrator on 2014/8/5.
 */
@DatabaseTable(tableName = "t_device")
public class Device implements Serializable,Table, Parcelable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6074321977003874049L;
	public static final String ADDRESS = "address";
    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(columnName = "account")
    private String account;//
    @DatabaseField(columnName = "address")
    private String address;//ble设备通讯地址;
    @DatabaseField(columnName = "firmware_version")
    private String firmwareVersion;//固件版本;
    @DatabaseField(columnName = "hardware_version")
    private String hardwareVersion;//硬件版本
    @DatabaseField(columnName = "vacul_elec")
    private String manufacturer;//生产厂商
    @DatabaseField(columnName = "manufacturer")
    private String serialNumber;//序列号
    @DatabaseField(columnName = "version_value")
    private int versionValue;//版本值，用于判断是否可以升级
    @DatabaseField(columnName = "version")
    private int version;
    @DatabaseField(columnName = "transducer_type")
    private int transducerType =1;
    
    public Device() {
		// TODO Auto-generated constructor stub
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getVersionValue() {
        return versionValue;
    }

    public void setVersionValue(int versionValue) {
        this.versionValue = versionValue;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", macAddress='" + address + '\'' +
                ", hardwareVersion='" + hardwareVersion + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", versionValue=" + versionValue + '\'' +
                ", version=" + version +
                '}';
    }

    public TransducerType getTransducerType() {
        return transducerType==2?TransducerType.type_st:TransducerType.type_362;
    }

    public void setTransducerType(int transducerType) {
        this.transducerType = transducerType;
    }

    private enum TransducerType{
        type_362,
        type_st
    }

	public void readFromParcel(Parcel in) {
		id = in.readLong();
		account = in.readString();
		address = in.readString();
		hardwareVersion = in.readString();
		firmwareVersion = in.readString();
		manufacturer = in.readString();
		serialNumber = in.readString();
		versionValue = in.readInt();
		version = in.readInt();
		transducerType = in.readInt();
	}         
    
    
	private Device(Parcel in) {
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
		dest.writeString(hardwareVersion);
		dest.writeString(firmwareVersion);
		dest.writeString(manufacturer);
		dest.writeString(serialNumber);
		dest.writeInt(versionValue);
		dest.writeInt(version);	
		dest.writeInt(transducerType);
	}
	
	public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
		public Device createFromParcel(Parcel in) {
			return new Device(in);
		}


		public Device[] newArray(int size) {
			return new Device[size];
		}
	};	
}
