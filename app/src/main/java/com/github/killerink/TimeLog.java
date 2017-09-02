package com.github.killerink;

import android.util.Log;

public class TimeLog {
    private final String TAG = TimeLog.class.getSimpleName();
    private long startTime;
    private String classs;

    public TimeLog(String classs)
    {
        this.classs = classs;
        startTime = System.currentTimeMillis();
    }

    public void logTime()
    {
        Log.d(TAG,classs +" " + (System.currentTimeMillis() - startTime) + " ms gone since start");

    }
}
