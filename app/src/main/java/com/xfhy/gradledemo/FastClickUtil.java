package com.xfhy.gradledemo;

import android.util.Log;

/**
 * @author : xfhy
 * Create time : 2020/8/19 10:53 PM
 * Description : 快速点击 工具类
 */
public class FastClickUtil {

    private static final int FAST_CLICK_TIME_DISTANCE = 300;
    private static long sLastClickTime = 0;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeDistance = time - sLastClickTime;
        if (0 < timeDistance && timeDistance < FAST_CLICK_TIME_DISTANCE) {
            return true;
        }
        sLastClickTime = time;
        return false;
    }

}
