package com.obsidium.bettermanual.controller;

import android.graphics.Color;
import android.widget.ImageView;

import com.obsidium.bettermanual.model.Model;

public abstract class ImageViewController<M extends Model> extends AbstractController<ImageView, M> {

    @Override
    public void bindModel(M model) {
        super.bindModel(model);
        if (view != null && model != null)
            view.post(()->updateImage()) ;
    }

    @Override
    public void bindView(ImageView imageView) {
        super.bindView(imageView);
        updateImage();
    }

    @Override
    public void setColorToView(Integer color) {
        if (color == Color.WHITE)
            view.setColorFilter(null);
        else
            view.setColorFilter(color);
    }

    @Override
    public void onValueChanged() {
        updateImage();
    }

    protected abstract void updateImage();
}
