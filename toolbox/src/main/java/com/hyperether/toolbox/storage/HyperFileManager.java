package com.hyperether.toolbox.storage;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.hyperether.toolbox.HyperApp;
import com.hyperether.toolbox.HyperLog;

import java.io.File;
import java.net.URISyntaxException;

/**
 * HyperFileManager for manipulating with files
 *
 * @author Marko Katic
 * @version 1.0 - 19/02/2017.
 */

public class HyperFileManager {

    private static final String TAG = HyperFileManager.class.getSimpleName();

    /**
     * Get File Storage Path
     *
     * @return Absolute Path
     */
    private static String getFileStoragePath() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        File mediaStorageDir = HyperApp.getInstance().getApplicationContext()
                .getExternalFilesDir(null);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        if (mediaStorageDir != null) {
            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            return mediaStorageDir.getAbsolutePath();
        }
        return null;
    }

    /**
     * Create an get file
     *
     * @param filename filename
     *
     * @return file
     */
    public static File getFile(String filename) {
        String root = getFileStoragePath();
        if (root == null) {
            root = Environment.getExternalStorageDirectory().getPath();
        }
        File myDir;
        myDir = new File(root);
        boolean createResult = myDir.mkdirs();
        HyperLog.getInstance().d(TAG, "getFile", " dir created:" + createResult);
        return new File(myDir, filename);
    }

    /**
     * Get File Path From Uri
     *
     * @param context context
     * @param uri uri
     *
     * @return path
     *
     * @throws URISyntaxException uri syntax exception
     */
    @SuppressLint("NewApi")
    public static String getFilePathFromUri(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context
                .getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = 0;
                if (cursor != null) {
                    column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                }
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Is External Storage Document
     *
     * @param uri uri
     *
     * @return boolean
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Is Downloads Document
     *
     * @param uri uri
     *
     * @return boolean
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Is Media Document
     *
     * @param uri uri
     *
     * @return boolean
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
