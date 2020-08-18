package com.xfhy.gradledemo;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author : xfhy
 * Create time : 2020/8/17 11:55 PM
 * Description :
 */
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("TestActivity", "-------> onCreate : " + this.getClass().getSimpleName());
        super.onCreate(savedInstanceState);
    }
}
