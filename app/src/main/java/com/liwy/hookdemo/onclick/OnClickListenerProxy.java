package com.liwy.hookdemo.onclick;

import android.view.View;

/**
 * 点击时间代理类
 * Created by liwy on 2017/6/30.
 */

public class OnClickListenerProxy implements View.OnClickListener {
    private View.OnClickListener mOnClickListener;
    private LiwyClickListener liwyClickListener;

    public OnClickListenerProxy(View.OnClickListener mOnClickListener,LiwyClickListener liwyClickListener) {
        this.mOnClickListener = mOnClickListener;
        this.liwyClickListener = liwyClickListener;
    }

    @Override
    public void onClick(View v) {
        System.out.println("---->id:" + v.getId() + " 开始执行点击事件");
        long start = System.currentTimeMillis();
        if (mOnClickListener != null)mOnClickListener.onClick(v);
        long end = System.currentTimeMillis();
        System.out.println("---->总共耗时：" + formatTime(end - start));
        if (liwyClickListener != null)liwyClickListener.onClick(v);
        System.out.println("---->id:" + v.getId() + " 点击事件执行结束");

    }

    /*
    * 毫秒转化
    s*/
    private static String formatTime(long ms) {

        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        String strDay = day < 10 ? "0" + day : "" + day; //天
        String strHour = hour < 10 ? "0" + hour : "" + hour;//小时
        String strMinute = minute < 10 ? "0" + minute : "" + minute;//分钟
        String strSecond = second < 10 ? "0" + second : "" + second;//秒
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;//毫秒
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;

        return strMinute + " 分钟 " + strSecond + " 秒";
    }

}
