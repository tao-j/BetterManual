package com.obsidium.bettermanual.model;

import android.os.Handler;
import android.os.Looper;

import com.obsidium.bettermanual.camera.CameraInstance;



public abstract class AbstractModel<T> implements Model<T> {

    private Events eventsListner;
    protected boolean supported;
    protected T value;
    protected T[] values;
    protected int value_int;
    protected CameraInstance camera;
    private Handler handler = new Handler(Looper.getMainLooper());

    public AbstractModel(CameraInstance camera)
    {
        this.camera = camera;
    }

    @Override
    public void setListner(Events events) {
        this.eventsListner = events;
    }

    protected void fireOnValueChanged()
    {
        if (eventsListner != null)
            handler.post(()-> eventsListner.onValueChanged());
    }

    protected void fireOnSupportedChanged()
    {
        if (eventsListner != null)
            handler.post(()-> eventsListner.onIsSupportedChanged());
    }
}
