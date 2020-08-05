package com.obsidium.bettermanual.layout;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.MainActivity;
import com.obsidium.bettermanual.R;
import com.sony.scalar.graphics.OptimizedImage;
import com.sony.scalar.graphics.OptimizedImageFactory;
import com.sony.scalar.media.AvindexContentInfo;
import com.sony.scalar.widget.OptimizedImageView;


public class ImageFragment extends BaseLayout {

    private final String TAG = ImageFragment.class.getSimpleName();
    private OptimizedImageView imageView;
    private FrameLayout surfaceViewParent;
    OptimizedImage image;
    AvindexContentInfo info;
    private float scaleFactor = 1;
    private final float scaleStep = 0.2f;
    private final float maxScaleFactor = 8;
    private TextView zoomImageView;
    private boolean zoomEnabled = false;

    public ImageFragment(Context context,ActivityInterface activityInterface) {
        super(context,activityInterface);
        inflateLayout(R.layout.image_fragment);
        surfaceViewParent = (FrameLayout) findViewById(R.id.surfaceParentView);
        zoomImageView = (TextView)findViewById(R.id.iv_zoom);
        zoomImageView.setVisibility(GONE);
        imageView = new OptimizedImageView(getContext());
        surfaceViewParent.addView(imageView);
        imageView.setDisplayPosition(new Point(0,0), OptimizedImageView.PositionType.POS_TYPE_NONE);
        Log.d(TAG, "ImageCount:" + activityInterface.getAvIndexManager().getCount());
        activityInterface.getAvIndexManager().update();
        loadOptimizedImg();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void Destroy() {
        closeImageInfo();
        closeOptimizedImage();
    }

    private void loadOptimizedImg()
    {
        if (activityInterface.getAvIndexManager().getPosition() > -1 && activityInterface.getAvIndexManager().getCount() > 0)
        {
            Log.d(TAG,"MEDIA");

            String data = activityInterface.getAvIndexManager().getData();
            Log.d(TAG,"Img path:" + data);
            Log.d(TAG,"Img folder:" + activityInterface.getAvIndexManager().getFolder());
            Log.d(TAG,"Img name:" + activityInterface.getAvIndexManager().getFileName());

            closeImageInfo();

            info = activityInterface.getAvIndexManager().getContentInfo();
            OptimizedImageFactory.Options options = new OptimizedImageFactory.Options();
            options.bBasicInfo = false;
            options.bCamInfo = false;
            options.bGpsInfo = false;
            options.bExtCamInfo = false;
            options.imageType = info.getAttributeInt(AvindexContentInfo.TAG_CONTENT_TYPE,2);
            options.colorType = info.getAttributeInt(AvindexContentInfo.TAG_COLOR_TYPE,1);
            options.outContentInfo = info;

            closeOptimizedImage();
            image = OptimizedImageFactory.decodeImage(data,options);
            imageView.setOptimizedImage(image);
        }
    }



    private void closeOptimizedImage() {
        if (image != null) {
            image.release();
            image = null;
        }
    }

    private void closeImageInfo() {
        if (info != null && !info.isRecycled())
        {
            info.recycle();
            info = null;
        }
    }

    private void logCursor(Cursor cursor)
    {
        String name;
        String value;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.getPosition() == -1)
                cursor.moveToFirst();
            int columnCount = cursor.getColumnCount();
            String out = "";
            for (int i = 0; i < columnCount; i++) {
                name = cursor.getColumnName(i);
                value = cursor.getString(cursor.getColumnIndexOrThrow(cursor.getColumnName(i)));

                out += " " +name + " " +value;

            }
            Log.d(TAG, out);
        }
        else Log.d(TAG, "Nothing to log");
    }


    public void moveX(boolean z) {
        Log.d(TAG,"Change translate. Start");
        Point point = new Point();
        if (z) {
            point.offset(20, 0);
        } else {
            point.offset(-20, 0);
        }
        OptimizedImageView.LayoutInfo layoutInfo = this.imageView.getLayoutInfo();
        if (layoutInfo != null) {
            Rect rect = layoutInfo.drawSize;
            this.imageView.translate(point, new Point(rect.height(), rect.height()), OptimizedImageView.TranslationType.TRANS_TYPE_INNER_CENTER);
            imageView.redraw();
            Log.d(TAG,"Change translate. End");
        }
    }

    public void moveY(boolean z) {
        Log.d(TAG,"Change translate. Start");
        Point point = new Point();
        if (z) {
            point.offset(0, -20);
        } else {
            point.offset(0, 20);
        }
        OptimizedImageView.LayoutInfo layoutInfo = this.imageView.getLayoutInfo();
        if (layoutInfo != null) {
            Rect rect = layoutInfo.drawSize;
            this.imageView.translate(point, new Point(rect.width(), rect.height()), OptimizedImageView.TranslationType.TRANS_TYPE_INNER_CENTER);
            imageView.redraw();
            Log.d(TAG,"Change translate. End");
        }
    }


    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        if (zoomEnabled) {
            if (value > 0) {
                scaleFactor += scaleStep;
                if (scaleFactor > maxScaleFactor)
                    scaleFactor = maxScaleFactor;

            } else {
                scaleFactor -= scaleStep;
                if (scaleFactor < 1)
                    scaleFactor = 1;
            }
            OptimizedImageView.LayoutInfo info = imageView.getLayoutInfo();
            if (info != null) {
                Point mTranslateDenom = new Point(info.imageSize.width(), info.imageSize.height());
                Point mTranslate = new Point(info.clipSize.centerX(), info.clipSize.centerY());

                imageView.setScale(scaleFactor, OptimizedImageView.BoundType.BOUND_TYPE_LONG_EDGE);
                info = imageView.getLayoutInfo();

                final int width2 = info.imageSize.width();
                final int height = info.imageSize.height();
                final Point point = new Point(info.clipSize.centerX() - width2 * mTranslate.x / mTranslateDenom.x, info.clipSize.centerY() - height * mTranslate.y / mTranslateDenom.y);
                imageView.translate(point, new Point(width2, height), OptimizedImageView.TranslationType.TRANS_TYPE_INNER_CENTER);
                imageView.redraw();
            }
        }
        else
        {
            if (value < 0)
            {
                activityInterface.getAvIndexManager().moveToPrevious();
            }
            else
                activityInterface.getAvIndexManager().moveToNext();
            loadOptimizedImg();
        }
        return false;
    }

    private void toggleZoom()
    {
        if (zoomEnabled)
        {
            zoomEnabled = false;
            zoomImageView.setVisibility(GONE);
        }
        else
        {
            zoomEnabled = true;
            zoomImageView.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onUpKeyDown() {
        return false;
    }

    @Override
    public boolean onUpKeyUp()
    {
        if (zoomEnabled)
        {
            moveY(false);
        }
        return false;
    }

    @Override
    public boolean onDownKeyDown() {
        return false;
    }

    @Override
    public boolean onDownKeyUp() {
        if (zoomEnabled)
        {
            moveY(true);
        }
        return false;
    }

    @Override
    public boolean onLeftKeyDown() {
        return false;
    }

    @Override
    public boolean onLeftKeyUp() {
        if (zoomEnabled)
        {
            moveX(true);
        }
        else {
            activityInterface.getAvIndexManager().moveToPrevious();
            loadOptimizedImg();
        }
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
        if (zoomEnabled)
        {
            moveX(false);
        }
        else {
            activityInterface.getAvIndexManager().moveToNext();
            loadOptimizedImg();
        }
        return false;
    }

    @Override
    public boolean onEnterKeyDown() {
        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
        toggleZoom();
        return false;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        return false;
    }

    @Override
    public boolean onAelKeyDown() {
        return false;
    }

    @Override
    public boolean onAelKeyUp() {
        return false;
    }

    @Override
    public boolean onMenuKeyDown() {
        return false;
    }

    @Override
    public boolean onMenuKeyUp() {
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        return false;
    }

    @Override
    public boolean onShutterKeyDown() {
        return false;
    }

    @Override
    public boolean onShutterKeyUp() {
        return false;
    }

    @Override
    public boolean onPlayKeyDown() {
        return false;
    }

    @Override
    public boolean onPlayKeyUp() {
        activityInterface.loadFragment(MainActivity.FRAGMENT_WAIT_FOR_CAMERA_RDY);
        return false;
    }

    @Override
    public boolean onMovieKeyDown() {
        return false;
    }

    @Override
    public boolean onMovieKeyUp() {
        return false;
    }

    @Override
    public boolean onC1KeyDown() {
        return false;
    }

    @Override
    public boolean onC1KeyUp() {
        return false;
    }

    @Override
    public boolean onLensAttached() {
        return false;
    }

    @Override
    public boolean onLensDetached() {
        return false;
    }

    @Override
    public boolean onModeDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onZoomTeleKey() {
        return false;
    }

    @Override
    public boolean onZoomWideKey() {
        return false;
    }

    @Override
    public boolean onZoomOffKey() {
        return false;
    }

    @Override
    public boolean onDeleteKeyDown() {
        return false;
    }

    @Override
    public boolean onDeleteKeyUp() {
        return false;
    }
}
