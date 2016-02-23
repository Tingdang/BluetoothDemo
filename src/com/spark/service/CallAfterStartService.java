package com.spark.service;

import com.spark.service.aidl.IBleService;

public interface CallAfterStartService {
	public void runAfterStartService(IBleService mService);
}
