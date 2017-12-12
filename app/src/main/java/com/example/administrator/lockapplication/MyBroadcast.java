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
        int hour = intent.getIntExtra("hour", 0);
        int minute = intent.getIntExtra("minute", 0);
        boolean isFirst = intent.getBooleanExtra("isFirst", false);
        Log.e("MyBroadcast", tag);
        Toast.makeText(context,tag,Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(new EventData(tag, hour, minute, isFirst));
    }

}
