package com.github.killerink;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.obsidium.bettermanual.R;
import com.sony.scalar.graphics.JpegExporter;
import com.sony.scalar.graphics.OptimizedImage;
import com.sony.scalar.graphics.OptimizedImageFactory;
import com.sony.scalar.hardware.avio.DisplayManager;
import com.sony.scalar.media.AvindexContentInfo;
import com.sony.scalar.media.AvindexFactory;
import com.sony.scalar.meta.Histogram;
import com.sony.scalar.provider.AvindexStore;
import com.sony.scalar.widget.OptimizedImageView;

import java.io.File;
import java.io.InputStream;


public class ImageFragment extends Fragment implements KeyEvents {

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
    private ActivityInterface activityInterface;
    private Cursor mediaCursor;
    OptimizedImage image;
    AvindexContentInfo info;

    final String[] GROUP_QUERY_PROJECTION = new String[] { "_id", "_count", "count_of_one_before", "dcf_folder_number" };
    final String[] QUERY_PROJECTION = new String[] { "_id", "_data", "_display_name", "datetaken" };
    protected static final String[] CONTENTS_QUERY_PROJECTION = { "_id", "_data", "dcf_file_number", "dcf_folder_number", "content_created_local_date", "content_created_utc_date", "content_created_local_date_time", "content_created_utc_date_time", "exist_jpeg", "exist_raw", "exist_mpo", "rec_order", "content_type" };
    final Uri baseUri = AvindexStore.Images.Media.EXTERNAL_CONTENT_URI;

    public static ImageFragment getFragment(ActivityInterface activityInterface)
    {
        ImageFragment fragment = new ImageFragment();
        fragment.activityInterface = activityInterface;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //NOTE: to use fragments its important to not attachToRoot!
        return inflater.inflate(R.layout.image_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = (OptimizedImageView) view.findViewById(R.id.touchimageview);

        imageView.setOnDisplayEventListener(new OptimizedImageView.onDisplayEventListener() {
            @Override
            public void onDisplay(int i) {
                Log.d(TAG,"onDisplay " + i);
            }
        });

        imageView.setOnLayoutChangeListener(new OptimizedImageView.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                Log.d(TAG, "onLayoutChange");
            }
        });

        imageView.setOnHistogramEventListener(new OptimizedImageView.onHistogramEventListener() {
            @Override
            public void onHistogram(int i, Histogram histogram) {
                Log.d(TAG, "onHistogram");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        /*activityInterface.getCamera().stopPreview();
        activityInterface.getCamera().stopDisplay();*/
        activityInterface.getCamera().closeCamera();

        AvindexStore.loadMedia(AvindexStore.getExternalMediaIds()[0], AvindexStore.CONTENT_TYPE_LOAD_STILL);
        AvindexStore.Images.waitAndUpdateDatabase(getContext().getContentResolver(), AvindexStore.getExternalMediaIds()[0]);

        Uri media = AvindexStore.Images.Media.getContentUri(AvindexStore.getExternalMediaIds()[0]);
        mediaCursor = getCursorFromUri(media);
        mediaCursor.moveToFirst();
        loadOptimizedImg();
        //loadImage();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaCursor != null && !mediaCursor.isClosed())
            mediaCursor.close();
        closeImageInfo();
        closeOptimizedImage();
        activityInterface.getCamera().startCamera();
        /*activityInterface.getCamera().startPreview();
        activityInterface.getCamera().startDisplay();*/
    }

    private void loadOptimizedImg()
    {
        /*String[] externalMediaIds = AvindexStore.getExternalMediaIds(); // a6000 = 1000
        Log.d(TAG,"FOLDER");
        Uri folder= AvindexStore.Images.Folder.getContentUri(AvindexStore.Images.Media.EXTERNAL_DEFAULT_MEDIA_ID);
        mediaCursor = getCursorFromUri(folder);
        logCursor(mediaCursor);
        mediaCursor.close();

        Log.d(TAG,"LAST_CONTENT");
        Uri lastContent = AvindexStore.Images.LastContent.getContentUri(AvindexStore.Images.Media.EXTERNAL_DEFAULT_MEDIA_ID);
        mediaCursor = getCursorFromUri(lastContent);
        logCursor(mediaCursor);
        mediaCursor.close();*/

        Log.d(TAG,"MEDIA");

        logCursor(mediaCursor);
        String data = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow("_data"));
        String id = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow("_id"));


        /*Log.d(TAG,"INFO");
        Uri infoUri = AvindexStore.Images.Info.getContentUri(AvindexStore.Images.Media.EXTERNAL_DEFAULT_MEDIA_ID);
        mediaCursor = getCursorFromUri(infoUri);
        logCursor(mediaCursor);
        mediaCursor.close();

        Log.d(TAG,"THUMB");
        Uri thumburi = AvindexStore.Images.Thumbnails.getContentUri(AvindexStore.Images.Media.EXTERNAL_DEFAULT_MEDIA_ID);
        mediaCursor = getCursorFromUri(thumburi);
        logCursor(mediaCursor);
        if (mediaCursor != null)
            mediaCursor.close();*/


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

    private String getStringFromCollumn(String column, Cursor cursor)
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
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

    private Cursor queryLastContent() {
        Cursor contentFocusPoint = AvindexStore.Images.Media.getContentFocusPoint(getContext().getContentResolver(), AvindexStore.Images.Media.getContentUri(AvindexStore.Images.Media.EXTERNAL_DEFAULT_MEDIA_ID));
        if (contentFocusPoint != null && !contentFocusPoint.moveToFirst()) {
            contentFocusPoint.close();
            contentFocusPoint = null;
        }
        return contentFocusPoint;
    }

    public String getImageId(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow("_id"));
    }


    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        if (value > 0) {
            mediaCursor.moveToNext();
            if(mediaCursor.isAfterLast())
                mediaCursor.moveToFirst();
        }
        else {
            mediaCursor.moveToPrevious();
            if(mediaCursor.isBeforeFirst())
                mediaCursor.moveToLast();
        }
        loadOptimizedImg();
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
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
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
