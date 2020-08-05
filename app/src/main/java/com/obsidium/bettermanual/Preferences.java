package com.obsidium.bettermanual;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sony.scalar.hardware.CameraEx;

public class Preferences {

    @SuppressLint("StaticFieldLeak")
    private static Preferences preferences;
    private SharedPreferences m_prefs;
    private Context context;

    public static void CREATE(Context context) {
        preferences = new Preferences(context);
    }

    public static Preferences GET() {
        return preferences;
    }

    public static void CLEAR() {
        if (preferences != null) {
            preferences.clear();
            preferences = null;
        }
    }

    private Preferences(Context context) {
        this.context = context;
        m_prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void clear() {
        this.context = null;
        this.m_prefs = null;
    }

    public Context getContext() {
        return context;
    }

    public String getSceneMode() {
        return m_prefs.getString(context.getString(R.string.ask_sceneMode), CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE);
    }

    public void setSceneMode(String mode) {
        m_prefs.edit().putString(context.getString(R.string.ask_sceneMode), mode).apply();
    }

    public String getDriveMode() {
        return m_prefs.getString(context.getString(R.string.ask_driveMode), CameraEx.ParametersModifier.DRIVE_MODE_BURST);
    }

    public void setDriveMode(String mode) {
        m_prefs.edit().putString(context.getString(R.string.ask_driveMode), mode).apply();
    }

    public String getBurstDriveSpeed() {
        return m_prefs.getString(context.getString(R.string.ask_burstDriveSpeed), CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH);
    }

    public void setBurstDriveSpeed(String speed) {
        m_prefs.edit().putString(context.getString(R.string.ask_burstDriveSpeed), speed).apply();
    }

    public int getMinShutterSpeed() {
        return m_prefs.getInt(context.getString(R.string.ask_minShutterSpeed), -1);
    }

    public void setMinShutterSpeed(int speed) {
        m_prefs.edit().putInt(context.getString(R.string.ask_minShutterSpeed), speed).apply();
    }

    public int getViewFlags(int defaultValue) {
        return m_prefs.getInt(context.getString(R.string.ask_viewFlags), defaultValue);
    }

    public void setViewFlags(int flags) {
        m_prefs.edit().putInt(context.getString(R.string.ask_viewFlags), flags).apply();
    }

    public int getDialMode(int defaultValue) {
        return m_prefs.getInt(context.getString(R.string.ask_dialMode), defaultValue);
    }

    public void setDialMode(int flags) {
        m_prefs.edit().putInt(context.getString(R.string.ask_dialMode), flags).apply();
    }

    public long getBulbTime() {
        return m_prefs.getLong(context.getString(R.string.ask_bulbtime), 1000);
    }

    public void setBulbTime(long time) {
        m_prefs.edit().putLong(context.getString(R.string.ask_bulbtime), time).commit();
    }

    public boolean showStarAlignView() {
        return m_prefs.getBoolean(context.getString(R.string.ask_showstaralign), false);
    }

    public void setShowStarAlign(boolean show) {
        m_prefs.edit().putBoolean(context.getString(R.string.ask_showstaralign), show).commit();
    }

    public boolean showStarAlignViewGrid() {
        return m_prefs.getBoolean(context.getString(R.string.ask_showstaraligngrid), false);
    }

    public void setShowStarAlignGrid(boolean show) {
        m_prefs.edit().putBoolean(context.getString(R.string.ask_showstaraligngrid), show).commit();
    }
}
