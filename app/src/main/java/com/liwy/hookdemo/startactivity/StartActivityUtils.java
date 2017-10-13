package com.liwy.hookdemo.startactivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Created by liwy on 2017/10/13.
 */

public class StartActivityUtils {
    public static final String TAG = "StartActivityUtils";
    private static final String REAL_TARGET_INTENT = "real_intent";
    private Context mContext;
    private Class<?> proxyActivity;

    public StartActivityUtils(Context mContext, Class<?> proxyActivity) {
        this.mContext = mContext;
        this.proxyActivity = proxyActivity;
    }

    public void hookCode(){
        try {
            /**
             * 欺骗ActivityManagerNative,将要启动的Activity替换成我们的占坑Activity
             */
            Class<?> activityManagerNativeClass = Class.forName("android.app" +
                    ".ActivityManagerNative");

            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);

            Object gDefault = gDefaultField.get(null);

            // gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段
            Class<?> singleton = Class.forName("android.util.Singleton");
            Field mInstanceField = singleton.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            // ActivityManagerNative 的gDefault对象里面原始的 IActivityManager对象
            final Object rawIActivityManager = mInstanceField.get(gDefault);

            // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] {iActivityManagerInterface}, new InvocationHandler() {


                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws
                                Throwable {
                            if ("startActivity".equals(method.getName())) {
                                Intent raw;
                                int index = 0;

                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }
                                raw = (Intent) args[index];

                                Intent newIntent = new Intent();

                                // 这里我们把启动的Activity临时替换为 StubActivity
                                ComponentName componentName = new ComponentName(mContext,
                                        proxyActivity);
                                newIntent.setComponent(componentName);

                                // 把我们原始要启动的TargetActivity先存起来
                                newIntent.putExtra(REAL_TARGET_INTENT, raw);

                                // 替换掉Intent, 达到欺骗AMS的目的
                                args[index] = newIntent;

                                Log.d(TAG, "hook succes{s");
                                return method.invoke(rawIActivityManager, args);
                            }
                            return method.invoke(rawIActivityManager, args);
                        }
                    });
            mInstanceField.set(gDefault, proxy);


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

            // 设置它的回调, 根据源码:
            // 我们自己给他设置一个回调,就会替代之前的回调;

            //        public void dispatchMessage(Message msg) {
            //            if (msg.callback != null) {
            //                handleCallback(msg);
            //            } else {
            //                if (mCallback != null) {
            //                    if (mCallback.handleMessage(msg)) {
            //                        return;
            //                    }
            //                }
            //                handleMessage(msg);
            //            }
            //        }

            Field mCallBackField = Handler.class.getDeclaredField("mCallback");
            mCallBackField.setAccessible(true);

            mCallBackField.set(mH, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == 100) {
                        Object obj = msg.obj;
                        // 根据源码:
                        // 这个对象是 ActivityClientRecord 类型
                        // 我们修改它的intent字段为我们原来保存的即可.
                        // switch (msg.what) {
                        //      case LAUNCH_ACTIVITY: {
                        //          Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
                        //          final ActivityClientRecord r = (ActivityClientRecord) msg.obj;

                        //          r.packageInfo = getPackageInfoNoCheck(
                        //                  r.activityInfo.applicationInfo, r.compatInfo);
                        //         handleLaunchActivity(r, null);
                        try {
                            // 把替身恢复成真身
                            Field intent = obj.getClass().getDeclaredField("intent");
                            intent.setAccessible(true);
                            Intent raw = (Intent) intent.get(obj);

                            Intent target = raw.getParcelableExtra(REAL_TARGET_INTENT);
                            raw.setComponent(target.getComponent());

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


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
