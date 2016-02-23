package com.spark.util;

import java.io.File;
import java.io.IOException;

/**
 * Created by WENFUMAN on 2014/12/31.
 */
interface HttpReq {
    public static final String UPLOAD_URL= Constant.SERVICER+"file/upload?type=TYPE&length=LENGTH&index=INDEX";
    public String upload(String type, File file, long index, long endIndex) throws IOException;
}
