package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;

public class ImageStabilisationModel extends ApertureModel
{
    public ImageStabilisationModel(CameraInstance camera) {
        super(camera);
    }

    @Override
    public void setValue(int i) {
        final String stabilisationMode= camera.getImageStabilisationMode();
        value = stabilisationMode;
        if(stabilisationMode.equals("onetime"))
            camera.setImageStabilisation("off");
        else
            camera.setImageStabilisation("onetime");
        fireOnValueChanged();
    }
}
