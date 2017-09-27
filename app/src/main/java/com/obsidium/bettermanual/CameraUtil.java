package com.obsidium.bettermanual;

import android.util.Pair;

import com.obsidium.bettermanual.camera.ShutterSpeedValue;

public class CameraUtil
{
    public static final ShutterSpeedValue[] SHUTTER_SPEED_VALUES = new ShutterSpeedValue[]
            {
                    new ShutterSpeedValue(1, 4000),
                    new ShutterSpeedValue(1, 3200),
                    new ShutterSpeedValue(1, 2500),
                    new ShutterSpeedValue(1, 2000),
                    new ShutterSpeedValue(1, 1600),
                    new ShutterSpeedValue(1, 1250),
                    new ShutterSpeedValue(1, 1000),
                    new ShutterSpeedValue(1, 800),
                    new ShutterSpeedValue(1, 640),
                    new ShutterSpeedValue(1, 500),
                    new ShutterSpeedValue(1, 400),
                    new ShutterSpeedValue(1, 320),
                    new ShutterSpeedValue(1, 250),
                    new ShutterSpeedValue(1, 200),
                    new ShutterSpeedValue(1, 160),
                    new ShutterSpeedValue(1, 125),
                    new ShutterSpeedValue(1, 100),
                    new ShutterSpeedValue(1, 80),
                    new ShutterSpeedValue(1, 60),
                    new ShutterSpeedValue(1, 50),
                    new ShutterSpeedValue(1, 40),
                    new ShutterSpeedValue(1, 30),
                    new ShutterSpeedValue(1, 25),
                    new ShutterSpeedValue(1, 20),
                    new ShutterSpeedValue(1, 15),
                    new ShutterSpeedValue(1, 13),
                    new ShutterSpeedValue(1, 10),
                    new ShutterSpeedValue(1, 8),
                    new ShutterSpeedValue(1, 6),
                    new ShutterSpeedValue(1, 5),
                    new ShutterSpeedValue(1, 4),
                    new ShutterSpeedValue(1, 3),
                    new ShutterSpeedValue(10, 25),
                    new ShutterSpeedValue(1, 2),
                    new ShutterSpeedValue(10, 16),
                    new ShutterSpeedValue(4, 5),
                    new ShutterSpeedValue(1, 1),
                    new ShutterSpeedValue(13, 10),
                    new ShutterSpeedValue(16, 10),
                    new ShutterSpeedValue(2, 1),
                    new ShutterSpeedValue(25, 10),
                    new ShutterSpeedValue(16, 5),
                    new ShutterSpeedValue(4, 1),
                    new ShutterSpeedValue(5, 1),
                    new ShutterSpeedValue(6, 1),
                    new ShutterSpeedValue(8, 1),
                    new ShutterSpeedValue(10, 1),
                    new ShutterSpeedValue(13, 1),
                    new ShutterSpeedValue(15, 1),
                    new ShutterSpeedValue(20, 1),
                    new ShutterSpeedValue(25, 1),
                    new ShutterSpeedValue(30, 1),
            };

    public static int getShutterValueIndex(final Pair<Integer,Integer> speed)
    {
        return getShutterValueIndex(speed.first, speed.second);
    }

    public static int getShutterValueIndex(int n, int d)
    {
        for (int i = 0; i < SHUTTER_SPEED_VALUES.length; ++i)
        {
            if (SHUTTER_SPEED_VALUES[i].getNumerator() == n &&
                    SHUTTER_SPEED_VALUES[i].getDenominator() == d)
            {
                return i;
            }
        }
        return -1;
    }

    public static String formatShutterSpeed(int n, int d)
    {
        if (n == 1 && d != 2 && d != 1)
            return String.format("%d/%d", n, d);
        else if (d == 1)
        {
            if (n == 65535)
                return "BULB";
            else
                return String.format("%d\"", n);
        }
        else
            return String.format("%.1f\"", (float) n / (float) d);
    }
}
