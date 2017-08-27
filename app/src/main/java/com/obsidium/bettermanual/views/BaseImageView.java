package com.obsidium.bettermanual.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.github.killerink.FragmentActivityInterface;

public abstract class BaseImageView extends ImageView implements DialValueSet
{

    protected FragmentActivityInterface activity;

    public BaseImageView(Context context) {
        super(context);
    }

    public BaseImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setActivity(FragmentActivityInterface activity)
    {
        this.activity = activity;
    }

    public abstract void updateImage();

    public abstract void toggle();
}
