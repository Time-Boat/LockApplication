package com.example.administrator.lockapplication;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class MyBroadcast extends BroadcastReceiver {

    private final String CLOSE_TAG = "息屏";
    private final String OPEN_TAG = "亮屏";

    public static final int WAKE_UNLOCK = 0x1123;

    private DevicePolicyManager devicePolicyManager;

    // 键盘管理器
    KeyguardManager keyguardManager;
    // 键盘锁
    private KeyguardManager.KeyguardLock keyguardLock;
    // 电源管理器
    private PowerManager powerManager;
    // 唤醒锁
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onReceive(Context context, Intent intent) {
        String tag = intent.getStringExtra("tag");
        Log.e("MyBroadcast", tag);
        Toast.makeText(context,tag,Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(new EventData(tag));

        //锁屏权限
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (OPEN_TAG.equals(tag)) {
//            status = 0;
            Message msg = new Message();
            msg.what = WAKE_UNLOCK;
            alarm(context);
        } else if (CLOSE_TAG.equals(tag)) {
            devicePolicyManager.lockNow();
        }
    }

    private void alarm(Context context){
        Log.e("MyBroadcast", " alarm    begin----------");

        // 点亮亮屏
        wakeLock = powerManager.newWakeLock
                (PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.SCREEN_DIM_WAKE_LOCK |
                        PowerManager.ON_AFTER_RELEASE, "bright");

//                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//                            MyBroadcast.class.getName());//acquire lock for the service

        wakeLock.acquire();

        //得到键盘锁管理器对象
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("unLock");

        // 键盘解锁
        keyguardLock.disableKeyguard();

        //锁屏
        //keyguardLock.reenableKeyguard();

        //释放wakeLock，关灯      释放屏幕常亮锁
        wakeLock.release();
    }

}
