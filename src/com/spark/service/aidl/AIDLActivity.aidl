package com.spark.service.aidl;  
import com.spark.service.aidl.BatteryInfo;
import com.spark.service.aidl.DictDay;
import com.spark.service.aidl.Radiation;
import com.spark.service.aidl.NiceAlarm;
interface AIDLActivity {   
    void RealBatteryInfo(in BatteryInfo para); 
    void RealDictDay(in DictDay para);  
    void RealRadiation(in Radiation para);
    void RealNiceAlarm(in NiceAlarm para);
    void RealUpdatePercent(int para, boolean isSuccess); 
    void RealRssi(int Rssi);
    void RealMessage(int para);  
}  
