package com.spark.widget;

import com.spark.percent.PercentRelativeLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2015/3/16.
 */
@SuppressLint("ClickableViewAccessibility")
public class CustomScrollView extends ScrollView {
    private int color,height;
    private PercentRelativeLayout top;
    private GestureDetector mGestureDetector;
    
    private void init(Context context){
        setWillNotDraw(false);
        this.setHorizontalFadingEdgeEnabled(false);
        this.setOverScrollMode(View.OVER_SCROLL_NEVER);
        setFadingEdgeLength(0);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());    	
    }
    
    public CustomScrollView(Context context) {
        this(context, null);
        init(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.scrollViewStyle);
        init(context);       
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setHeight(int height) {
        this.height=height;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setTop(PercentRelativeLayout top) {
        this.top = top;
        this.top.setBackgroundColor(color);
        this.top.getBackground().setAlpha(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (null != top) {
            if (ev.getAction() == 1) {
                detectScrollY();
            } else {
                if (height <= getScrollY()) {
                    top.setBackgroundColor(color);
                    top.getBackground().setAlpha(255);
                } else if (1 > getScrollY()) {
                    top.getBackground().setAlpha(0);
                } else {
                    top.setBackgroundColor(Color.WHITE);
                    top.getBackground().setAlpha(127);
                }
            }
        }
        return super.onTouchEvent(ev);
    }

    public void detectScrollY() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (height <= getScrollY()) {
                    top.setBackgroundColor(color);
                    top.getBackground().setAlpha(255);
                } else if (1 > getScrollY()) {
                    top.getBackground().setAlpha(0);
                } else {
                    top.setBackgroundColor(Color.WHITE);
                    top.getBackground().setAlpha(127);
                }
            }
        }, 200);
    }

    // Return false if we're scrolling in the x direction  
    class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {             
            return Math.abs(distanceY) > Math.abs(distanceX);
        }
    }
}
