package com.android.leezp.loadbigimage.Listener;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Leezp on 2017/5/4 0004.
 *
 * function:创建一个双击事件
 */

public class OnDoubleClickListener implements View.OnTouchListener{
    //点击的次数
    private int count = 0;
    //第一次点击的时间
    private long firstClick = 0;
    //第二次点击的时间
    private long secondClick = 0;


     //两次点击的时间间隔不能超过此间隔，否则不为双击事件
    private final int interval = 1000;
    //双击事件的回调方法的接口对象
    private DoubleClickCallback callback;

    public interface DoubleClickCallback {
        void onDoubleClick();
    }

    public OnDoubleClickListener(DoubleClickCallback callback) {
        this.callback = callback;
    }

    /**
     * 点击事件的调用函数
     * @param v 控件
     * @param event 事件
     * @return
     */

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            count++;
            if (count == 1) {
                firstClick = System.currentTimeMillis();
            } else if (count == 2) {
                secondClick = System.currentTimeMillis();
                if (secondClick-firstClick < interval) {
                    if (callback != null) {
                        callback.onDoubleClick();
                    } else {
                        Log.e(getClass().getSimpleName(), "没有传入双击回调接口实例");
                    }
                    count = 0;
                    firstClick = 0;
                } else {
                    firstClick = secondClick;
                    count = 1;
                }
                secondClick = 0;
            }
        }
        return true;
    }

}
