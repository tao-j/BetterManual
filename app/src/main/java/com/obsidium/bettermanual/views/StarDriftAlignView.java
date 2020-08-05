package com.obsidium.bettermanual.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class StarDriftAlignView extends View {

    private int margin = 5;
    private Paint crossPaint;
    private Paint gridPaint;
    private final int gridSize = 20;

    private boolean drawGrid = false;
    private boolean drawCenterLines = false;

    public StarDriftAlignView(Context context) {
        super(context);
        initPaint();
    }

    public StarDriftAlignView(Context context, AttributeSet attrs) {
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

        if (drawCenterLines) {
            canvas.drawLine(centerX - margin, 0, centerX - margin, getHeight(), crossPaint);
            canvas.drawLine(centerX + margin, 0, centerX + margin, getHeight(), crossPaint);

            canvas.drawLine(0, centerY - margin, getWidth(), centerY - margin, crossPaint);
            canvas.drawLine(0, centerY + margin, getWidth(), centerY + margin, crossPaint);
        }
    }

    public void enableGrid(boolean enable)
    {
        this.drawGrid = enable;
        invalidate();
    }

    public void enableCenterLines(boolean enable)
    {
        this.drawCenterLines = enable;
        invalidate();
    }

    public void increaseSize()
    {
        if (margin < 100)
            margin++;
    }

    public void decraseSize()
    {
        if (margin > 2)
            margin--;
    }
}
