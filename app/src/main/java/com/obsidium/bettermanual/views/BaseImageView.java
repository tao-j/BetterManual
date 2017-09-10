package com.obsidium.bettermanual.views;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.github.killerink.ActivityInterface;

public abstract class BaseImageView extends ImageView implements DialViewInterface
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

    @Override
    public void setColorToView(Integer color) {
        if (color == Color.WHITE)
            setColorFilter(null);
        else
            setColorFilter(color);
    }
}
