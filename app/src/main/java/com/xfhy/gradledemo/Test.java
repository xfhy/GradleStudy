package com.xfhy.gradledemo;

import transform.methodtime.MethodTrack;

/**
 * @author : xfhy
 * Create time : 2020/8/13 10:41 PM
 * Description :
 */
@MethodTrack
public class Test {

    public void delete() {
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
    }
}
