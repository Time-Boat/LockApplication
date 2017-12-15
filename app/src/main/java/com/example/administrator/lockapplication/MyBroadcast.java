package com.example.administrator.lockapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class MyBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String tag = intent.getStringExtra("tag");
        int closeHour = intent.getIntExtra("closeHour", 0);
        int closeMinute = intent.getIntExtra("closeMinute", 0);
        int openHour = intent.getIntExtra("openHour", 0);
        int openMinute = intent.getIntExtra("openMinute", 0);
        boolean isFirst = intent.getBooleanExtra("isFirst", false);
        Log.e("MyBroadcast", tag);
        Log.e("MyBroadcast", tag);
        Toast.makeText(context,tag,Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(new EventData(tag, closeHour, closeMinute, openHour, openMinute, isFirst));
    }

}
