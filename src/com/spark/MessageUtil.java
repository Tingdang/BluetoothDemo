package com.spark;

import android.os.Handler;
import android.os.Message;

public class MessageUtil{
    public static final int ACTION_BIND = 10002;
    public static final int CONNECT = ACTION_BIND + 1;
    public static final int BATTERYINFO = CONNECT + 1;
    public static final int REQUEST_BIND_BACK = BATTERYINFO + 1;
    public static final int UPDATE_BACK = REQUEST_BIND_BACK + 1;
    public static final int DISCONNECT = UPDATE_BACK + 1;
    public static final int RealTimeFetalMovement = DISCONNECT + 1;
    public static final int NICEALARM_DATA = RealTimeFetalMovement + 1;
    public static final int RADIATION_DATA = NICEALARM_DATA + 1;
    public static final int RADIATION_DATA1 = RADIATION_DATA + 1;
    public static final int RSSI_DATA = RADIATION_DATA1 + 1;
    public static final int ACTION_REAL_TIME_STEP_DATA = RSSI_DATA + 1;
    public static final int FIRMWAREVERSIONS_BACK = ACTION_REAL_TIME_STEP_DATA + 1;
    public static final int UPDATA_MODE = FIRMWAREVERSIONS_BACK + 1;
    public static final int PROCESS_RUN = UPDATA_MODE + 1;
    public static final int CLOSE_FETALMOVEMENT = PROCESS_RUN + 1;
    public static final int CHECK_TIMEOUT = CLOSE_FETALMOVEMENT + 1;
    public static final int LOGCAT_INFO = CHECK_TIMEOUT + 1;
    
    private static Handler curHandler = null;
    
    public static void setCurHandler(Handler curHandler) {
        MessageUtil.curHandler = curHandler;
    }
    
    public static void sendMessage(Message message){
        if (null != curHandler) {
            curHandler.sendMessage(message);
        }	    	
    }
}
