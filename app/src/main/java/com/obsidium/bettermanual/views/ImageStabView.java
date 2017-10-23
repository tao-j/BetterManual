package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;

import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;

/**
 * Created by KillerInk on 31.08.2017.
 */

public class ImageStabView extends BaseImageView {

    private final String TAG = ImageStabView.class.getSimpleName();
    public ImageStabView(Context context) {
        super(context);
    }

    public ImageStabView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageStabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setIn_DecrementValue(int value) {

    }

    @Override
    public String getNavigationString() {
        return getResources().getString(R.string.view_drivemode_navigation);
    }

    //onetime
    @Override
    public void updateImage() {
        final String stabilisationMode= CameraInstance.GET().getImageStabilisationMode();
        if (stabilisationMode.equals("onetime"))
            setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_fn_1_5_layer_sel_steadyshot_on));
        else
            setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_fn_1_5_layer_sel_steadyshot_off));
    }

    @Override
    public void toggle() {
        final String stabilisationMode= CameraInstance.GET().getImageStabilisationMode();
        if(stabilisationMode.equals("onetime"))
            CameraInstance.GET().setImageStabilisation("off");
        else
            CameraInstance.GET().setImageStabilisation("onetime");
        updateImage();
    }
}
