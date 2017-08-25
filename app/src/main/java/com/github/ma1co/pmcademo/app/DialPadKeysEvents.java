package com.github.ma1co.pmcademo.app;

/**
 * Created by KillerInk on 24.08.2017.
 */

public interface DialPadKeysEvents {
    boolean onUpperDialChanged(int value);
    boolean onLowerDialChanged(int value);

    boolean onUpKeyDown();
    boolean onUpKeyUp();
    boolean onDownKeyDown();
    boolean onDownKeyUp();
    boolean onLeftKeyDown();
    boolean onLeftKeyUp();
    boolean onRightKeyDown();
    boolean onRightKeyUp();
    boolean onEnterKeyDown();
    boolean onEnterKeyUp();
}
