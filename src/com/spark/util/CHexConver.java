/*
 * Copyright (c) 2015. Guangdong Spark Technology Co.,Ltd.
 *     Author: WENFUMAN
 *     Email: hehufuman@163.com
 *     Mobile phone: +86 18033432336
 */

package com.spark.util;

import java.util.Locale;

/**
 * Created by wenhehu on 15/7/14.
 */
public class CHexConver {

    /**
     * 16进制值与String/Byte之间的转换
     * @author JerryLi
     * @email lijian@dzs.mobi
     * @data 2011-10-16
     * */
        private final static char[] mChars = "0123456789ABCDEF".toCharArray();


        /**
         * bytes转换成十六进制字符串
         * @param b byte[] byte数组
         * @param iLen int 取前N位处理 N=iLen
         * @return String 每个Byte值之间空格分隔
         */
        public static String byte2HexStr(byte[] b, int iLen){
            StringBuilder sb = new StringBuilder();
            for (int n=0; n<iLen; n++){
                sb.append(mChars[(b[n] & 0xFF) >> 4]);
                sb.append(mChars[b[n] & 0x0F]);
                if (n!=iLen-1)sb.append(':');
            }
            return sb.toString().trim().toUpperCase(Locale.US);
        }
}
