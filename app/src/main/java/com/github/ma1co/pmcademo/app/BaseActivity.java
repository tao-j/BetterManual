package com.github.ma1co.pmcademo.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.github.ma1co.openmemories.framework.DeviceInfo;
import com.obsidium.bettermanual.DialHandler;
import com.sony.scalar.hardware.avio.DisplayManager;
import com.sony.scalar.sysutil.ScalarInput;
import com.sony.scalar.sysutil.didep.Gpelibrary;

public class BaseActivity extends Activity implements DialPadKeysEvents {
    public static final String NOTIFICATION_DISPLAY_CHANGED = "NOTIFICATION_DISPLAY_CHANGED";

    public static final String KEY_ACCESSORY_APO = "KEY_ACCESSORY_APO";
    public static final String KEY_DEDICATED_APO = "KEY_DEDICATED_APO";
    public static final String KEY_LENS_APO = "KEY_LENS_APO";
    public static final String KEY_MEDIA_INOUT_APO = "KEY_MEDIA_INOUT_APO";
    public static final String KEY_PLAY_APO = "KEY_PLAY_APO";
    public static final String KEY_PLAY_PON = "KEY_PLAY_PON";
    public static final String KEY_POWER_APO = "KEY_POWER_APO";
    public static final String KEY_POWER_SLIDE_PON = "KEY_POWER_SLIDE_PON";
    public static final String KEY_RELEASE_APO = "KEY_RELEASE_APO";

    private DisplayManager displayManager;

    protected DialHandler dialHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialHandler = new DialHandler(this);
    }

    @Override
    protected void onResume() {
        Logger.info("Resume " + getComponentName().getClassName());
        super.onResume();

        setColorDepth(true);
        notifyAppInfo();

        displayManager = new DisplayManager();
        displayManager.setDisplayStatusListener(new DisplayManager.DisplayEventListener() {
            @Override
            public void onDeviceStatusChanged(int event) {
                if (event == DisplayManager.EVENT_SWITCH_DEVICE)
                    onDisplayChanged(displayManager.getActiveDevice());
            }
        });
    }

    @Override
    protected void onPause() {
        Logger.info("Pause " + getComponentName().getClassName());
        super.onPause();

        setColorDepth(false);

        displayManager.releaseDisplayStatusListener();
        displayManager.finish();
        displayManager = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        dialHandler.onKeyDown(keyCode,event);
        switch (event.getScanCode()) {
            case ScalarInput.ISV_KEY_FN:
                return onFnKeyDown();
            case ScalarInput.ISV_KEY_AEL:
                return onAelKeyDown();
            case ScalarInput.ISV_KEY_MENU:
            case ScalarInput.ISV_KEY_SK1:
                return onMenuKeyDown();
            case ScalarInput.ISV_KEY_S1_1:
                return onFocusKeyDown();
            case ScalarInput.ISV_KEY_S1_2:
                return true;
            case ScalarInput.ISV_KEY_S2:
                return onShutterKeyDown();
            case ScalarInput.ISV_KEY_PLAY:
                return onPlayKeyDown();
            case ScalarInput.ISV_KEY_STASTOP:
                return onMovieKeyDown();
            case ScalarInput.ISV_KEY_CUSTOM1:
                return onC1KeyDown();
            case ScalarInput.ISV_KEY_DELETE:
            case ScalarInput.ISV_KEY_SK2:
                return onDeleteKeyDown();
            case ScalarInput.ISV_KEY_LENS_ATTACH:
                return onLensAttached();
            case ScalarInput.ISV_KEY_MODE_DIAL:
                return onModeDialChanged(getDialStatus(ScalarInput.ISV_KEY_MODE_DIAL));
            case ScalarInput.ISV_KEY_ZOOM_OFF: // zoom not active
                return onZoomOffKey();
            case ScalarInput.ISV_KEY_ZOOM_TELE: //zoom in
                return onZoomTeleKey();
            case ScalarInput.ISV_KEY_IR_ZOOM_WIDE: //zoom out
                return onZoomWideKey();
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        dialHandler.onKeyUp(keyCode,event);
        switch (event.getScanCode()) {

            case ScalarInput.ISV_KEY_FN:
                return onFnKeyUp();
            case ScalarInput.ISV_KEY_AEL:
                return onAelKeyUp();
            case ScalarInput.ISV_KEY_MENU:
            case ScalarInput.ISV_KEY_SK1:
                return onMenuKeyUp();
            case ScalarInput.ISV_KEY_S1_1:
                return onFocusKeyUp();
            case ScalarInput.ISV_KEY_S1_2:
                return true;
            case ScalarInput.ISV_KEY_S2:
                return onShutterKeyUp();
            case ScalarInput.ISV_KEY_PLAY:
                return onPlayKeyUp();
            case ScalarInput.ISV_KEY_STASTOP:
                return onMovieKeyUp();
            case ScalarInput.ISV_KEY_CUSTOM1:
                return onC1KeyUp();
            case ScalarInput.ISV_KEY_DELETE:
            case ScalarInput.ISV_KEY_SK2:
                return onDeleteKeyUp();
            case ScalarInput.ISV_KEY_LENS_ATTACH:
                return onLensDetached();
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    protected int getDialStatus(int key) {
        return ScalarInput.getKeyStatus(key).status;
    }

    protected boolean onFnKeyDown() { return false; }
    protected boolean onFnKeyUp() { return false; }
    protected boolean onAelKeyDown() { return false; }
    protected boolean onAelKeyUp() { return false; }
    protected boolean onMenuKeyDown() { return false; }
    protected boolean onMenuKeyUp() { return false; }
    protected boolean onFocusKeyDown() { return false; }
    protected boolean onFocusKeyUp() { return false; }
    protected boolean onShutterKeyDown() { return false; }
    protected boolean onShutterKeyUp() { return false; }
    protected boolean onPlayKeyDown() { return false; }
    protected boolean onPlayKeyUp() { return false; }
    protected boolean onMovieKeyDown() { return false; }
    protected boolean onMovieKeyUp() { return false; }
    protected boolean onC1KeyDown() { return false; }
    protected boolean onC1KeyUp() { return false; }
    protected boolean onLensAttached() { return false; }
    protected boolean onLensDetached() { return false; }
    protected boolean onModeDialChanged(int value) { return false; }

    protected boolean onZoomTeleKey(){return false;}
    protected boolean onZoomWideKey(){return false;}
    protected boolean onZoomOffKey(){return false;}


    protected boolean onDeleteKeyDown() {
        return true;
    }
    protected boolean onDeleteKeyUp() {
        onBackPressed();
        return true;
    }

    public void onDisplayChanged(String device) {
        AppNotificationManager.getInstance().notify(NOTIFICATION_DISPLAY_CHANGED);
    }

    protected void setAutoPowerOffMode(boolean enable) {
        String mode = enable ? "APO/NORMAL" : "APO/NO";// or "APO/SPECIAL" ?
        Intent intent = new Intent();
        intent.setAction("com.android.server.DAConnectionManagerService.apo");
        intent.putExtra("apo_info", mode);
        sendBroadcast(intent);
    }

    protected void setColorDepth(boolean highQuality) {
        Gpelibrary.GS_FRAMEBUFFER_TYPE type = highQuality ? Gpelibrary.GS_FRAMEBUFFER_TYPE.ABGR8888 : Gpelibrary.GS_FRAMEBUFFER_TYPE.RGBA4444;
        Gpelibrary.changeFrameBufferPixel(type);
    }

    protected void notifyAppInfo() {
        Intent intent = new Intent("com.android.server.DAConnectionManagerService.AppInfoReceive");
        intent.putExtra("package_name", getComponentName().getPackageName());
        intent.putExtra("class_name", getComponentName().getClassName());
        //intent.putExtra("pkey", new String[] {});// either this or these two:
        //intent.putExtra("pullingback_key", new String[] {});
        // Exit app when plugging camera into USB
        intent.putExtra("pullingback_key", new String[] { "KEY_USB_CONNECT" });
        // Automatically resume app after power off etc.
        intent.putExtra("resume_key", new String[] { KEY_POWER_SLIDE_PON, KEY_RELEASE_APO, KEY_PLAY_APO,
                KEY_MEDIA_INOUT_APO, KEY_LENS_APO, KEY_ACCESSORY_APO, KEY_DEDICATED_APO, KEY_POWER_APO, KEY_PLAY_PON });
        sendBroadcast(intent);
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    public DeviceInfo getDeviceInfo() {
        return DeviceInfo.getInstance();
    }

    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onUpKeyDown() {
        return false;
    }

    @Override
    public boolean onUpKeyUp() {
        return false;
    }

    @Override
    public boolean onDownKeyDown() {
        return false;
    }

    @Override
    public boolean onDownKeyUp() {
        return false;
    }

    @Override
    public boolean onLeftKeyDown() {
        return false;
    }

    @Override
    public boolean onLeftKeyUp() {
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
        return false;
    }

    @Override
    public boolean onEnterKeyDown() {
        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
        return false;
    }
}
