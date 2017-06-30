package com.liwy.hookdemo;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.liwy.hookdemo.onclick.LiwyClickListener;
import com.liwy.hookdemo.onclick.OnClickListenerProxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liwy on 2017/6/30.
 */

public class HookManager {
    public static void hookActivity(Activity activity, LiwyClickListener liwyClickListener){
        List<View> views = getAllChildViews(activity);
        hookViews(views,liwyClickListener);
    }

    public static void hookView(View view,LiwyClickListener liwyClickListener){
        try {
            //通过反射获取ListenerInfo对象
            Class mClassView = Class.forName("android.view.View");
            Method method = mClassView.getDeclaredMethod("getListenerInfo");
            method.setAccessible(true);
            Object listenerInfoObj = method.invoke(view);

            //通过反射获取onClickListener成员变量
            Class listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
            Field onClickListenerField = listenerInfoClass.getDeclaredField("mOnClickListener");
            onClickListenerField.setAccessible(true);
            View.OnClickListener onClickListener = (View.OnClickListener) onClickListenerField.get(listenerInfoObj);

            // 通过反射重新设置onClickListener,
            if (onClickListener != null){
                OnClickListenerProxy onClickListenerProxy = new OnClickListenerProxy(onClickListener,liwyClickListener);
                onClickListenerField.set(listenerInfoObj,onClickListenerProxy);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    public static void hookViews(List<View> views,LiwyClickListener liwyClickListener){
        for (View view : views){
            hookView(view,liwyClickListener);
        }
    }

    /**
     * 遍历需要监听Listenerd的Activity
     * @param activity
     * @return
     */
    private static List<View> getAllChildViews(Activity activity) {
        View view = activity.getWindow().getDecorView();
        return getAllChildViews(view);
    }

    /**
     * 获取view的所有子view
     * @param view
     * @return
     */
    private static List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<View>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                allchildren.add(viewchild);
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }
}
