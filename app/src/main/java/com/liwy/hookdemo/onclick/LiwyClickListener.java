package com.liwy.hookdemo.onclick;

import android.view.View;

/**
 * Created by liwy on 2017/6/30.
 */

public interface LiwyClickListener{
    public void onClickBefore(View view);
    public void onClickAfter(View view);
}
