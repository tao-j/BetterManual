package com.obsidium.bettermanual.controller;

import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.obsidium.bettermanual.model.Model;

public abstract class TextViewController<M extends Model<String>> extends AbstractController<TextView, M> {

    private static final String TAG = TextViewController.class.getSimpleName();

    @Override
    public void bindView(TextView v) {
        super.bindView(v);
        setText();
    }

    @Override
    public void bindModel(M model) {
        super.bindModel(model);
        if (view != null)
            view.post(()-> setText());
    }

    @Override
    public void setColorToView(Integer color) {
        if (view != null)
            view.setTextColor(color);
    }

    @Override
    public void onValueChanged() {
        setText();
    }

    protected void setText()
    {
        if (model == null)
            Log.d(TAG, "model is null");
        if (view == null)
            Log.d(TAG, "view is null");
        if (model!= null && view != null){
            view.setText(model.getValue());
        }
    }
}
