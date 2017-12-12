package com.example.administrator.lockapplication;

/**
 * Created by Administrator on 2017/11/29.
 */

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

/**
 * 监听屏幕ON和OFF PRESENT状态
 *
 * @author
 * @2014
 *
 */
public class ScreenObserver {
    private static String TAG = "ScreenObserver";
    private Context mContext;
    private ScreenBroadcastReceiver mScreenReceiver;
    private ScreenStateListener mScreenStateListener;
    private static Method mReflectScreenState;


    public ScreenObserver(Context context) {
        mContext = context;

        mScreenReceiver = new ScreenBroadcastReceiver();
        try {
            mReflectScreenState = PowerManager.class.getMethod("isScreenOn",
                    new Class[] {});
        } catch (Exception nsme) {
            Log.d(TAG, "API < 7," + nsme);
        }
    }

    /**
     * screen状态广播接收者
     */
    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("onReceive", intent.getAction());
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                mScreenStateListener.onScreenOn();
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                mScreenStateListener.onScreenOff();
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                mScreenStateListener.onUserPresent();
            }
        }
    }

    /**
     * 请求screen状态更新
     */
    public void requestScreenStateUpdate(ScreenStateListener listener) {
        mScreenStateListener = listener;
        startScreenBroadcastReceiver();
        firstGetScreenState();
    }

    /**
     * 第一次请求screen状态
     */
    private void firstGetScreenState() {
        PowerManager manager = (PowerManager) mContext
                .getSystemService(Activity.POWER_SERVICE);
        if (isScreenOn(manager)) {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOn();
            }
        } else {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOff();
            }
        }
    }

    /**
     * 停止screen状态更新
     */
    public void stopScreenStateUpdate() {
        mContext.unregisterReceiver(mScreenReceiver);
    }

    /**
     * 启动screen状态广播接收器
     */
    private void startScreenBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenReceiver, filter);
    }

    /**
     * screen是否打开状态
     */
    private static boolean isScreenOn(PowerManager pm) {
        boolean screenState;
        try {
            screenState = (Boolean) mReflectScreenState.invoke(pm);
        } catch (Exception e) {
            screenState = false;
        }
        return screenState;
    }

    // 外部调用接口
    public interface ScreenStateListener {
        public void onScreenOn();

        public void onScreenOff();

        public void onUserPresent();
    }

    public final static boolean isScreenLocked(Context c) {
        android.app.KeyguardManager mKeyguardManager = (KeyguardManager) c
                .getSystemService(c.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.e("handleMessage", "Thread----------");
            KeyguardManager km= (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
//    KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
            //解锁
//    kl.disableKeyguard();
            //获取电源管理器对象
            PowerManager pm=(PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
            wl.acquire();

        }
    };

    public void wakeUpAndUnlock(){

        KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
//    KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
//    kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm=(PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");

        //点亮屏幕
        // wl.acquire();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    SystemClock.sleep(10000);
//                    Log.e("Thread","11111");
//                    //点亮屏幕
//                    wl.acquire();
                    Message msg = new Message();
                    msg.what = 111;
                    mHandler.sendMessageDelayed(msg, 100);
                }
            }
        }).start();

        //释放
        //wl.release();
    }


//    public void wakeUp(){
//        wl.acquire();
//    }

}
