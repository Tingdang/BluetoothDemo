/*
 * Copyright (c) 2015. Guangdong Spark Technology Co.,Ltd.
 * Author: WENFUMAN
 * Email: hehufuman@163.com
 * Mobile phone: +86 18033432336
 */

package com.spark.util;

import com.spark.bluetoothdemo.BuildConfig;


/**
 * Created by Administrator on 2014/11/6.
 */
public class Constant {
	public static final String SOFTWARE_ID = "Lenovo-October";
	/*********SharedPreferences store parameters as below**************/
	public static final String BLE_STATUS = "bleStatus";
	public static final String LOGIN_HISTORY = "loginHistory";
	public static final String HISTORY = "History";
	
	//与账号相关的参数,要转移到数据库中去，要不然会混乱
	public static final String HEAD_LOGO_MD5 = "headLogoMD5";
    public static final String CURRENT_USER = "currentUser";
	public static final String POWER_SAVING = "powerSaving";
	public static final String SEDENTARY_NOTIFICATION = "sedentaryNotification";
	public static final String CURRENT_ACCOUNT = "currentAccount";
	public static final String PWD = "password";
	public static final String HEAD_LOGO = "headLogo.jpg";
	public static final String DEST_INTERVALS = "destIntervals";
	public static final String DEST_STEPS = "destSteps";
	public static final String ENABLE_OR_DISABLE_FETALMOVEMENT = "enableOrDisableFetalMovement";
	public static final String ENABLE_OR_DISABLE_RADIATION = "enableOrDisableRadiation";
	public static final String NICEALARM = "niceAlarm";
	public static final String SHAREDATE = "shareDate";
	public static final String SHARENUMBER = "shareNumber";
	
	public static final String SHOCK_NOTIFICATION = "shockNotification";
	public static final String SYSYTEM_NOTIFICATION = "enableOrDisableSystemNotification";
	public static final String BIRTH_PACKAGE_NOTIFICATION = "BIRTH_PACKAGE_NOTIFICATION";
	public static final String EX_FETALMOVEMENT_NOTIFICATION = "EX_FETALMOVEMENT_NOTIFICATION";
	public static final String PREGNANT_CHECK_NOTIFICATION = "PREGNANT_CHECK_NOTIFICATION";
	public static final String HEIGHT_OF_UTERUS_NOTIFICATION = "HEIGHT_OF_UTERUS_NOTIFICATION";
	public static final String ABDOMINAL_CIRCUMFERENCE_NOTIFICATION = "ABDOMINAL_CIRCUMFERENCE_NOTIFICATION";

	//与设备相关的参数,要转移到数据库中去，要不然会混乱
	public static final String ENABLE_FETALMOVEMENT = "enableFetalMovementDateTime";	
	public static final String CURRENT_ADDRESS = "currentAddress";
	public static final String CURRENT_NAME = "currentName";	
	public static final String UPDATE_DATA = "update_data";
	public static final String UPDATE_RADIATION = "update_radiation";
	public static final String UPDATE_FETALMOVEMENT = "update_fetalMovement";
	public static final String CHARGING_DATE = "chargingDate";
	public static final String FRISTBIND = "fristbind";
	public static final String CLEAR_AFTER_UPDATE = "clearAfterUpdate";
	
	/*********SharedPreferences store parameters as above**************/
	 
    public static final String SERVICER = "http://121.40.137.250:80/spk-server/";
    //public static final String SERVICER = "http://192.168.30.47:8080/spk-server/";    
    public static final String IW_ZIP0 = "http://121.40.137.250/spark/appImg/ble_app_hrs.hex";
    public static final String IW_PNG = "http://121.40.137.250/spark/appImg/share";
    public static final String IW_PNG1 = "http://121.40.137.250/spark/appImg/fetal_d";
    public static final String FILE_TYPE_IMG = "img";
    
    public static final String IS_LOGIN = "isLogin";
    public static final String FORMAT_Y_M_D_H = "yyyy-MM-dd HH";
    public static final String FORMAT_Y_M_D_WEEKDAY = "yyyy年MM月dd日  E";    
    public static final String FORMAT_Y_M_D1 = "yyyy年MM月dd日";
    public static final String FORMAT_Y_M_D = "yyyy-MM-dd";
    public static final String FORMAT_M_D_H_M = "MM-dd HH:mm";
    public static final String FORMAT_M_D = "MM-dd";
    public static final String FORMAT_M_D1 = "MM月dd日";
    public static final String FORMAT_H_M = "HH:mm";
    public static final String FORMAT_H_M_S = "HH:mm:ss";
    public static final String FORMAT_D = "dd";
    
    
    public static final String REGISTER = SERVICER + "user/reg";
    public static final String OPT_REG = "reg";
    public static final String OPT_ACTIVATE = "activate";
    public static final String OPT_EDITPWD = "editPwd";
    public static final String OPT_UPDATE = "update";
    public static final String USER_UPDATE = SERVICER + "user/update";
    public static final String LOGIN = SERVICER + "auth/login";
    public static final String LOGIN_STATUS = SERVICER + "auth/isLogin";
    public static final String GET_SESSION = SERVICER + "auth/getSessionStatus";
    public static final String GET_AUTHCODE = SERVICER + "auth/getVerifyNum";
    public static final String AUTH = SERVICER + "auth/verify";
    public static final String GET_FILE = SERVICER + "file/get";
    public static final String CHECK_FILE_EXIST = SERVICER + "file/checkExist";
    public static final String USER_TYPE_MOBILE = "mobile";
    public static final String USER_TYPE_EMAIL = "email";
    public static final String USERINFO = "userinfo";
    public static final String USERKEY = "userKey";
    public static final String KEYTYPE = "keyType";
    public static final String OPT = "opt";
    
    public static final String FROM = "from";
    public static final String TO = FROM;
    public static final String EXPECTED_DATE_OF_CONFINEMENT = "expectedDate";
    public static final long DAY = 1000L * 60 * 60 * 24;
    public static final int DEFAULT_TARGET_STEP = 10000;
    public static final int MAX_TARGET_STEP = 30000;
    public static final int MIN_TARGET_STEP = 1000;
    
    public static final int MAX_TARGET_INTERVALS = 10;
    public static final int MIN_TARGET_INTERVALS = 0;
    
    public static boolean DEBUG = BuildConfig.DEBUG;
    public static String DFU_SERVICE  = "00001530-1212-efde-1523-785feabcd123";
    public static String DFU_CONTROL  = "00001531-1212-efde-1523-785feabcd123";
    public static String DFU_PACKET   = "00001532-1212-efde-1523-785feabcd123";

    private static void fillCheckSumByte(byte[] buf) {
        byte checksum;
        int i;
        checksum = 0;
        for (i = 0; i < buf.length - 1; i++)
            checksum = (byte) (checksum + buf[i]);
        checksum = (byte) (((~checksum) + 1) & 0x7F);
        buf[buf.length - 1] = checksum;
    }
    
    private static byte[] writeProtocolDataBytes(byte[] bytes) {
        int n = 0;
        int i, j, leastBit = 0;
        int bit7 = 0;
        int count = (bytes.length * 8 + 7 - 1) / 7;
        byte[] d = new byte[count];

        for (i = 0; i < bytes.length; i++) {
            for (j = 0; j < 8; j++) {
                leastBit = (bytes[i] >> j) & 0x01;
                d[n] |= leastBit << (bit7++);
                if (7 == bit7) {
                    bit7 = 0;
                    n++;
                }
            }
        }
        return d;
    }    
    
    public static byte[] writeByte(byte header, byte[] data) {
    	Trace.e("test", "input " + CHexConver.byte2HexStr(data, data.length));
        byte[] encodeData = writeProtocolDataBytes(data);
        Trace.e("test", "output " + CHexConver.byte2HexStr(encodeData, encodeData.length));
        final byte[] byte_send = new byte[3 + encodeData.length];
        byte_send[0] = header;
        byte_send[1] = (byte) (encodeData.length + 1);
        for (int i = 2; i < encodeData.length + 2; i++) {
            byte_send[i] = encodeData[i - 2];
        }
        fillCheckSumByte(byte_send);

        boolean isValid = isCheckSumValid(byte_send);
        if (!isValid) {
            throw new RuntimeException("runtime_exception");
        }
        return byte_send;
    }

    public static byte[] readProtocolDataBytes(byte[] s) {
        int n = 0;
        int bit8 = 0;
        int i, j;
        int leastBit = 0;
        int length = s.length;
        int count = length * 7 / 8;
        byte[] d = new byte[length];
        for (i = 0; i < length; i++) {
            for (j = 0; j < 7; j++) {
                leastBit = (s[i] >> j) & 0x01;
                d[n] |= leastBit << (bit8++);
                if (8 == bit8) {
                    bit8 = 0;
                    n++;
                }
            }
        }
        byte[] rt = new byte[count];
        System.arraycopy(d, 0, rt, 0, count);
        return rt;
    }


    public static byte[] stripByte(byte[] buf) {
        byte[] data = new byte[buf.length - 3];
        for (int i = 0; i < buf.length - 3; i++) {
            data[i] = buf[i + 2];
        }
        return data;
    }

    public static boolean isCheckSumValid(byte[] rcvBuff) {
        byte checksum;
        int i = 0;
        checksum = rcvBuff[0];
        for (i = 1; i < rcvBuff.length; i++) {
            if (rcvBuff[i] >= 0x80)
                return false;
            checksum = (byte) (checksum + rcvBuff[i]);
        }
        checksum &= 0x7F;
        if (0 == checksum)
            return true;
        return false;
    }
}
