package com.spark.util;

import org.apache.commons.lang3.StringUtils;

import com.spark.MessageUtil;

import android.os.CountDownTimer;
import android.os.Message;
import android.widget.TextView;

public class CountDownUtils extends CountDownTimer {
	private TextView tv_display;

	public CountDownUtils(long millisInFuture, long countDownInterval,
			TextView _tv_display) {
		super(millisInFuture, countDownInterval);
		// TODO Auto-generated constructor stub
		tv_display = _tv_display;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
        Message message = new Message();
        message.what = MessageUtil.CLOSE_FETALMOVEMENT;
		MessageUtil.sendMessage(message);						
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		int minutes = (int) ((millisUntilFinished / 1000)/60);
		int seconds = (int) ((millisUntilFinished / 1000)%60);
		String text2="", text1="", text0 = "(";
		
		if(minutes > 0){
			text1 = minutes+"分钟";
		}
		if(seconds > 0){
			text2 = seconds+"秒";
		}
		
		if(!StringUtils.isEmpty(text2) || !StringUtils.isEmpty(text1)){
			text0 += text1;
			text0 += text2;
			text0 += "以后失效)";
			tv_display.setText(text0);
		}
	}
}
