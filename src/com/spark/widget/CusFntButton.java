package com.spark.widget;

import com.spark.service.ReConnectService;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class CusFntButton extends Button {

	public CusFntButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTypeface(ReConnectService.getTypeface());
	}

	public CusFntButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(ReConnectService.getTypeface());
	}

	public CusFntButton(Context context) {
		super(context);
		setTypeface(ReConnectService.getTypeface());
	}
}
