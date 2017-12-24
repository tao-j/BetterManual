package com.obsidium.bettermanual;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

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
    private final String TAG = AvIndexManager.class.getSimpleName();

    private ContentResolver contentResolver;
    private Cursor cursor;
    private final Uri mediaStorageUri;

    final String[] GROUP_QUERY_PROJECTION = new String[] { "_id", "_count", "count_of_one_before", "dcf_folder_number" };
    final String[] QUERY_PROJECTION = new String[] { "_id", "_data", "_display_name", "datetaken" };
    protected static final String[] CONTENTS_QUERY_PROJECTION = { "_id", "_data", "dcf_file_number", "dcf_folder_number", "content_created_local_date", "content_created_utc_date", "content_created_local_date_time", "content_created_utc_date_time", "exist_jpeg", "exist_raw", "exist_mpo", "rec_order", "content_type" };
    //final Uri baseUri = AvindexStore.Images.Media.EXTERNAL_CONTENT_URI;

    public IntentFilter MEDIA_INTENTS = new IntentFilter();
    public IntentFilter AVAILABLE_SIZE_INTENTS = new IntentFilter("com.sony.scalar.providers.avindex.action.AVINDEX_MEDIA_AVAILABLE_SIZE_CHANGED");

    public AvIndexManager(ContentResolver contentResolver)
    {
        this.contentResolver = contentResolver;
        mediaStorageUri = AvindexStore.Images.Media.getContentUri(AvindexStore.getExternalMediaIds()[0]);
    }

    public void onResume(Context context)
    {
        update();
    }

    private Cursor getCursorFromUri(Uri uri)
    {
        return contentResolver.query(uri, CONTENTS_QUERY_PROJECTION, null, null,"content_created_utc_date_time DESC");
    }

    public void onPause(Context context)
    {
        if (cursor != null && cursor.isClosed())
            cursor.close();
    }

    public String getData()
    {
        return cursor.getString(cursor.getColumnIndexOrThrow("_data"));
    }

    public boolean existsJpeg()
    {
        if (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("exist_jpeg")))> 0)
            return true;
        else
            return false;
    }

    public boolean existsRaw()
    {
        if (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("exist_raw")))> 0)
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
        /*
        I/_jniOsalUtil_: SendObjMsg OSAL send. srcId = 0x15e6119, osalErr = 0x0, msgId(android) = 0x1100b, msgId(osal) = 0x1100b
        I/_jniOsalUtil_: RecvObjMsg start OSAL Receive waiting...
        I/_jniOsalUtil_: RecvObjMsg messageId = 0x1100c, srcId = 0x15e6119, osalErr = OK
        I/_InfraScalarMprWrapper_: InfraScalarMprWrapper::waitAndUpdateDatabase# return ret = true
         */
        //AvindexStore.Images.waitAndUpdateDatabase(contentResolver, AvindexStore.getExternalMediaIds()[0]);

        /*
            I/_jniOsalUtil_: SendObjMsg OSAL send. srcId = 0x0, osalErr = 0x0, msgId(android) = 0x1100d, msgId(osal) = 0x1100d
            I/_InfraScalarMprWrapper_: InfraScalarMprWrapper::cancelWaitAndUpdateDatabase# return ret = true
         */
        //AvindexStore.Images.cancelWaitAndUpdateDatabase(contentResolver, AvindexStore.getExternalMediaIds()[0]);
       /* AvindexDatabaseManager avindexDatabaseManager = AvindexDatabaseManager.GET();
        AvindexDatabase[] avindexDatabases = avindexDatabaseManager.getAllDatabase();
        for (AvindexDatabase database : avindexDatabases)
            database.updateDatabase();

        if (cursor != null && cursor.isClosed())
            cursor.close();
        cursor = getCursorFromUri(mediaStorageUri);*/
        /*AvindexStore.Images.waitAndUpdateDatabase(contentResolver, AvindexStore.getExternalMediaIds()[0]);
        AvindexStore.loadMedia(AvindexStore.getExternalMediaIds()[0], 1);
        AvindexStore.Images.waitAndUpdateDatabase(contentResolver, AvindexStore.getExternalMediaIds()[0]);
        AvindexStore.waitLoadMediaComplete(AvindexStore.getExternalMediaIds()[0]);
        AvindexStore.cancelWaitLoadMediaComplete(AvindexStore.getExternalMediaIds()[0]);
        MediaInfo info = AvindexStore.getMediaInfo(AvindexStore.getExternalMediaIds()[0]);
        String state = android.os.Environment.getExternalStorageState();
        int remaining = AvindexStore.Images.getAvailableCount(AvindexStore.getExternalMediaIds()[0]);*/
        cursor = getCursorFromUri(mediaStorageUri);
        cursor.moveToFirst();
    }

    public void moveToNext()
    {
        cursor.moveToNext();
        if(cursor.isAfterLast())
            cursor.moveToFirst();
    }

    public void moveToPrevious()
    {
        cursor.moveToPrevious();
        if (cursor.isBeforeFirst())
            cursor.moveToLast();
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

}
