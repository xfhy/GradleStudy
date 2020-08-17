package com.xfhy.gradledemo;

import androidx.appcompat.app.AppCompatActivity;
import transform.methodtime.MethodTrack;

import android.os.Bundle;
import android.util.Log;

import com.xfhy.test.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @MethodTrack
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Test test = new Test();
        Log.d(TAG, "onCreat  sse: " + test.toString());

        new Thread(new Runnable() {
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
        }).start();

        com.xfhy.gradledemo.Test test1 = new com.xfhy.gradledemo.Test();
        /*test1.add();
        test1.delete();*/

    }
}
