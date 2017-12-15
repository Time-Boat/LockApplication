package com.example.administrator.lockapplication;

/**
 * Author：leguang on 2016/10/9 0009 15:49
 * Email：langmanleguang@qq.com
 */
public class EventData {
    public String name;

    public int closeHour;
    public int closeMinute;

    public int openHour;
    public int openMinute;

    public boolean isFirst;

    public EventData(String name, int closeHour, int closeMinute, int openHour, int openMinute, boolean isFirst) {
        this.name = name;
        this.closeHour = closeHour;
        this.closeMinute = closeMinute;
        this.openHour = openHour;
        this.openMinute = openMinute;
        this.isFirst = isFirst;
    }
}
