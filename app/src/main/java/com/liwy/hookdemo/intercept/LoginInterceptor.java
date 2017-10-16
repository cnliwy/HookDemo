package com.liwy.hookdemo.intercept;

import com.liwy.hookdemo.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by liwy on 2017/10/16.
 */

public class LoginInterceptor {
    public void interceptLogin() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
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
        LoginInterceptHandler handler = new LoginInterceptHandler(iActivityManager);
        // 钩子对象
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{iActivityIntercept},handler);

        instanceField.set(defaultValue,proxy);
    }

    public class LoginInterceptHandler implements InvocationHandler{
        private Object iActivityManagerObj;

        public LoginInterceptHandler(Object iActivityManagerObj) {
            this.iActivityManagerObj = iActivityManagerObj;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".contains(method.getName())){
                if (!Constants.isLogined){
                    Constants.isLogined = true;
                    System.out.println("---------------->未登录");
                    return null;
                }else{
                    System.out.println("---------------->已登录，进入下个页面");
                }
            }
            return method.invoke(iActivityManagerObj,args);
        }
    }


}
