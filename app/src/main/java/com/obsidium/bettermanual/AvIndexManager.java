package com.obsidium.bettermanual;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.github.ma1co.openmemories.framework.ImageInfo;
import com.sony.scalar.media.AvindexContentInfo;
import com.sony.scalar.provider.AvindexStore;

/**
 * Created by KillerInk on 29.09.2017.
 */

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

public class AvIndexManager extends BroadcastReceiver
{

    public static boolean isSupported()
    {
        try
        {
            Class avindex =  Class.forName("com.sony.scalar.provider.AvindexStore");
            return true;
        }
        catch (ClassNotFoundException ex)
        {
            Log.e(TAG, ex.getMessage());
            return false;
        }
    }

    private static final String TAG = AvIndexManager.class.getSimpleName();

    private ContentResolver contentResolver;
    private Cursor cursor;
    private final Uri mediaStorageUri;
    private Context context;
    private long _id;

    public IntentFilter MEDIA_INTENTS = new IntentFilter();
    public IntentFilter AVAILABLE_SIZE_INTENTS = new IntentFilter("com.sony.scalar.providers.avindex.action.AVINDEX_MEDIA_AVAILABLE_SIZE_CHANGED");

    public AvIndexManager(ContentResolver contentResolver,Context context)
    {
        this.context = context;
        this.contentResolver = contentResolver;
        mediaStorageUri = AvindexStore.Images.Media.getContentUri(AvindexStore.getExternalMediaIds()[0]);
    }

    public void onResume(Context context)
    {
        update();
    }

    private Cursor getCursorFromUri(Uri uri)
    {
        return contentResolver.query(uri, AvindexStore.Images.Media.ALL_COLUMNS, null, null,AvindexStore.Images.ImageColumns.CONTENT_CREATED_UTC_DATE +" DESC");
    }


    public void onPause(Context context)
    {
        if (cursor != null && cursor.isClosed())
            cursor.close();
    }

    public String getData()
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(AvindexStore.Images.ImageColumns.DATA));
    }

    public String getFolder()
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(AvindexStore.Images.ImageColumns.DCF_FOLDER_NUMBER));
    }

    public String getFileName()
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(AvindexStore.Images.ImageColumns.DCF_FILE_NUMBER));
    }

    /**
     * Creates a new {@link ImageInfo} instance for the given image id
     */
    public ImageInfo getImageInfo() {
        return ImageInfo.create(context, mediaStorageUri, _id);
    }

    public boolean existsJpeg()
    {
        if (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(AvindexStore.Images.ImageColumns.EXIST_JPEG)))> 0)
            return true;
        else
            return false;
    }

    public boolean existsRaw()
    {
        if (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(AvindexStore.Images.ImageColumns.EXIST_RAW)))> 0)
            return true;
        else
            return false;
    }

    public String getId()
    {
        return cursor.getString(cursor.getColumnIndexOrThrow("_id"));
    }

    public AvindexContentInfo getContentInfo()
    {
        return AvindexStore.Images.Media.getImageInfo(getId());
    }

    public void update()
    {
        cursor = getCursorFromUri(mediaStorageUri);
        cursor.moveToFirst();
        _id = Long.parseLong(getId());
    }

    public void moveToNext()
    {
        cursor.moveToNext();
        if(cursor.isAfterLast())
            cursor.moveToFirst();
        _id = Long.parseLong(getId());
    }

    public void moveToPrevious()
    {
        cursor.moveToPrevious();
        if (cursor.isBeforeFirst())
            cursor.moveToLast();
        _id = Long.parseLong(getId());
    }

    public int getPosition()
    {
        return  cursor.getPosition();
    }

    public int getCount()
    {
        return cursor.getCount();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,intent.getAction());
        update();
    }

    public byte[] getFullImage() {
        return AvindexStore.Images.Media.getJpegImage(contentResolver, mediaStorageUri, _id);
    }

}
