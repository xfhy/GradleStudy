package com.xfhy.gradledemo;

import android.content.Context;
import android.text.TextUtils;

import transform.methodtime.MethodTrack;

/**
 * @author : xfhy
 * Create time : 2020/8/13 10:41 PM
 * Description :
 */
@MethodTrack
public class Test {

    /*public void delete() {
        int result = 0;
        for (int i = 0; i < 100000; i++) {
            result -= i;
        }
        System.out.println(result);
    }


    public void add() {
        int result = 0;
        for (int i = 0; i < 100000; i++) {
            result += i;
        }
        System.out.println(result);
    }*/

    /*public void delete2() {
        long currentTimeMillis = System.currentTimeMillis();

        System.out.println("xfhy");

        long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
        Log.d("Test", "⇢ " + "delete()V: " + currentTimeMillis2 + "ms");
    }*/

    public void hello(String name) {
        /*if (FastClickUtil.isFastDoubleClick()) {
            return;
        }*/
        if(TextUtils.isEmpty(name)){
            return;
        }
        System.out.println("hello");
    }

}
