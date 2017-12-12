package com.example.administrator.lockapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.administrator.lockapplication.floatball.LightActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

public class MainActivity extends Activity {

    private TextView closeTime;
    private TextView openTime;
    private Button startAlarm;
    private Button closeAlarm;

    private Button btn_skip;

    private DevicePolicyManager devicePolicyManager;
    private boolean isAdminActive;

    private final String CLOSE_TAG = "息屏";
    private final String OPEN_TAG = "亮屏";

    //亮屏时间
    private int openHour = 0;
    private int openMinute = 0;

    //息屏时间
    private int closeHour = 0;
    private int closeMinute = 0;

    //判断亮屏的初始时间
    private boolean isFirst = true;

    // 键盘管理器
    KeyguardManager keyguardManager;
    // 键盘锁
    private KeyguardManager.KeyguardLock keyguardLock;
    // 电源管理器
    private PowerManager powerManager;
    // 唤醒锁
    private PowerManager.WakeLock wakeLock;

    public static final int WAKE_UNLOCK = 0x1123;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case WAKE_UNLOCK:
                    Log.e("WakeActivity", "handleMessage begin----------");

                    // 点亮亮屏
                    wakeLock = powerManager.newWakeLock
                            (PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                    PowerManager.SCREEN_DIM_WAKE_LOCK |
                                    PowerManager.ON_AFTER_RELEASE, "bright");

//                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//                            MyBroadcast.class.getName());//acquire lock for the service

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
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setLockTime();

        //常亮     软件必须前台运行
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        EventBus.getDefault().register(this);
        initData();
        initView();

    }

    private void setLockTime() {

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.System.canWrite(getApplicationContext())) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                getApplicationContext().startActivity(intent);
//                Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT,Integer.MAX_VALUE);
//            }
//        }else{
//            Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT,Integer.MAX_VALUE);
//        }

        try {
            float result  = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Log.e("setLockTime", "result = " + result);

            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);

        } catch (Settings.SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void initView() {

        openTime = (TextView) findViewById(R.id.openTime);
        openTime.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog("o");
            }
        });

        closeTime = (TextView) findViewById(R.id.closeTime);
        closeTime.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog("c");
            }
        });

        btn_skip = (Button) findViewById(R.id.btn_skip);
        btn_skip.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LightActivity.class);
                startActivity(intent);
            }
        });

        startAlarm = (Button) findViewById(R.id.startAlarm);
        startAlarm.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (closeTime.getText() == "" || openTime.getText() == "") {
                    errorDialog("        时间不能为空");
                }else{
                    isFirst = true;
//                cancel(v);
                    stopAlarm(0);

                    Toast.makeText(MainActivity.this, "开启定时", Toast.LENGTH_SHORT).show();
                    startOpenAlarm();
//                    startTimer();
                    //startCloseAlarm();
                }
            }
        });

        closeAlarm = (Button) findViewById(R.id.closeAlarm);
        closeAlarm.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                isFirst = true;
//                cancel(v);
                stopAlarm(1);
            }
        });
    }

    private void errorDialog(String msg){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage(msg)
                .setPositiveButton("确定", null)
                .show();
    }

    private void initData() {

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        //锁屏权限
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        // 申请权限
        ComponentName componentName = new ComponentName(this, MyAdmin.class);
        // 判断该组件是否有系统管理员的权限
        isAdminActive = devicePolicyManager.isAdminActive(componentName);

        if (!isAdminActive) {
            Intent intent = new Intent();
            //指定动作
            intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            //指定给那个组件授权
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "锁屏");
            startActivity(intent);
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(EventData event) {
//        Log.e("MainActivity", "EventBus事件调用     name:" + event.name);
//        if (OPEN_TAG.equals(event.name)) {
////            status = 0;
//            Message msg = new Message();
//            msg.what = WAKE_UNLOCK;
//            //mHandler.sendMessageDelayed(msg, 100);
//            startCloseAlarm();
//            //startMp();
//        } else if (CLOSE_TAG.equals(event.name)) {
////            status = 1;
//            startOpenAlarm();
//            //devicePolicyManager.lockNow();
//            //stopMp();
//        }
//    }

    //测试
//    int second = 0;

    //启动亮屏定时
    public void startOpenAlarm() {
        Log.e("MainActivity", "startOpenAlarm        hourOfDay:" + openHour + "   minute:" + openMinute);
        //设置闹钟时间
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, openHour);//小时
        instance.set(Calendar.MINUTE, openMinute);//分钟
        instance.set(Calendar.SECOND, 0);//秒
        Log.e("MainActivity", "getActualMaximum:" + instance.get(Calendar.HOUR_OF_DAY));
        long mill = instance.getTimeInMillis();
//        second = second + 500;
//        Log.e("MainActivity", "startOpenAlarm        mill:" + mill + "   second:" + second);
//        Log.e("MainActivity", "startOpenAlarm        date:" + new Date(mill));
//        Log.e("MainActivity", "startOpenAlarm        currentMill:" + new Date().getTime());
        //如果不是第一次启动，就添加一天的时间
        if(!isFirst){
            //mill = System.currentTimeMillis();
            mill += 1000 * 60 * 60 * 24;
        }

        isFirst = false;

        //获取系统闹钟服务
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyBroadcast.class);
        intent.putExtra("tag", OPEN_TAG);

        //测试
        intent.putExtra("hour", openHour);
        intent.putExtra("minute", openMinute);
        intent.putExtra("isFirst", isFirst);

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

    //启动息屏定时
    public void startCloseAlarm() {
        Log.e("MainActivity", "startCloseAlarm        hourOfDay:" + closeHour + "   minute:" + closeMinute);
        //设置闹钟时间
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, closeHour);//小时
        instance.set(Calendar.MINUTE, closeMinute);//分钟
        instance.set(Calendar.SECOND, 0);//秒

        long mill = instance.getTimeInMillis();
        //second = second + 500;

        //测试
//        if(!isFirst){
//            mill = System.currentTimeMillis();
//            mill += 1000 * 5;
//        }

//        Log.e("MainActivity", "startCloseAlarm        mill:" + mill + "   second:" + second);

        //获取系统闹钟服务
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyBroadcast.class);
        intent.putExtra("tag", CLOSE_TAG);

        //测试
        intent.putExtra("hour", closeHour);
        intent.putExtra("minute", closeMinute);
        intent.putExtra("isFirst", false);

        PendingIntent op = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,mill,op);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,mill,op);
        }else{
            alarmManager.set(AlarmManager.RTC_WAKEUP,mill,op);
        }
        //一次性闹钟
        //alarmManager.set(AlarmManager.RTC_WAKEUP, mill, op);
    }

    //停止定时
    private void stopAlarm(int a){
        if(a == 1){
            Toast.makeText(MainActivity.this, "停止定时", Toast.LENGTH_SHORT).show();
        }
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, MyBroadcast.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.cancel(pi);
    }

    //设置定时时间
    private void showTimeDialog(final String tag) {
        /**
         * 0：初始化小时
         * 0：初始化分
         * true:是否采用24小时制
         */
        TimePickerDialog timeDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            //从这个方法中取得获得的时间
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay1,
                                  final int minute1) {
                String h = hourOfDay1 + "";
                String m = minute1 + "";

                if (hourOfDay1 < 10) {
                    h = "0" + hourOfDay1;
                }
                if (minute1 < 10) {
                    m = "0" + minute1;
                }

                Log.e("MainActivity", "hourOfDay:" + h + "   minute:" + m);
                if ("o".equals(tag)) {
                    openTime.setText(h + ":" + m);
                    openHour = hourOfDay1;
                    openMinute = minute1;
                } else if ("c".equals(tag)) {
                    if (hourOfDay1 < openHour || (hourOfDay1 == openHour && minute1 < openMinute)) {
                        errorDialog("       锁屏时间不能小于亮屏时间");
                    } else {
                        closeTime.setText(h + ":" + m);
                        closeHour = hourOfDay1;
                        closeMinute = minute1;
                    }
                }
            }
        }, 0, 0, true);
        timeDialog.show();
    }
}
