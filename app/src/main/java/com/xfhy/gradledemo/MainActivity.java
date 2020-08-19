package com.xfhy.gradledemo;

import androidx.appcompat.app.AppCompatActivity;
import transform.methodtime.MethodTrack;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.xfhy.test.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @MethodTrack
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Test test = new Test();
        //Log.d(TAG, "onCreate  sse: " + test.toString());

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://www.baidu.com").openConnection();
                    int responseCode = httpURLConnection.getResponseCode();
                    Log.d("xfhy", "responseCode = " + responseCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/

        //com.xfhy.gradledemo.Test test1 = new com.xfhy.gradledemo.Test();
        /*test1.add();
        test1.delete();*/

        findViewById(R.id.btn_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("xfhy666", "onClick: 按钮1");
            }
        });
        findViewById(R.id.btn_second).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.w("xfhy666", "onClick: 按钮2");
    }
}
