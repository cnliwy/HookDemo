package com.liwy.hookdemo;

import android.app.Application;

import com.liwy.hookdemo.intercept.LoginInterceptor;

/**
 * Created by liwy on 2017/10/13.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LoginInterceptor loginInterceptor = new LoginInterceptor();
        try {
            loginInterceptor.interceptLogin();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
