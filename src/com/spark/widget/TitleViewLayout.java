package com.spark.widget;


import com.spark.bluetoothdemo.R;
import com.spark.percent.PercentFrameLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

@SuppressLint("ClickableViewAccessibility")
public class TitleViewLayout extends PercentFrameLayout {
    private ImageView leftBtn;
    private CusFntTextView title,rightBtn;
    private Drawable leftBtnRes, rightBtnRes;
    private String titleRes;
    private ImageView iv_onScan;
    private Animation operatingAnim;
    
    public TitleViewLayout(Context context) {
        this(context, null);
    }

    public TitleViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.title_view_layout, this);
        leftBtn = (ImageView) view.findViewById(R.id.leftBtn);
        rightBtn = (CusFntTextView) view.findViewById(R.id.rightBtn);
        title = (CusFntTextView) view.findViewById(R.id.title);
        iv_onScan = (ImageView) view.findViewById(R.id.iv_onScan);
        operatingAnim = AnimationUtils.loadAnimation(context, R.anim.tryconnect);  
        LinearInterpolator lin = new LinearInterpolator();  
        operatingAnim.setInterpolator(lin);         
        setAttrs(context, attrs);
        if(null == leftBtnRes){
        	leftBtn.setBackground(leftBtnRes);
        }
        if(null == rightBtnRes){
        	rightBtn.setBackground(rightBtnRes);
        }
        title.setText(titleRes); 
        TextPaint tp = rightBtn.getPaint(); 
        tp.setFakeBoldText(true);  
    }
    
    /**
     * 获取自定义View的属性值
     *
     * @param context
     * @param attrs
     */
    private void setAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleView);
        if (null != a) {
        	leftBtnRes = a.getDrawable(R.styleable.TitleView_leftdrawable);
        	rightBtnRes = a.getDrawable(R.styleable.TitleView_rightdrawable);
        	titleRes = a.getString(R.styleable.TitleView_titlecontent);
            a.recycle();
        }
    }    
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
	
    public void setClickListener(OnClickListener l, OnClickListener r) {
    	if(null != l){
        	leftBtn.setOnClickListener(l);
        	leftBtn.setVisibility(View.VISIBLE);
        }
        if(null != r){
        	rightBtn.setOnClickListener(r);
        	rightBtn.setVisibility(View.VISIBLE);
        }
    }
    
    public void setClickListener(OnClickListener l) {
    	if(null != l){
        	leftBtn.setOnClickListener(l);
        	leftBtn.setVisibility(View.VISIBLE);
        }       
    } 
    
    public void startAnimation(){
    	if (operatingAnim != null) {  
    		iv_onScan.startAnimation(operatingAnim); 
    		iv_onScan.setVisibility(View.VISIBLE);
    	}     	
    }
    
    public void stopAnimation(){
    	iv_onScan.clearAnimation();  
    	iv_onScan.setVisibility(View.INVISIBLE); 
    }  
    
    public void setTitle(String titleRes){
    	title.setText(titleRes);
    }

    public void setTitleSize(float size){
    	title.setTextSize(size);
    }    
    
    public void setTitle(int titleRes){
    	title.setText(titleRes);
    }
    
    public void setRightContent(int titleRes){
    	rightBtn.setText(titleRes);
    }    
}
