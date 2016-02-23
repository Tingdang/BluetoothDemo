package com.spark.widget;

import com.spark.service.ReConnectService;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class CusFntTextView extends TextView {

	public CusFntTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTypeface(ReConnectService.getTypeface());
	}

	public CusFntTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(ReConnectService.getTypeface());
	}

	public CusFntTextView(Context context) {
		super(context);
		setTypeface(ReConnectService.getTypeface());
	}
}
