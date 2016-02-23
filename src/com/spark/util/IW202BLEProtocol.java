package com.spark.util;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;

import com.spark.MessageUtil;
import com.spark.data.Data;
import com.spark.data.FetalMovement;
import com.spark.service.DfuService.MyBinder;
import com.spark.service.aidl.BatteryInfo;
import com.spark.service.aidl.Device;
import com.spark.service.aidl.DictDay;
import com.spark.service.aidl.NiceAlarm;
import com.spark.service.aidl.Radiation;
import android.os.RemoteException;


public class IW202BLEProtocol {
	private static final String TAG = IW202BLEProtocol.class.getSimpleName();
	private static final String CHARSET_UTF_8 = "utf-8";
	private static final int radiationDivisor = 10;
    private static final byte HEAD_StepGauge = (byte) 0x81;// 计步 实时数据
    private static final byte HEAD_TimeTheAlarmClock = (byte) 0x84;// 时间闹钟
    private static final byte HEAD_SynchronousMeterStep_SynchronousSleep = (byte) 0x90;// 历史数据
    private static final byte HEAD_SynchronousRadiation = (byte) 0x91;// 历史数据
    private static final byte HEAD_radiation = (byte) 0x92;// 历史数据
    private static final byte HEAD_NOTIFICATION = (byte) 0x93;// 历史数据
    private static final byte HEAD_FetalMovement = (byte) 0xA0;// 个人信息
    private static final byte HEAD_DeviceInformation = (byte) 0xF0;// 设备信息
    private static final byte HEAD_BatteryCapacity = (byte) 0xF1;// 电池电量

    private static final byte TOAPP_ID0 = (byte) 0x80;
    private static final byte TOAPP_ID1 = (byte) 0x81;
    private static final byte TOAPP_ID2 = (byte) 0x82;
    private static final byte TOAPP_ID3 = (byte) 0x83;
    private static final byte TOAPP_ID4 = (byte) 0x84;
    private static final byte TOAPP_ID5 = (byte) 0x85;	
    private static IW202BLEProtocol protocol;
	private ConcurrentLinkedQueue<Byte> queue = new ConcurrentLinkedQueue<Byte>();
    private byte[] mergePackage = new byte[1024];
    private MyBinder bleService = null;
    private String account = "";
    private String address = "";
    
    private int index = 0;	
    private int sum = 0;
    private int DataCnt = 0;
    private int sumAll = 0;
    private int SkipDataCnt = 0;
    private int DataCnt1 = 0;
    private int lastTick = 0;
    private boolean isOpenRadiation = false;
    private boolean isOpenFetal = false;
    private boolean isOpen = false;
    private boolean bSyncError = false;
    
    private IW202BLEProtocol() {
    }

    public static synchronized IW202BLEProtocol getInstance() {
        if (protocol == null) {
            protocol = new IW202BLEProtocol();
        }
        return protocol;
    }    
    
    private void noticeSynchronizationResult(boolean isSuccess){
//    	DfuService.isUpdate = false;
//    	Trace.e("isUpdate", "4.BleService.isUpdate = " + BleService.isUpdate);
    	boolean isClear = bleService.getBool(Constant.CLEAR_AFTER_UPDATE);
    	if(isClear){
    	      if (isSuccess == true) {
    		      try {
    		      	bleService.synchronizationSuccess();
    		      } catch (RemoteException e) {
    		          e.printStackTrace();
    		      }
    		  } else {
    		      try {
    		      	bleService.synchronizationFailure();
    		      } catch (RemoteException e) {
    		          e.printStackTrace();
    		      }
    		  }     		
    	}
    }    
    
    private void updateProgress(int Counter, boolean isSuccess){
    	if(sumAll == 0){
        	if(Counter != bleService.getUpdatePercent()){
        		
        		try {
        			bleService.setUpdatePercent(Counter);
					bleService.getActivityCallback().RealUpdatePercent(Counter, isSuccess);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}    		
        	} 
    	}else{
        	int newPercent = (100 * (DataCnt + Counter))/ sumAll;
        	if(newPercent != bleService.getUpdatePercent()){
        		try {
        			bleService.setUpdatePercent(newPercent);
					bleService.getActivityCallback().RealUpdatePercent(newPercent, isSuccess);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   		
        	}    		
    	}
    }
    
    public synchronized void mergePackage(MyBinder myBinder, byte[] data) {
        this.bleService = myBinder;
        
        // StringBuffer stringBuffer = new StringBuffer("queue=");
        for (byte b : data) {
            queue.offer(b);
        }
        int[] indexTemp = new int[10];// 默认一个包最多十个帧数据
        int count = 0;// 帧个数
        while (!queue.isEmpty()) {
            int temp = 0;
            try {
                if (index > 1023)
                    break;
                temp = mergePackage[index] = queue.poll();// 将取到的包的数据填入byte数组
            } catch (Exception e) {
                Trace.e(TAG, "___", e);
            }
            temp &= 0xFF;
            if (127 < temp) {// 如果有byte数值大于0x7F，则算是包头，即一个帧的开始
                if (count > 9)
                    break;
                indexTemp[count] = index;// 将描述帧数据包头的位置的字段存起来
                count++;
            }
            // stringBuffer.append("  " +
            // Integer.toHexString(mergePackage[index] & 0xFF));
            index++;
        }
        // Trace.e(TAG, stringBuffer.toString());
        boolean flag = false;// 判断最后一个帧数据是否缺失
        /*
         * 首先判断最后帧的下标位置 然后位置加一为帧的数据长度的下标
		 */
        if (count > 0
                && index == indexTemp[count - 1]
                + mergePackage[indexTemp[count - 1] + 1] + 2) {
            flag = true;
        } else {
            flag = false;
        }
        // Trace.e(TAG, "flag=" + flag);
        if (flag) {
            for (int i = 0; i < count; i++) {
                int temp = mergePackage[indexTemp[i] + 1];
                temp &= 0xFF;
                temp += 2;
                byte[] bytes = new byte[temp];

                System.arraycopy(mergePackage, indexTemp[i], bytes, 0, temp);
                // for (int j = 0; j < temp; j++) {
                // bytes[j] = mergePackage[indexTemp[i] + j];
                // }
                filterHead(bytes);
            }
        } else {
            if (count > 1) {
                for (int i = 0; i < count - 1; i++) {
                    int temp = mergePackage[indexTemp[i] + 1];
                    temp &= 0xFF;
                    temp += 2;
                    byte[] bytes = new byte[temp];
                    System.arraycopy(mergePackage, indexTemp[i], bytes, 0, temp);
                    filterHead(bytes);
                }
                for (int i = indexTemp[count - 1]; i < index; i++) {
                    queue.offer(mergePackage[i]);
                }
            } else {
                for (int i = 0; i < index; i++) {
                    queue.offer(mergePackage[i]);
                }
            }
        }
        index = 0;
    }
    
    private synchronized void filterHead(byte[] data) {
        if (!Constant.isCheckSumValid(data))
            return;
            
        account = bleService.getStr(Constant.CURRENT_ACCOUNT);
        address = bleService.getStr(Constant.CURRENT_ADDRESS);

        byte[] temp = Constant.stripByte(data);
        byte[] stripData = Constant.readProtocolDataBytes(temp);

        switch (data[0]) {
            case HEAD_StepGauge:
            	if(!StringUtils.isEmpty(address)){
            		stepGauge(stripData);
            	}
                break;
            case HEAD_TimeTheAlarmClock:
            	if(StringUtils.isEmpty(address)){
            		break;
            	}
            	timeTheAlarmClock(stripData);
                break;

            case HEAD_SynchronousMeterStep_SynchronousSleep:
            	if(StringUtils.isEmpty(address)){
            		break;
            	}
                synchronousMeterStepSynchronousSleep(stripData);
                break;
            case HEAD_SynchronousRadiation:
            	if(StringUtils.isEmpty(address)){
            		break;
            	}            	
                synchronousRadiation(stripData);
                break;
            case HEAD_radiation:
            	if(StringUtils.isEmpty(address)){
            		break;
            	}            	
                radiation(stripData);
                break;
            case HEAD_NOTIFICATION:
                try {
                    notify(stripData);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case HEAD_FetalMovement:
            	if(StringUtils.isEmpty(address)){
            		break;
            	}  
                fetalMovement(stripData);
                break;

            case HEAD_DeviceInformation:
            	if(StringUtils.isEmpty(address)){
            		break;
            	}                	
                deviceInformation(stripData);
                break;
            case HEAD_BatteryCapacity:
            	if(StringUtils.isEmpty(address)){
            		break;
            	}                	
                batteryCapacity(stripData);
                break;
        }
    }   
    

    
    private void stepGauge(byte[] data) {
    	DictDay stepData = null;
        switch (data[0]) {
            case TOAPP_ID1:
                if (data.length == 9) {
                    stepData = new DictDay();
                    int temp1 = data[2];
                    int temp2 = data[1];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    stepData.m_step = (temp1 << 8) | temp2;
                    temp1 = data[4];
                    temp2 = data[3];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    stepData.m_calorie = (temp1 << 8) | temp2;
                    temp1 = data[6];
                    temp2 = data[5];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    stepData.m_distance = (temp1 << 8) | temp2;
                    temp1 = data[8];
                    temp2 = data[7];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    stepData.m_duration = (temp1 << 8) | temp2;
                } else if (data.length == 7) {
                    stepData = new DictDay();
                    int temp1 = data[2];
                    int temp2 = data[1];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    stepData.m_step = (temp1 << 8) | temp2;
                    temp1 = data[4];
                    temp2 = data[3];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    stepData.m_calorie = (temp1 << 8) | temp2;
                    temp1 = data[6];
                    temp2 = data[5];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    stepData.m_distance = (temp1 << 8) | temp2;
                }
            /*
             * ble设备上传计步器实时值（8Bytes 无符号数） 步数 卡路里 距离 运动时长 2Bytes 2Bytes 2Bytes
			 * 2Bytes 说明：在数据大于65535后会自动清零 低字节先传，周期测量打开后，如果记步数据改变就上传计步值
			 * 步数、卡路里(kcal)、距离(m)、运动时长(min)是每日数据累加，每天晚上24.00点清零。
			 */
                break;
            case TOAPP_ID2:
                if (data.length == 11) {
                    stepData = new DictDay();
//                    int temp1 = data[5];
//                    int temp2 = data[4];
//                    int temp3 = data[3];
//                    int temp4 = data[2];
//                    temp1 &= 0xFF;
//                    temp2 &= 0xFF;
//                    temp3 &= 0xFF;
//                    temp4 &= 0xFF;
//                    int targer1 = (temp1 << 24) | (temp2 << 16) | (temp3 << 8) | temp4;
//                    temp1 = data[9];
//                    temp2 = data[6];
//                    temp3 = data[7];
//                    temp4 = data[6];
//                    temp1 &= 0xFF;
//                    temp2 &= 0xFF;
//                    temp3 &= 0xFF;
//                    temp4 &= 0xFF;
//                    int targer2 = (temp1 << 24) | (temp2 << 16) | (temp3 << 8) | temp4;
//                    Trace.e(TAG, "targer1:" + targer1 + "  targer2:" + targer2);
                }
                break;
            case TOAPP_ID3:
                break;
        }
        
        try {
        	bleService.setRealDictDay(stepData);
        	bleService.getActivityCallback().RealDictDay(stepData);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }  
    
    
    private void timeTheAlarmClock(byte[] data) {
        if (data.length == 5 && data[0] == TOAPP_ID1) {
            try {
                NiceAlarm niceAlarm = bleService.dbHelper.getNiceAlarm(account, address, data[1]);
                int temp = 0xFF;
                if (null == niceAlarm) {
                    niceAlarm = new NiceAlarm();
                    niceAlarm.setNumber(data[1] & temp);
                    niceAlarm.setTimeMin(data[4] & temp);
                    niceAlarm.setTimeHour(data[3] & temp);
                    niceAlarm.setRepetition(data[2] & temp);
                    niceAlarm.setAccount(account);
                    niceAlarm.setDeviceAddress(address);
                    if (niceAlarm.getRepetition() == 0) {
                        niceAlarm.setEnabled(false);
                    } else {
                        niceAlarm.setEnabled(true);
                    }
                    bleService.dbHelper.createNiceAlarm(niceAlarm);
                } else {
                    niceAlarm.setNumber(data[1] & temp);
                    niceAlarm.setTimeMin(data[4] & temp);
                    niceAlarm.setTimeHour(data[3] & temp);
                    niceAlarm.setRepetition(data[2] & temp);
                    if (niceAlarm.getRepetition() == 0) {
                        niceAlarm.setEnabled(false);
                    } else {
                        niceAlarm.setEnabled(true);
                    }
                    bleService.dbHelper.updateNiceAlarm(niceAlarm);
                }
                try {
					bleService.getActivityCallback().RealNiceAlarm(niceAlarm);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}                
            } catch (Exception e) {
                e.printStackTrace();
                Trace.e(TAG, "解析并保存闹钟信息时出错", e);
            }
        } else if (data.length == 7 && data[0] == TOAPP_ID1) {
            try {
                NiceAlarm niceAlarm = bleService.dbHelper.getNiceAlarm(account, address, data[1]);
                int temp = 0xFF;
                if (null == niceAlarm) {
                    niceAlarm = new NiceAlarm();
                    niceAlarm.setNumber(data[1] & temp);
                    niceAlarm.setRepetition(data[2] & temp);
                    niceAlarm.setTimeHour(data[3] & temp);
                    niceAlarm.setTimeMin(data[4] & temp);
                    niceAlarm.setAccount(account);
                    niceAlarm.setDeviceAddress(address);
                    niceAlarm.setEnabled((data[5] & temp) == 1);
                    niceAlarm.setSmartEnabled((data[6] & temp) == 1);
//                if (niceAlarm.getRepetition() == 0) {
//                    niceAlarm.setEnabled(false);
//                } else {
//                    niceAlarm.setEnabled(true);
//                }
                    bleService.dbHelper.createNiceAlarm(niceAlarm);
                } else {
                    niceAlarm.setNumber(data[1] & temp);
                    niceAlarm.setTimeMin(data[4] & temp);
                    niceAlarm.setTimeHour(data[3] & temp);
                    niceAlarm.setRepetition(data[2] & temp);
                    niceAlarm.setEnabled((data[5] & temp) == 1);
                    niceAlarm.setSmartEnabled((data[6] & temp) == 1);
                    if (niceAlarm.getRepetition() == 0) {
                        niceAlarm.setEnabled(false);
                    } else {
                        niceAlarm.setEnabled(true);
                    }
                    bleService.dbHelper.updateNiceAlarm(niceAlarm);
                }
                try {
					bleService.getActivityCallback().RealNiceAlarm(niceAlarm);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
            } catch (Exception e) {
                e.printStackTrace();
                Trace.e(TAG, "解析并保存闹钟信息时出错", e);
            }
        }
    }    
    
    
	private synchronized void synchronousMeterStepSynchronousSleep(byte[] data) {
		switch (data[0]) {
		case TOAPP_ID5:
			if (data.length == 5 && isOpen == false) {
				int sum1 = data[2]&0xff;
				int sum2 = data[1]&0xff;
				int sum3 = data[4]&0xff;
				int sum4 = data[3]&0xff;	
				sum = (sum1 << 8) | sum2; 
				Trace.e(TAG, "synchronousMeterStepSynchronousSleep sum = " + sum);
				sumAll = (sum3 << 8) | sum4; 
				DataCnt = 0;
				Trace.e(TAG, "sumAll = " + sumAll);
				DataCnt1 = 0;
				SkipDataCnt = 0;
				lastTick = 0;
				isOpen = true;				
			}break;

		case TOAPP_ID3:
			if(data.length == 3 && isOpen == true){
				Trace.e(TAG, "DataCnt1 = " + DataCnt1);
				if(DataCnt1 > 0){	
					DataCnt = DataCnt1;
					DataCnt1 = 0;	
					if(bSyncError) {
						bSyncError = false;
					}
					
					bleService.center.updateAfterFresh(address);

					Date tmp = new Date();
					bleService.setLong(Constant.UPDATE_DATA, tmp.getTime());
				}else{
					noticeSynchronizationResult(false);
				}
				isOpen = false;
			}
			break;
		case TOAPP_ID4:
			if(data.length == 17 && isOpen == true) {
				Data data1 = new Data();
				int temp1 = data[8] & 0xFF;
				int temp2 = data[7] & 0xFF;
				data1.setSteps((temp1 << 8) | temp2);
				temp1 = data[10] & 0xFF;
				temp2 = data[9] & 0xFF;
				data1.setCalorie((temp1 << 8) | temp2);
				temp1 = data[12] & 0xFF;
				temp2 = data[11] & 0xFF;
				data1.setDistance((temp1 << 8) | temp2);
				temp1 = data[14] & 0xFF;
				temp2 = data[13] & 0xFF;
				data1.setSleep((temp1 << 8) | temp2);
				temp1 = data[16] & 0xFF;
				temp2 = data[15] & 0xFF;
				data1.setDuration((temp1 << 8) | temp2);
				
				temp1 = data[6] & 0xFF;
				temp2 = data[5] & 0xFF;
				int temp3 = data[4] & 0xFF;
				int temp4 = data[3] & 0xFF;
				int tick = (temp1 << 24) | (temp2 << 16) | (temp3 << 8) | temp4;
				
				if(tick < lastTick) {
					Trace.e(TAG, "datas bSyncError true:tick/lastTick = " + tick +"/"+lastTick);
					bSyncError = true;
				}else{
					bSyncError = false;
				}
				
				data1.setDateTime(tick);
				data1.setDateFlag(Util.DateToString(new Date(tick * 1000l)));
				data1.setTimeFlag(Util.DateToString(new Date(tick * 1000l),Constant.FORMAT_Y_M_D_H));
				data1.setAccount(account);
				data1.setAddress(address);
				if(!bSyncError) {
					boolean save = bleService.center.dbUtil.insertData(address, data1);
					lastTick = data1.getDateTime();
					Trace.e(TAG, "insertData DataCnt1 = " + DataCnt1+", save = " + save);
					DataCnt1++;
					if(DataCnt1 == 1){
						bleService.center.setStartTick(lastTick);
					}
				}else{
					SkipDataCnt++;
					Trace.e(TAG, "insertData SkipDataCnt = " + SkipDataCnt);
				}
				updateProgress(DataCnt1+SkipDataCnt, true);
			} 
			break;
		}
	}    
    
    
    private synchronized void synchronousRadiation(byte[] data) {
        switch (data[0]) {
            case TOAPP_ID1:
            case (byte) 0x91:
                if (data.length == 3 && isOpenRadiation == false) {
                    int sum1 = data[2];
                    int sum2 = data[1];
                    sum1 &= 0xFF;
                    sum2 &= 0xFF;
                    sum = (sum1 << 8) | sum2;
                    Trace.e(TAG, "synchronousRadiation sum = " + sum);
                    Trace.e(TAG, "DataCnt = " + DataCnt);
                    DataCnt1 = 0;
                    isOpenRadiation = true;
                }
                break;
            case TOAPP_ID2:
            case (byte) 0x92:
                if (data.length == 11 && isOpenRadiation) {
                    Radiation radiation = new Radiation();
                    int temp1 = data[6];
                    int temp2 = data[5];
                    int temp3 = data[4];
                    int temp4 = data[3];
                    //Trace.e(TAG, "1:" + temp1 + " 2:" + temp2 + " 3:" + temp3 + " 4:" + temp4);
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    temp3 &= 0xFF;
                    temp4 &= 0xFF;
                    //Trace.e(TAG, "1:" + temp1 + " 2:" + temp2 + " 3:" + temp3 + " 4:" + temp4);
                    long a = (temp1 << 24) + (temp2 << 16) + (temp3 << 8) + temp4;
                    //Trace.e(TAG, "a:" + a);
                    a &= 0xFFFFFFFF;
                    //Trace.e(TAG, "a:" + a);
                    radiation.setDateTime((int) (a));
                    Date date1 = new Date();
                    date1.setTime(a * 1000L);
                    radiation.setDateFlag(Util.DateToString(date1));
                    //Trace.e(TAG, radiation.toString());
                    radiation.setTimeFlag(Util.DateToString(date1, Constant.FORMAT_H_M));
                    temp1 = data[2];
                    temp2 = data[1];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    int number = (int) ((temp1 << 8) | temp2);
                    radiation.setNumber(number);
//                    Date date = getDate((int) ((temp1 << 8) | temp2));
//                    radiation.setDateFlag(Util.DateToString(date));
//                    radiation.setDateTime((int) (date.getTime() / 1000));
//                    radiation.setTimeFlag(Util.DateToString(date, Constant.FORMAT_H_M));
                    radiation.setAccount(account);
                    radiation.setAddress(address);
                    temp1 = data[7];
                    temp1 &= 0xFF;
                    radiation.setLevel((int) temp1);
                    temp1 = data[9];
                    temp2 = data[8];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    int b = (int) ((temp1 << 8) | temp2);
                    float val = (float)b / radiationDivisor;
                    radiation.setRadiation(val);
                    try {
                    	bleService.dbHelper.saveRadiation(radiation);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    DataCnt1++;
                    updateProgress(DataCnt1, true);                  
                }
                break;
            case (byte) 0x93:
            case TOAPP_ID3:
            	if(data.length == 3 && isOpenRadiation){
            		boolean insertSuccess = false;
            		Trace.e(TAG, "DataCnt1 = " + DataCnt1);  
            		if(DataCnt1 > 0){
                    	DataCnt += DataCnt1;
						Date tmp = new Date();
						bleService.setLong(Constant.UPDATE_RADIATION, tmp.getTime());
            		}
    				if(insertSuccess == false){
    					noticeSynchronizationResult(false);
    				} 
    				isOpenRadiation = false;
    				Trace.e(TAG, "synchronousRadiation DataCnt = " + DataCnt);
            	}               
                break;
        }
    }    
    
    
    private synchronized void radiation(byte[] data) {
        switch (data[0]) {
            case TOAPP_ID0:
                if (data.length == 8) {
                    int temp1 = data[1];
                    Radiation radiation = new Radiation();
                    radiation.setLevel(temp1);

                    temp1 = data[3];
                    int temp2 = data[2];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    float a = (temp1 << 8) | temp2;
                    a /= radiationDivisor;
                    radiation.setRadiation(a);
                    long a1 = data[7];
                    long a2 = data[6];
                    long a3 = data[5];
                    long a4 = data[4];
                    a1 &= 0xFF;
                    a2 &= 0xFF;
                    a3 &= 0xFF;
                    a4 &= 0xFF;
                    radiation.setSum((int) ((a1 << 24) + (a2 << 16) + (a3 << 8) + a4));
                    Trace.e(TAG, "Radiation = " + a);
                    try {
                    	bleService.setRealRadiation(radiation);
                    	bleService.getActivityCallback().RealRadiation(radiation);
            		} catch (RemoteException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}                    
                }break;  
            case TOAPP_ID1://测试命令，正式版本不执行
                if (data.length == 8 && Constant.DEBUG) {
                    int temp1 = data[1];
                    Radiation radiation = new Radiation();
                    radiation.setLevel(temp1);

                    temp1 = data[3];
                    int temp2 = data[2];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    float a = (temp1 << 8) | temp2;
                    radiation.setRadiation(a);
                    long a1 = data[7];
                    long a2 = data[6];
                    long a3 = data[5];
                    long a4 = data[4];
                    a1 &= 0xFF;
                    a2 &= 0xFF;
                    a3 &= 0xFF;
                    a4 &= 0xFF;
                    radiation.setSum((int) ((a1 << 24) + (a2 << 16) + (a3 << 8) + a4));
                    try {
                    	bleService.setRealRadiation(radiation);
                    	bleService.getActivityCallback().RealRadiation(radiation);
            		} catch (RemoteException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}                     
                }break;                  
        }
    }
    
    private void notify(byte[] data) throws RemoteException {
        Trace.e(TAG, "notify");
        switch (data[0]) {
            case TOAPP_ID0:
            	boolean notifly = bleService.getBool(Constant.SHOCK_NOTIFICATION);
                if (6 == data.length && notifly == true) {
//                    if (data[1] == 0x10) {
//                    	Util.notification(0, context, UtilActivity.class);
//                    }
//                    if (data[2] == 0x10) {
//                    	Util.notification(1, context, UtilActivity.class);
//                    }
//                    if (data[3] == 0x10) {
//                    	Util.notification(2, context, UtilActivity.class);
//                    }
//                    if (data[4] == 0x10) {
//                    	Util.notification(3, context, UtilActivity.class);
//                    }
//                    if (data[5] == 0x10) {
//                    	Util.notification(4, context, UtilActivity.class);
//                    }
                }
                break;
        }
    }  
    

    private void fetalMovement(byte[] stripData) {
        switch (stripData[0]) {
            case (byte) 0x91:
                if (stripData.length == 3 && isOpenFetal == false) {
                    int temp1 = stripData[2];
                    int temp2 = stripData[1];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    sum = (temp1 << 8) + temp2;
                    Trace.e(TAG, "fetalMovement sum = " + sum);
                    Trace.e(TAG, "DataCnt = " + DataCnt);
                    isOpenFetal = true;
                    DataCnt1 = 0;
                }
                break;
            case (byte) 0x92:
                if (stripData.length == 7 && isOpenFetal == true) {
                    int temp1 = stripData[6];
                    int temp2 = stripData[5];
                    int temp3 = stripData[4];
                    int temp4 = stripData[3];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    temp3 &= 0xFF;
                    temp4 &= 0xFF;
                    int dateTime = (temp1 << 24) + (temp2 << 16) + (temp3 << 8) + temp4;
                    dateTime &= 0xFFFFFFFF;
                    Date date1 = new Date();
                    FetalMovement movement = new FetalMovement();
                    date1.setTime(dateTime * 1000L);
                    movement.setDateTime(dateTime);
                    movement.setDateFlag(Util.DateToString(date1));
                    movement.setTimeFlag(Util.DateToString(date1, Constant.FORMAT_H_M));
                    movement.setType(0);
                    movement.setAccount(account);
                    movement.setAddress(address);
                    Trace.e(TAG, movement.toString());
                    try {
                    	bleService.dbHelper.saveFetalMovement(movement); 
                        DataCnt1++;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }                      
                    updateProgress(DataCnt1, true);  
                }
                break;
            case (byte) 0x93:
            	if(stripData.length == 3 && isOpenFetal == true){
            		 Trace.e(TAG, "DataCnt1 = " + DataCnt1);
            		if(DataCnt1 > 0){
                    	DataCnt += DataCnt1;
						Date tmp = new Date();
						bleService.setLong(Constant.UPDATE_FETALMOVEMENT, tmp.getTime());    
                        Trace.e(TAG, "DataCnt = " + DataCnt);
            		}
            		if(DataCnt == sumAll){
            			noticeSynchronizationResult(true);
            		}else{
            			noticeSynchronizationResult(false);
            		}
                    sum = 0;
                    DataCnt = 0;
                    sumAll = 0;
                    bleService.setUpdatePercent(-1);                  
                    isOpenFetal = false;
            	}
                break;
            case (byte) 0x94:
                if (stripData.length == 5) {
                    int temp1 = stripData[4];
                    int temp2 = stripData[3];
                    int temp3 = stripData[2];
                    int temp4 = stripData[1];
                    temp1 &= 0xFF;
                    temp2 &= 0xFF;
                    temp3 &= 0xFF;
                    temp4 &= 0xFF;
                    int dateTime = (temp1 << 24) + (temp2 << 16) + (temp3 << 8) + temp4;
                    dateTime &= 0xFFFFFFFF;
                    Date date1 = new Date();
                    FetalMovement movement = new FetalMovement();
                    date1.setTime(dateTime * 1000L);
                    movement.setDateTime(dateTime);
                    movement.setDateFlag(Util.DateToString(date1));
                    movement.setTimeFlag(Util.DateToString(date1, Constant.FORMAT_H_M));
                    movement.setAccount(account);
                    movement.setType(0);
                    movement.setAddress(address);
                    try {
                    	bleService.dbHelper.insertFetalMovement(movement);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try {
						bleService.getActivityCallback().RealMessage(MessageUtil.RealTimeFetalMovement);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                break;
        }
    }	    
    
    
    private void deviceInformation(byte[] data) {
        try {
            Device device = null;
            if (!StringUtils.isEmpty(account)) {
                device = bleService.dbHelper.getDevice(account, address);
            }
            if (null != device)
                Trace.e(TAG, device.toString());
            switch (data[0]) {
	            case TOAPP_ID5:{
	            	int len = 0;
	            	for(int i = 1; i < data.length; i++){
	            		if(data[i] != 0){
	            			len++;
	            		}else{
	            			break;
	            		}
	            	}
	            	if(len != 0){
	            		byte[] firmwareVersions = new byte[len];
		            	for(int i = 0; i < len; i++){
		            		firmwareVersions[i] = data[i + 1];
		            	}
	                    String firmwareV = null;
	                    try {
	                        firmwareV = new String(firmwareVersions, CHARSET_UTF_8);
	                        Trace.e(TAG, "固件版本：" + firmwareV);
	                    } catch (UnsupportedEncodingException e) {
	                        Trace.e(TAG, "获取版本信息错误", e);
	                        e.printStackTrace();
	                        break;
	                    }
	                    if (null != device) {
	                        device.setFirmwareVersion(firmwareV);
	                        bleService.dbHelper.updateDevice(device);
	                    } else {
	                        device = new Device();
	                        device.setFirmwareVersion(firmwareV);
	                        device.setAddress(address);
	                        device.setAccount(account);
	                        bleService.dbHelper.createDevice(device);
	                    }
	                    try {
							bleService.getActivityCallback().RealMessage(MessageUtil.FIRMWAREVERSIONS_BACK);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	                    
	            	}
	            }break;
            
                case TOAPP_ID1:
                    byte[] firmwareVersions = new byte[]{data[1], data[2],
                            data[3]};
                    byte[] hardwareVersions = new byte[]{data[4], data[5],
                            data[6]};
                    String firmwareV = null;
                    String hardwareV = null;
                    try {
                        firmwareV = new String(firmwareVersions, CHARSET_UTF_8);
                        hardwareV = new String(hardwareVersions, CHARSET_UTF_8);
                        Trace.e(TAG, "固件版本：" + firmwareV + " \n硬件版本：" + hardwareV);
                    } catch (UnsupportedEncodingException e) {
                        Trace.e(TAG, "获取版本信息错误", e);
                        e.printStackTrace();
                        break;
                    }
                    if (null != device) {
                        device.setFirmwareVersion(firmwareV);
                        device.setHardwareVersion(hardwareV);
                        bleService.dbHelper.updateDevice(device);
                    } else {
                        device = new Device();
                        device.setFirmwareVersion(firmwareV);
                        device.setHardwareVersion(hardwareV);
                        device.setAddress(address);
                        device.setAccount(account);
                        bleService.dbHelper.createDevice(device);
                    }
                    try {
						bleService.getActivityCallback().RealMessage(MessageUtil.FIRMWAREVERSIONS_BACK);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    break;
                case TOAPP_ID2:
                    byte[] bytes = new byte[data.length - 1];
                    System.arraycopy(data, 1, bytes, 0, bytes.length);
                    String manufacturer = null;
                    try {
                        manufacturer = new String(bytes, CHARSET_UTF_8);
                        Trace.e(TAG, "生产厂家：" + manufacturer);
                    } catch (UnsupportedEncodingException e) {
                        Trace.e(TAG, "获取生产厂家信息错误", e);
                        e.printStackTrace();
                        break;
                    }
                    if (null != device) {
                        device.setManufacturer(manufacturer);
                        bleService.dbHelper.updateDevice(device);
                    } else {
                        device = new Device();
                        device.setManufacturer(manufacturer);
                        device.setAccount(account);
                        device.setAddress(address);
                        bleService.dbHelper.createDevice(device);
                    }
                    break;
                case TOAPP_ID3:
                    byte[] bytes1 = new byte[data.length - 1];
                    System.arraycopy(data, 1, bytes1, 0, bytes1.length);
                    String serialNumber = null;
                    try {
                        serialNumber = new String(bytes1, CHARSET_UTF_8);
                        Trace.e(TAG, "序 列 号：" + serialNumber);
                    } catch (UnsupportedEncodingException e) {
                        Trace.e(TAG, "获取序 列 号信息错误", e);
                        e.printStackTrace();
                        break;
                    }
                    if (device != null) {
                        device.setSerialNumber(serialNumber);
                        bleService.dbHelper.updateDevice(device);
                    } else {
                        device = new Device();
                        device.setSerialNumber(serialNumber);
                        device.setAccount(account);
                        device.setAddress(address);
                        bleService.dbHelper.createDevice(device);
                    }
                    break;
                case TOAPP_ID4:
                    switch (data.length) {
                        case 5:
                            int a = data[1];
                            int b = data[2];
                            int versions = 0xFFFF & (a | (b << 8));
                            int ver = data[3];
                            int transducer = data[4];
                            transducer &= 0x03;
                            Trace.e(TAG, "设备当前版本号为：" + versions);
                            Trace.e(TAG, "设备型号：" + ver);
                            Trace.e(TAG, "设备传感器：" + (transducer == 2 ? "st" : "362"));
                            if (device != null) {
                                device.setVersionValue(versions);
                                device.setTransducerType(transducer);
                                device.setVersion(ver);
                                bleService.dbHelper.updateDevice(device);
                            } else {
                                device = new Device();
                                device.setVersionValue(versions);
                                device.setVersion(ver);
                                device.setTransducerType(transducer);
                                device.setAccount(account);
                                device.setAddress(address);
                                bleService.dbHelper.createDevice(device);
                            }
                            break;
                        case 4:
                            int a4 = data[1];
                            int b4 = data[2];
                            int versions4 = 0xFFFF & (a4 | (b4 << 8));
                            Trace.e(TAG, "设备当前版本号为：" + versions4);
                            int ver4 = data[3];
                            Trace.e(TAG, "设备当前型号为：" + ver4);
                            if (device != null) {
                                device.setVersionValue(versions4);
                                device.setVersion(ver4);
                                bleService.dbHelper.updateDevice(device);
                            } else {
                                device = new Device();
                                device.setVersionValue(versions4);
                                device.setVersion(ver4);
                                device.setAccount(account);
                                device.setAddress(address);
                                bleService.dbHelper.createDevice(device);
                            }
                            break;
                        case 3:
                            int a1 = data[1];
                            int b1 = data[2];
                            int versions1 = 0xFFFF & (a1 | (b1 << 8));
                            Trace.e(TAG, "设备当前版本号为：" + versions1);
                            Trace.e(TAG, "当前设备无法获取型号：");
                            if (device != null) {
                                device.setVersionValue(versions1);
                                device.setVersion(-1);
                                bleService.dbHelper.updateDevice(device);
                            } else {
                                device = new Device();
                                device.setVersionValue(versions1);
                                device.setVersion(-1);
                                device.setAccount(account);
                                device.setAddress(address);
                                bleService.dbHelper.createDevice(device);
                            }

                            break;
                    }
                    try {
						bleService.getActivityCallback().RealMessage(MessageUtil.FIRMWAREVERSIONS_BACK);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                    
                    break;
            }
        } catch (Exception e) {
            Trace.e(TAG, "操作数据库失败", e);
        }    
    }
    
    private void batteryCapacity(byte[] data) {
        if (data[0] == TOAPP_ID1 && data.length == 3) {
            try {
                BatteryInfo batteryInfo = new BatteryInfo();
                if(!StringUtils.isEmpty(address)){
	                switch (data[1]) {
	                    case 0:
	                       // Trace.e(TAG, "未充电");
	                        batteryInfo.setState(0);
	                        break;
	                    case 1:
	                       // Trace.e(TAG, "充电");
	                        batteryInfo.setState(1);
	                        bleService.setLong(Constant.CHARGING_DATE, new Date().getTime());
	                        break;
	                    case 2:
	                       // Trace.e(TAG, "充电充满");
	                        batteryInfo.setState(2);
	                        break;
	                }
                    int soc = data[2];
                    soc &= 0xFF;  
                    batteryInfo.setCapacity(soc);
                    Trace.e(TAG, "电池电量:" + soc + "%");
                }else{
                	batteryInfo.setState(-1);
                	batteryInfo.setCapacity(0);
                	Trace.e(TAG, "电池电量:未绑定设备为 0");
                }
                try {
                	bleService.setRealBatteryInfo(batteryInfo);
                	bleService.getActivityCallback().RealBatteryInfo(batteryInfo);
        		} catch (RemoteException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}                 
            } catch (Exception e) {
                Trace.e(TAG, "________", e);
            }
        }
    } 
}
