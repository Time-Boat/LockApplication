package com.example.administrator.lockapplication.floatball;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallService extends AccessibilityService {
    public static final int TYPE_ADD = 0;
    public static final int TYPE_DEL = 1;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");

        //点亮屏幕
        wl.acquire();

        Bundle data = intent.getExtras();
        if (data != null) {
            int type = data.getInt("type");
            if (type == TYPE_ADD) {
                FloatWindowManager.addBallView(this);
            } else {
                FloatWindowManager.removeBallView(this);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
