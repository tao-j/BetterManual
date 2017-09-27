package com.obsidium.bettermanual.camera;

import android.util.Pair;

import com.obsidium.bettermanual.CameraUtil;

/**
 * Created by KillerInk on 11.09.2017.
 */

public class ShutterSpeedValue {

    private int numerator;
    private int denominator;

    public ShutterSpeedValue(int numerator,int denominator)
    {
        this.denominator = denominator;
        this.numerator = numerator;
    }

    public Pair<Integer,Integer> getPair()
    {
        return new Pair<Integer, Integer>(numerator, denominator);
    }

    public int getMillisecond()
    {
        return (int)(((float)numerator/ (float)denominator) * 1000000);
    }

    public int getNumerator()
    {
        return numerator;
    }

    public int getDenominator()
    {
        return denominator;
    }

    public String getShutterSpeed()
    {
        return CameraUtil.formatShutterSpeed(numerator, denominator);
    }


}
