package com.liwy.hookdemo.startactivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by liwy on 2017/10/12.
 */

public class StartUtils {
    private Context mContext;
    private Class<?> proxyActivity;

    public StartUtils(Context mContext, Class<?> proxyActivity) {
        this.mContext = mContext;
        this.proxyActivity = proxyActivity;
    }

    // hook ActivityManagerServices
    public void hookAMS() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // 反射得到IActivityManager
        Class<?> forName = Class.forName("android.app.ActivityManagerNative");
        Field defaultField = forName.getDeclaredField("gDefault");
        defaultField.setAccessible(true);
        Object defaultValue = defaultField.get(null);

        Class<?> forName2 = Class.forName("android.util.Singleton");
        Field instanceField = forName2.getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        Object iActivityManager = instanceField.get(defaultValue);
        // 通过动态代理实现控制
        Class<?> iActivityIntercept = Class.forName("android.app.IActivityManager");
        AMSInvocationHandler handler = new AMSInvocationHandler(iActivityManager);
        // 钩子对象
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{iActivityIntercept},handler);

        instanceField.set(defaultValue,proxy);
    }

    // hook SystemHandler
    public void hookSystemHandler(){
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field currentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            currentActivityThreadField.setAccessible(true);
            Object currentActivityThread = currentActivityThreadField.get(null);

            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);

            Handler handlerObj = (Handler)mHField.get(currentActivityThread);
            Field callbackField = Handler.class.getDeclaredField("mCallback");
            callbackField.setAccessible(true);

            ActivityThreadCallBack activityThreadCallBack = new ActivityThreadCallBack(handlerObj);
            callbackField.set(handlerObj,activityThreadCallBack);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void hook2()throws Exception{
        /**
         * 欺骗ActivityThread
         */

        // 先获取到当前的ActivityThread对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Field currentActivityThreadField = activityThreadClass.getDeclaredField
                ("sCurrentActivityThread");
        currentActivityThreadField.setAccessible(true);
        Object currentActivityThread = currentActivityThreadField.get(null);

        // 由于ActivityThread一个进程只有一个,我们获取这个对象的mH
        Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        final Handler mH = (Handler) mHField.get(currentActivityThread);

        Field mCallBackField = Handler.class.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);

        mCallBackField.set(mH, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 100) {
                    Object obj = msg.obj;
                    try {
                        // 把替身恢复成真身
                        Field intent = obj.getClass().getDeclaredField("intent");
                        intent.setAccessible(true);
                        Intent raw = (Intent) intent.get(obj);

                        Intent target = raw.getParcelableExtra("realIntent");
                        if (target != null){
                            raw.setComponent(target.getComponent());
                        }
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    mH.handleMessage(msg);
                }
                return true;
            }
        });

    }
    // 重写Handler
    public class ActivityThreadCallBack implements Handler.Callback{
        private Handler mHandler;

        public ActivityThreadCallBack(Handler mHandler) {
            this.mHandler = mHandler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            System.out.println("msg.what-------------------->" + msg.what);
            if (msg.what == 100){
                handlerLaunchActivity(mHandler,msg);
            }
            return true;
        }
        private void handlerLaunchActivity(Handler handler,Message msg){
            Object obj = msg.obj;
            try {
                Field field =  obj.getClass().getDeclaredField("intent");
                field.setAccessible(true);
                Intent currentIntent = (Intent)field.get(obj);
                // 将隐藏在代理intent里的真正intent取出来作为现在的intent
                Intent realIntent = currentIntent.getParcelableExtra("realIntent");
                if (realIntent != null){
                    currentIntent.setComponent(realIntent.getComponent());
                }
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public class AMSInvocationHandler implements InvocationHandler{
        private Object iActivityManagerObj;

        public AMSInvocationHandler(Object iActivityManagerObj) {
            this.iActivityManagerObj = iActivityManagerObj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".contains(method.getName())){
                Intent intent = null;
                int index = 0;
                for (int i  = 0; i < args.length; i++){
                    if (args[i] instanceof Intent){
                        intent = (Intent)args[i];
                        index = i;
                        break;
                    }
                }
                Intent proxyIntent = new Intent();
                ComponentName componentName = new ComponentName(mContext,proxyActivity);
                proxyIntent.setComponent(componentName);
                proxyIntent.putExtra("realIntent",intent);
                args[index] = proxyIntent;
                return method.invoke(iActivityManagerObj,args);
            }
            return method.invoke(iActivityManagerObj,args);
        }
    }
}
