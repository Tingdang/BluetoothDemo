package com.spark.util;

import com.loopj.android.http.*;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

public class AsyncHttpUtil {

    private static final String TAG = "AsyncHttpUtil";
    private static AsyncHttpClient client = new AsyncHttpClient();

    static {
        client.setTimeout(10000); // 设置链接超时，如果不设置，默认为10s
    }
  
    
    public static RequestHandle post(String urlString, String body,
                            AsyncHttpResponseHandler res) throws UnsupportedEncodingException // url里面带参数
    {
        HttpEntity entity = new StringEntity(body, "UTF-8");
        Trace.e(TAG, "post：" + urlString + "?" + body);
        RequestHandle rqhandle = client.post(null, urlString, entity, null, res);
        return rqhandle;
    }

}
