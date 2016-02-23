package com.spark.service.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class LogcatInfo implements Parcelable{
   private String text;
   private int color;
   
   public LogcatInfo(int color, String text) {
	   // TODO Auto-generated constructor stub
	   this.color = color;
	   this.text = text;
   }
   
	public String getText() {
		return text;
	}

	public int getColor() {
		return color;
	}


	public void readFromParcel(Parcel in) {
		color = in.readInt();
		text = in.readString();
	}    
    
    
	private LogcatInfo(Parcel in) {
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
		dest.writeInt(color);
		dest.writeString(text);		
	}
	
	public static final Parcelable.Creator<LogcatInfo> CREATOR = new Parcelable.Creator<LogcatInfo>() {
		public LogcatInfo createFromParcel(Parcel in) {
			return new LogcatInfo(in);
		}


		public LogcatInfo[] newArray(int size) {
			return new LogcatInfo[size];
		}
	};	
}
