package com.spark.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

public class Util {
    private static long lastClickTime;

	public static int copyAssetFileToFiles(Context context, String filename,
			String newPath) throws IOException {
		InputStream is = context.getAssets().open(filename);
		int length = is.available();
		byte[] buffer = new byte[length];
		is.read(buffer);
		is.close();

		File of = new File(newPath);
		of.createNewFile();
		FileOutputStream os = new FileOutputStream(of);
		os.write(buffer);
		os.close();
		return length;
	}    
    
	 public static int compareVersion(String version1, String version2) {  
		 int val1,val2,ret = 0,index = 0;
		 if (version1.equals(version2)) {            
			 return ret;        
		  }        
		 String[] version1Array = version1.split("\\.");        
		 String[] version2Array = version2.split("\\.");        
		 int minLen = Math.min(version1Array.length, version2Array.length); 
		 
		 for(index = 0; index < minLen; index++){
			 val1 = Integer.parseInt(version1Array[index]);
			 val2 = Integer.parseInt(version2Array[index]);
			 if(val1 != val2){
				 if(val1 > val2){
					 ret = 1;
				 }else{
					 ret = -1;
				 }
				 return ret;
			 }
		 }
		 
		 if(version1Array.length == version2Array.length){
			 return ret;
		 }else{
			 if(version1Array.length > version2Array.length){
				 for(index = minLen; index < version1Array.length; index++){
					 val1 = Integer.parseInt(version1Array[index]);
					 if(val1 != 0){
						 ret = 1;
						 return ret;
					 }
				 }
				 return ret;
			 }else{
				 for(index = minLen; index < version2Array.length; index++){
					 val2 = Integer.parseInt(version2Array[index]);
					 if(val2 != 0){
						 ret = -1;
						 return ret;
					 }
				 }
				 return ret;
			 }
		 }  
	}	
	    
    
    
	/**
	 * @Description: 时间转string
	 * @param date
	 *            时间
	 * @param format
	 *            时间格式
	 * @return String
	 * @date Dec 4, 2009 10:06:16 AM
	 */
	public static String DateToString(Date date, String format) {

		SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
		return formatter.format(date);
	}
	
    public static String DateToString(Date date) {
        String dateStr = new SimpleDateFormat(Constant.FORMAT_Y_M_D, Locale.getDefault()).format(date);
        return dateStr;
    }
    
    public static Date StringToDate(String dateStr){
    	SimpleDateFormat df = new SimpleDateFormat(Constant.FORMAT_Y_M_D, Locale.getDefault());  
        try {
			return df.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return null;
    }
    

    public static String DateToString1(Date date) {
        String dateStr = new SimpleDateFormat(Constant.FORMAT_D, Locale.getDefault()).format(date);
        return dateStr;
    }
    
	@SuppressWarnings("static-access")
	public static boolean isTopActivity(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = am.getRunningTasks(1);
		String currentTop = null;
		if (null != tasksInfo && tasksInfo.size() > 0) {
			currentTop = tasksInfo.get(0).topActivity.getPackageName();
		}
		return (null == currentTop) ? false : currentTop
				.equals(context.getPackageName());
	}
	
	/** 
	    * 判断某个界面是否在前台 
	    *  
	    * @param context 
	    * @param className 
	    *            某个界面名称 
	    */  
	 public static boolean isForeground(Context context, String className) {  
       if (context == null || TextUtils.isEmpty(className)) {  
           return false;  
       }  
  
       ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
       List<RunningTaskInfo> list = am.getRunningTasks(1);  
       if (list != null && list.size() > 0) {  
           ComponentName cpn = list.get(0).topActivity;  
           if (className.equals(cpn.getClassName())) {  
               return true;  
           }  
       }  
  
       return false;  
	}  
	
	public static boolean isFastDoubleClick() {
	      long time = System.currentTimeMillis();
	      long timeD = time - lastClickTime;
	      if (0 < timeD && timeD < 2000) {
	          return true;
	      }
	      lastClickTime = time;
	      return false;
	}	

    // 判断手机格式是否正确
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[^4,\\D])|(18[^4,\\D]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    // 判断手机格式是否正确
    public static String getKeyType(String accout) {
        if (isMobileNO(accout)) {
            return Constant.USER_TYPE_MOBILE;
        } else {
            return Constant.USER_TYPE_EMAIL;
        }
    }

    // 判断email格式是否正确
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }  
}
