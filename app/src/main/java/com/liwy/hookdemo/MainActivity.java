package com.liwy.hookdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.liwy.hookdemo.onclick.LiwyClickListener;
import com.liwy.hookdemo.startactivity.SecondActivity;

public class MainActivity extends AppCompatActivity {
    Button testBtn;
    Button loginBtn;
    Button turnBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testBtn = (Button)findViewById(R.id.btn_test);
        loginBtn = (Button)findViewById(R.id.btn_login);
        turnBtn = (Button)findViewById(R.id.btn_turn);
        turnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnNext(v);
            }
        });

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(v.getId() + ":hello world!");
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(v.getId() + ":登陆成功!");
            }
        });
        Intent intent = new Intent(this,ProxyActivity.class);
        startActivity(intent);
    }
    public void turnNext(View view){
        System.out.println("准备进入第二页面");
        Intent intent = new Intent(this,SecondActivity.class);
        startActivity(intent);
    }

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


}
