package com.github.killerink;

/**
 * Created by KillerInk on 24.08.2017.
 */

public interface KeyEvents {
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

    boolean onFnKeyDown();
    boolean onFnKeyUp();
    boolean onAelKeyDown();
    boolean onAelKeyUp();
    boolean onMenuKeyDown();
    boolean onMenuKeyUp();
    boolean onFocusKeyDown();
    boolean onFocusKeyUp();
    boolean onShutterKeyDown();
    boolean onShutterKeyUp();
    boolean onPlayKeyDown();
    boolean onPlayKeyUp();
    boolean onMovieKeyDown();
    boolean onMovieKeyUp();
    boolean onC1KeyDown();
    boolean onC1KeyUp();
    boolean onLensAttached();
    boolean onLensDetached();
    boolean onModeDialChanged(int value);

    boolean onZoomTeleKey();
    boolean onZoomWideKey();
    boolean onZoomOffKey();


    boolean onDeleteKeyDown();
    boolean onDeleteKeyUp();
}
