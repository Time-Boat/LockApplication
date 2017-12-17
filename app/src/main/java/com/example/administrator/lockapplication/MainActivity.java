package com.example.administrator.lockapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.administrator.lockapplication.floatball.FloatBallService;
import com.example.administrator.lockapplication.floatball.LightActivity;

import java.util.Calendar;

public class MainActivity extends Activity {

    private TextView closeTime;
    private TextView openTime;
    private Button startAlarm;
    private Button closeAlarm;

    private Button btn_skip;

    private DevicePolicyManager devicePolicyManager;
    private boolean isAdminActive;

    //亮屏时间
    private int openHour = 0;
    private int openMinute = 0;

    //息屏时间
    private int closeHour = 0;
    private int closeMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setLockTime();
        initData();
        initView();

    }

    //不知道干嘛的
    private void setLockTime() {
        try {
            float result  = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Log.e("setLockTime", "result = " + result);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
        } catch (Settings.SettingNotFoundException e) {
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
                    stopAlarm(0);
                    Toast.makeText(MainActivity.this, "开启定时", Toast.LENGTH_SHORT).show();
                    startAlarm();
                }
            }
        });

        closeAlarm = (Button) findViewById(R.id.closeAlarm);
        closeAlarm.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    //启动亮屏定时
    public void startAlarm() {
        Log.e("MainActivity", "startOpenAlarm        hourOfDay:" + openHour + "   minute:" + openMinute);

        //获取系统闹钟服务
        Intent intent = new Intent(this, MyBroadcast.class);
        intent.putExtra("tag", FloatBallService.CLOSE_TAG);

        intent.putExtra("openHour", openHour);
        intent.putExtra("openMinute", openMinute);
        intent.putExtra("closeHour", closeHour);
        intent.putExtra("closeMinute", closeMinute);
        intent.putExtra("isFirst", true);

        this.sendBroadcast(intent);
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
