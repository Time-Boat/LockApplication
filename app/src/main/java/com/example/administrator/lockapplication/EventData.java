package com.example.administrator.lockapplication;

/**
 * Author：leguang on 2016/10/9 0009 15:49
 * Email：langmanleguang@qq.com
 */
public class EventData {
    public String name;

    public int hour;
    public int minute;

    public boolean isFirst;

    public EventData(String name, int hour, int minute, boolean isFirst) {
        this.name = name;
        this.hour = hour;
        this.minute = minute;
        this.isFirst = isFirst;
    }
}
