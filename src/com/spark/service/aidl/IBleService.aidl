package com.spark.service.aidl;
import com.spark.service.aidl.AIDLActivity;
import com.spark.service.aidl.BatteryInfo;
import com.spark.service.aidl.DictDay;
import com.spark.service.aidl.Radiation;
import com.spark.service.aidl.Device;
import com.spark.service.aidl.NiceAlarm;
 interface IBleService {
     boolean send(in byte[] byte_send);
     boolean localeInit();
     boolean readRssi();
     boolean connect( String address);
     void disconnect();
     void registerCallBack(AIDLActivity callback);
     BatteryInfo getRealBatteryInfo(); 
     DictDay getRealDictDay();  
     Radiation getRealRadiation();  
     int getUpdatePercent();
     int getConnectionState();
	 void clear();
	 
	 String getStr(String key);
	 int getInt(String key);
     boolean getBool(String key);
     long getLong(String key);
     float getFloat(String key);
     byte getByte(String key); 
     
	 void setStr(String key, String value);
     void setInt(String key, int value);
     void setBool(String key, boolean value);
     void setLong(String key, long value);
     void setFloat(String key, float value);
     void setByte(String key, byte value);   
     Device dbgetDevice();
     NiceAlarm getNiceAlarm(int number);
     void updateNiceAlarm(in NiceAlarm niceAlarm);   
     void resetSleepDataDataBase();
}
