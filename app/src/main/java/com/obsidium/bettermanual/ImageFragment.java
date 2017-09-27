package com.obsidium.bettermanual;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;

import com.sony.scalar.graphics.OptimizedImage;
import com.sony.scalar.graphics.OptimizedImageFactory;
import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.media.AvindexContentInfo;
import com.sony.scalar.provider.AvindexStore;
import com.sony.scalar.widget.OptimizedImageView;


public class ImageFragment extends BaseLayout implements KeyEvents {

    /*
        FOLDER
        _id 0 _data null dcf_folder_number 100 dcf_file_number null content_type null exist_jpeg null exist_mpo null exist_raw null content_created_local_date_time null content_created_local_date null content_created_local_time null content_created_utc_date_time null content_created_utc_date null content_created_utc_time null has_gps_info null time_zone null latitude null longitude null rec_order null
        LAST_CONTENT
        Nothing to log
        MEDIA
        _id 1 _data avindex://1000/00000001-default/00000001-00000925 dcf_folder_number 100 dcf_file_number 1663 content_type 1 exist_jpeg 1 exist_mpo 0 exist_raw 1 content_created_local_date_time 1507114550000 content_created_local_date 20171004 content_created_local_time 105550 content_created_utc_date_time 1507110950000 content_created_utc_date 20171004 content_created_utc_time 095550 has_gps_info 0 time_zone 60 latitude 0 longitude 0 rec_order 1
        INFO
        avi_version 45cd5a8d03e44b57b963dc9e781b722e
        THUMB
        Nothing to log
     */

    private final String TAG = ImageFragment.class.getSimpleName();
    private OptimizedImageView imageView;
    private FrameLayout surfaceViewParent;
    private Cursor mediaCursor;
    OptimizedImage image;
    AvindexContentInfo info;
    private float scaleFactor = 1;
    private final float scaleStep = 0.2f;
    private final float maxScaleFactor = 8;

    final String[] GROUP_QUERY_PROJECTION = new String[] { "_id", "_count", "count_of_one_before", "dcf_folder_number" };
    final String[] QUERY_PROJECTION = new String[] { "_id", "_data", "_display_name", "datetaken" };
    protected static final String[] CONTENTS_QUERY_PROJECTION = { "_id", "_data", "dcf_file_number", "dcf_folder_number", "content_created_local_date", "content_created_utc_date", "content_created_local_date_time", "content_created_utc_date_time", "exist_jpeg", "exist_raw", "exist_mpo", "rec_order", "content_type" };
    final Uri baseUri = AvindexStore.Images.Media.EXTERNAL_CONTENT_URI;

    public ImageFragment(Context context,ActivityInterface activityInterface) {
        super(context,activityInterface);
        inflateLayout(R.layout.image_fragment);

        surfaceViewParent = (FrameLayout) findViewById(R.id.surfaceParentView);
        imageView = new OptimizedImageView(getContext());
        surfaceViewParent.addView(imageView);
        imageView.setDisplayPosition(new Point(0,0), OptimizedImageView.PositionType.POS_TYPE_NONE);



        //stop camera else Images are not load


        /*AvindexStore.loadMedia(AvindexStore.getExternalMediaIds()[0], AvindexStore.CONTENT_TYPE_LOAD_STILL);
        AvindexStore.Images.waitAndUpdateDatabase(getContext().getContentResolver(), AvindexStore.getExternalMediaIds()[0]);*/



        Uri media = AvindexStore.Images.Media.getContentUri(AvindexStore.getExternalMediaIds()[0]);
        mediaCursor = getCursorFromUri(media);
        mediaCursor.moveToFirst();
        loadOptimizedImg();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();


    }

    @Override
    public void Destroy() {
        if (mediaCursor != null && !mediaCursor.isClosed())
            mediaCursor.close();
        closeImageInfo();
        closeOptimizedImage();

    }

    private void loadOptimizedImg()
    {
        if (mediaCursor.getPosition() > -1 && mediaCursor.getCount() > 0)
        {
            Log.d(TAG,"MEDIA");

            logCursor(mediaCursor);
            String data = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow("_data"));
            String id = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow("_id"));

            closeImageInfo();

            info = AvindexStore.Images.Media.getImageInfo(id);

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

    private Cursor getCursorFromUri(Uri uri)
    {
        return getContext().getContentResolver().query(uri, AvindexStore.Images.Media.ALL_COLUMNS, null, null, null);
    }


    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        if (value > 0) {
            scaleFactor += scaleStep;
            if (scaleFactor > maxScaleFactor)
                scaleFactor = maxScaleFactor;

        }
        else {
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
        return false;
    }

    @Override
    public boolean onUpKeyDown() {
        return false;
    }

    @Override
    public boolean onUpKeyUp() {
        return false;
    }

    @Override
    public boolean onDownKeyDown() {
        return false;
    }

    @Override
    public boolean onDownKeyUp() {
        return false;
    }

    @Override
    public boolean onLeftKeyDown() {
        return false;
    }

    @Override
    public boolean onLeftKeyUp() {
        mediaCursor.moveToPrevious();
        if(mediaCursor.isBeforeFirst())
            mediaCursor.moveToLast();
        loadOptimizedImg();
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
        mediaCursor.moveToNext();
        if(mediaCursor.isAfterLast())
            mediaCursor.moveToFirst();
        loadOptimizedImg();
        return false;
    }

    @Override
    public boolean onEnterKeyDown() {
        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
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
        activityInterface.loadFragment(MainActivity.FRAGMENT_WAITFORCAMERARDY);
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
