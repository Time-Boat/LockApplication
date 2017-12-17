package com.example.administrator.lockapplication.floatball;

import android.accessibilityservice.AccessibilityService;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.administrator.lockapplication.EventData;
import com.example.administrator.lockapplication.MyBroadcast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Date;

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
        EventBus.getDefault().register(this);

        //锁屏权限
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

    }

    public final static String CLOSE_TAG = "息屏";
    public final static String OPEN_TAG = "亮屏";

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

    //亮屏时间
    private int openHour = 0;
    private int openMinute = 0;

    //息屏时间
    private int closeHour = 0;
    private int closeMinute = 0;

    boolean isFirst = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData event) {
        closeHour = event.closeHour;
        closeMinute = event.closeMinute;
        openHour = event.openHour;
        openMinute = event.openMinute;
        isFirst = event.isFirst;
        Log.e("FloatBallService", "EventBus事件调用     name:" + event.name);
        Log.e("FloatBallService", "EventBus事件调用      openHour:" + openHour + "   openMinute:" + openMinute);
        Log.e("FloatBallService", "EventBus事件调用      closeHour:" + closeHour + "   closeMinute:" + closeMinute);
        if (OPEN_TAG.equals(event.name)) {
            Message msg = new Message();
            msg.what = WAKE_UNLOCK;
            light();
            startOverAlarm();
        } else if (CLOSE_TAG.equals(event.name)) {
            startOpenAlarm();
            if(!isFirst){
                devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                devicePolicyManager.lockNow();
            }
        }
    }

    private void light() {
        Log.e("FloatBallService", " alarm    begin----------");

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // 点亮亮屏
        wakeLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.SCREEN_DIM_WAKE_LOCK |
                PowerManager.ON_AFTER_RELEASE, "bright");

        wakeLock.acquire();
        //得到键盘锁管理器对象
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("unLock");

        // 键盘解锁
        keyguardLock.disableKeyguard();
        //锁屏
        //keyguardLock.reenableKeyguard();

        //释放wakeLock，关灯      释放屏幕常亮锁
        wakeLock.release();
    }

    //启动亮屏定时
    public void startOverAlarm() {
        //获取系统闹钟服务
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Log.e("MainActivity", "startOverAlarm       启动息屏闹钟 ");
        Log.e("MainActivity", "startOverAlarm        closeHour:" + closeHour + "   closeMinute:" + closeMinute);
        //设置闹钟时间
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, closeHour);//小时
        instance.set(Calendar.MINUTE, closeMinute);//分钟
        instance.set(Calendar.SECOND, 0);//秒
        Log.e("MainActivity", "getActualMaximum:" + instance.get(Calendar.HOUR_OF_DAY));
        long mill = instance.getTimeInMillis();

        Intent intent = new Intent(this, MyBroadcast.class);
        intent.putExtra("tag", CLOSE_TAG);

        //测试
        intent.putExtra("openHour", openHour);
        intent.putExtra("openMinute", openMinute);
        intent.putExtra("closeHour", closeHour);
        intent.putExtra("closeMinute", closeMinute);

        PendingIntent op = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            Log.e("startOpenAlarm", "1");                   //ELAPSED_REALTIME_WAKEUP  相对系统时间
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,mill,op);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Log.e("startOpenAlarm", "2");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,mill,op);
        }else{
            Log.e("startOpenAlarm", "3");
            alarmManager.set(AlarmManager.RTC_WAKEUP,mill,op);
        }

        //一次性闹钟
        //alarmManager.set(AlarmManager.RTC_WAKEUP, mill, op);
    }

    //启动亮屏定时
    public void startOpenAlarm() {
        Log.e("MainActivity", "startOverAlarm       启动亮屏闹钟 ");
        Log.e("MainActivity", "startOpenAlarm        hourOfDay:" + openHour + "   minute:" + openMinute);

        //设置闹钟时间
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, openHour);//小时
        instance.set(Calendar.MINUTE, openMinute);//分钟
        instance.set(Calendar.SECOND, 0);//秒
        Log.e("MainActivity", "getActualMaximum:" + instance.get(Calendar.HOUR_OF_DAY));
        long mill = instance.getTimeInMillis();


        //如果不是第一次启动，就添加一天的时间
        if(!isFirst){
            mill += 1000 * 60 * 60 * 24;
//            mill += 1000 * 60 * 60 * 4;
        }
        Log.e("mill", new Date(mill).toString());

        //获取系统闹钟服务
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyBroadcast.class);
        intent.putExtra("tag", OPEN_TAG);

        //测试
        intent.putExtra("openHour", openHour);
        intent.putExtra("openMinute", openMinute);
        intent.putExtra("closeHour", closeHour);
        intent.putExtra("closeMinute", closeMinute);

        PendingIntent op = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            Log.e("startOpenAlarm", "1");                   //ELAPSED_REALTIME_WAKEUP  相对系统时间
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,mill,op);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Log.e("startOpenAlarm", "2");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,mill,op);
        }else{
            Log.e("startOpenAlarm", "3");
            alarmManager.set(AlarmManager.RTC_WAKEUP,mill,op);
        }
    }

}
