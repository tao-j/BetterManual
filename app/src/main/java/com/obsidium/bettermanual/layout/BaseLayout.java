package com.obsidium.bettermanual.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.KeyEvents;

/**
 * Created by KillerInk on 27.09.2017.
 */

public abstract class BaseLayout extends RelativeLayout implements KeyEvents
{
    protected ActivityInterface activityInterface;
    protected Context context;

    public BaseLayout(Context context) {
        super(context);
        this.context = context;
    }

    public void inflateLayout(int layoutid)
    {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutid, this);
    }

    public BaseLayout(Context context,ActivityInterface activityInterface) {
        this(context);
        this.activityInterface =activityInterface;
    }

    public abstract void Destroy();

}
