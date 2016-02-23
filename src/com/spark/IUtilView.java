package com.spark;

import android.view.View;

/**
 * Created by Administrator on 2015/4/3.
 */
public interface IUtilView {
    public void init();
    public void getViewId();
    public void back(View v);
    public void setViewListener();
    public void setStatus(int state);
}
