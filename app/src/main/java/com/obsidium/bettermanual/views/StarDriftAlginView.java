package com.obsidium.bettermanual.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class StarDriftAlginView extends View {

    private int margine = 5;
    private Paint crossPaint;
    private Paint gridPaint;
    private final int gridSize = 20;

    private boolean drawGrid = false;

    public StarDriftAlginView(Context context) {
        super(context);
        initPaint();
    }

    public StarDriftAlginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    void initPaint()
    {
        crossPaint = new Paint();
        crossPaint.setStyle(Paint.Style.STROKE);
        crossPaint.setColor(Color.RED);
        crossPaint.setStrokeWidth(1);
        crossPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        gridPaint = new Paint();
        gridPaint.setAntiAlias(false);
        gridPaint.setARGB(100, 100, 100, 100);
        gridPaint.setStrokeWidth(1);
        gridPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth()/2;
        float centerY = getHeight()/2;

        if (drawGrid)
        {
            for (int i = gridSize; i < getHeight(); i+=gridSize)
            {
                canvas.drawLine(0, i, getWidth(), i,gridPaint);
            }

            for (int i = gridSize; i < getWidth(); i+=gridSize)
            {
                canvas.drawLine(i, 0, i, getHeight(),gridPaint);
            }
        }

        canvas.drawLine(centerX - margine,0,centerX-margine, getHeight(),crossPaint);
        canvas.drawLine(centerX + margine,0,centerX+margine, getHeight(),crossPaint);

        canvas.drawLine(0, centerY -margine, getWidth(), centerY-margine,crossPaint);
        canvas.drawLine(0, centerY +margine, getWidth(), centerY+margine,crossPaint);


    }

    public void enableGrid(boolean enable)
    {
        this.drawGrid = enable;
        invalidate();
    }

    public void increaseSize()
    {
        if (margine < 100)
            margine++;
    }

    public void decraseSize()
    {
        if (margine > 2)
            margine--;
    }
}
