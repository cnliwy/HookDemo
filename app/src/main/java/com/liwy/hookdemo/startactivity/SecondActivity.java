package com.liwy.hookdemo.startactivity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.liwy.hookdemo.R;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        System.out.println("----------->SecondActivity create");
    }
    public void turnNext(View view){
        Intent intent = new Intent(this,ThirdActivity.class);
        startActivity(intent);
    }
}
