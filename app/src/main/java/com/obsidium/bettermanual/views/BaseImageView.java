package com.obsidium.bettermanual.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.obsidium.bettermanual.ActivityInterface;

public abstract class BaseImageView extends ImageView
{

    protected ActivityInterface activity;

    public BaseImageView(Context context) {
        super(context);
    }

    public BaseImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setActivity(ActivityInterface activity)
    {
        this.activity = activity;
    }

    public abstract void updateImage();

    public abstract void toggle();
}
