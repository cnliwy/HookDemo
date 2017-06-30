简介
===
## 1 功能说明
可以监控Activity下所有view的点击事件或者具体某个View的点击事件。
onAttachedToWindow()方法里注册HookManager事件，通过自定义LiwyOnClickListener即可实现OnClickListener的自定义执行监听。
默认已实现OnClickListener的执行时间监控，可输出onClick事件执行耗时值。
### 2 使用
```
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        HookManager.hookActivity(this, new LiwyClickListener() {
            @Override
            public void onClickBefore(View view) {
                System.out.println("---->方法执行前");
            }

            @Override
            public void onClickAfter(View view) {
                System.out.println("---->方法执行后");
            }
        });
    }
```

原理
===

### 1 思路
主要的原理就是通过反射获取View的OnClickListener事件，然后通过代理的方式重新设置该View的OnClickListener事件。
只不过View的事件统一在ListenerInfo类里存储，所以需要先获取view的ListenerInfo的成员变量，通过getListenerInfo。
而OnClickListener就在ListenerInfo里了，通过反射获取并重新赋值即可。

### 2 实现
```
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
```