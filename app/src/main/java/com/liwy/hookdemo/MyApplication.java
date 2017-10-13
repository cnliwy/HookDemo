package com.liwy.hookdemo;

import android.app.Application;

import com.liwy.hookdemo.startactivity.StartUtils;

/**
 * Created by liwy on 2017/10/13.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StartUtils startUtils = new StartUtils(this,ProxyActivity.class);
        try {
            startUtils.hookAMS();
            startUtils.hook2();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
