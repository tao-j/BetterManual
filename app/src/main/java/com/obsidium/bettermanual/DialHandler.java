package com.obsidium.bettermanual;

import android.view.KeyEvent;

import com.github.ma1co.pmcademo.app.DialPadKeysEvents;
import com.sony.scalar.sysutil.ScalarInput;

/**
 * Created by KillerInk on 24.08.2017.
 */

public class DialHandler {
    private DialPadKeysEvents dialEventListner;
    private DialPadKeysEvents defaultListner;

    public DialHandler(DialPadKeysEvents defaultListner)
    {
        this.defaultListner = defaultListner;
    }

    public void setDefaultListner()
    {
        this.dialEventListner = defaultListner;
    }

    public void setDialEventListner(DialPadKeysEvents eventListner)
    {
        this.dialEventListner = eventListner;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (dialEventListner != null) {
            switch (event.getScanCode()) {
                case ScalarInput.ISV_KEY_UP:
                    return dialEventListner.onUpKeyUp();
                case ScalarInput.ISV_KEY_DOWN:
                    return dialEventListner.onDownKeyUp();
                case ScalarInput.ISV_KEY_LEFT:
                    return dialEventListner.onLeftKeyUp();
                case ScalarInput.ISV_KEY_RIGHT:
                    return dialEventListner.onRightKeyUp();
                case ScalarInput.ISV_KEY_ENTER:
                    return dialEventListner.onEnterKeyUp();

                case ScalarInput.ISV_DIAL_1_CLOCKWISE:
                case ScalarInput.ISV_DIAL_1_COUNTERCW:
                case ScalarInput.ISV_DIAL_2_CLOCKWISE:
                case ScalarInput.ISV_DIAL_2_COUNTERCW:
                    return true;
            }
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getScanCode()) {
            case ScalarInput.ISV_KEY_UP:
                return dialEventListner.onUpKeyDown();
            case ScalarInput.ISV_KEY_DOWN:
                return dialEventListner.onDownKeyDown();
            case ScalarInput.ISV_KEY_LEFT:
                return dialEventListner.onLeftKeyDown();
            case ScalarInput.ISV_KEY_RIGHT:
                return dialEventListner.onRightKeyDown();
            case ScalarInput.ISV_KEY_ENTER:
                return dialEventListner.onEnterKeyDown();

            case ScalarInput.ISV_DIAL_1_CLOCKWISE:
                return dialEventListner.onUpperDialChanged(1);
            case ScalarInput.ISV_DIAL_1_COUNTERCW:
                return dialEventListner.onUpperDialChanged(-1);
            case ScalarInput.ISV_DIAL_2_CLOCKWISE:
                return dialEventListner.onLowerDialChanged(1);
            case ScalarInput.ISV_DIAL_2_COUNTERCW:
                return dialEventListner.onLowerDialChanged(-1);
        }
        return true;
    }
}
