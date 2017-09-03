package com.github.killerink;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.ma1co.openmemories.framework.ImageInfo;
import com.github.ma1co.openmemories.framework.MediaManager;
import com.obsidium.bettermanual.R;
import com.ortiz.touch.TouchImageView;

import java.io.InputStream;


public class ImageFragment extends Fragment implements KeyEvents {

    private final String TAG = ImageFragment.class.getSimpleName();
    private TouchImageView imageView;
    private MediaManager mediaManager;
    private ActivityInterface activityInterface;
    private Cursor cursor;
    private Bitmap bitmap;

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

        imageView = (TouchImageView)view.findViewById(R.id.touchimageview);
        mediaManager = MediaManager.create(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        cursor = mediaManager.queryNewestImages();
        cursor.moveToFirst();
        loadImage();
    }

    private void loadImage()
    {
        final TimeLog timeLog = new TimeLog("loadImage");
        if (cursor.getCount() > 0 && cursor.getPosition() > -1)
        {
            ImageInfo info = mediaManager.getImageInfo(cursor);
            Log.d(TAG,"W:" + info.getWidth() +" H:"+info.getHeight());
            InputStream inputStream = null;

            try {
                inputStream = info.getPreviewImage();
                bitmap = BitmapFactory.decodeStream(inputStream);
                Log.d(TAG,"image from info.getPreviewImage");
            }
            catch (NullPointerException ex)
            {
                Log.d(TAG,"unsupported info.getPreviewImage, load it from full....");
                inputStream = info.getFullImage();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                options.inDither = true;
                int paddingl = (info.getWidth() - (info.getWidth()/4))/2;
                int paddingt = (info.getHeight() - (info.getHeight()/4))/2;
                Rect rect = new Rect(paddingl,paddingt,paddingl,paddingt);
                Log.d(TAG,"new W:" +rect.width() +" H:"+rect.height());
                bitmap = BitmapFactory.decodeStream(inputStream,rect, options);
                Log.d(TAG, "final bitmapSize W:" +bitmap.getWidth() + " H:" +bitmap.getHeight());
            }

            imageView.setImageBitmap(bitmap);
        }
        else
            Log.d(TAG, "No Images");
        timeLog.logTime();
    }

    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        if (value > 0)
            cursor.moveToNext();
        else
            cursor.moveToPrevious();
        loadImage();
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
        activityInterface.loadFragment(MainActivity.FRAGMENT_CAMERA_UI);
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
