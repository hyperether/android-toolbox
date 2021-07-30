package com.hyperether.toolbox.storage;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import com.hyperether.toolbox.HyperApp;
import com.hyperether.toolbox.HyperLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;

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
     * Does not work for Android N and above
     *
     * @param context context
     * @param uri     uri
     * @return path
     * @throws URISyntaxException uri syntax exception
     */
    @SuppressLint("NewApi")
    public static String getFilePathFromUri(Context context, Uri uri, File destinationDir) {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // isDocumentUri is always content Uri
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context, uri)) {
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
            return getContentUriFileCopyForAndroidN(context, uri, destinationDir);

            /* This approach is deprecated from Android N
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
            */
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    /**
     * This method reads file using Content resolver and Input stream
     *
     * @param context  app context
     * @param uri      file uri
     * @param fileName required file name
     * @param dir      folder that will contain this file
     * @return file object
     */
    public static File getFile(Context context,
                               Uri uri,
                               String fileName,
                               File dir) {
        File file = null;
        try {
            InputStream inputStream = null;
            try {
                ContentResolver contentResolver = context.getContentResolver();
                if (contentResolver != null)
                    inputStream = contentResolver.openInputStream(uri);
                if (inputStream != null) {
                    file = new File(dir, fileName);
                    final OutputStream output = new FileOutputStream(file);
                    try {
                        try {
                            final byte[] buffer = new byte[1024];
                            int read;

                            while ((read = inputStream.read(buffer)) != -1)
                                output.write(buffer, 0, read);

                            output.flush();
                        } finally {
                            output.close();
                        }
                    } catch (Exception e) {
                        HyperLog.getInstance().e(TAG, "getFile", e);
                    }
                }
            } finally {
                if (inputStream != null)
                    inputStream.close();
            }
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "getFile", e);
        }

        return file;
    }

    /**
     * @param context        context
     * @param uri            "content" uri
     * @param destinationDir destination dir for file that will provde "file" uri
     * @return file path
     */
    public static String getContentUriFileCopyForAndroidN(Context context,
                                                          Uri uri,
                                                          File destinationDir) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(destinationDir, name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "deleteFile", e);
        }

        returnCursor.close();
        return file.getPath();
    }

    /**
     * Method makes a copy of URI into desired directory and returns path
     *
     * @param context
     * @param uri
     * @param destinationDir
     * @return
     */
    public static String getFileCopyPath(Context context,
                                         Uri uri,
                                         File destinationDir) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            try {
                Cursor cursor = null;
                String path = null;
                ContentResolver contentResolver = context.getContentResolver();
                if (contentResolver != null)
                    cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(column_index);
                    }
                    cursor.close();
                }
                File sourceFile = new File(path);
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs();
                }
                File destinationFile = new File(destinationDir, sourceFile.getName());
                FileChannel source = new FileInputStream(sourceFile).getChannel();
                FileChannel dest = new FileOutputStream(destinationFile).getChannel();
                dest.transferFrom(source, 0, source.size());
                source.close();
                dest.close();
                return destinationFile.getAbsolutePath();
            } catch (Exception e) {
                HyperLog.getInstance().e(TAG, "deleteFile", e);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                HyperLog.getInstance().e(TAG, "deleteFile", e);
            }
        }
    }

    /*
     *  Convert txt files to String
     */
    public static String readTxtFile(String file) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
        } catch (IOException e) {
            HyperLog.getInstance().e(TAG, "readTxtFile", e);
        }
        return stringBuilder.toString();
    }

    /**
     * Is External Storage Document
     *
     * @param uri uri
     * @return boolean
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Is Downloads Document
     *
     * @param uri uri
     * @return boolean
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Is Media Document
     *
     * @param uri uri
     * @return boolean
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
