package com.hyperether.toolbox.streaming;

import android.webkit.MimeTypeMap;

import com.hyperether.toolbox.HyperLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Hyper Upload Task class for uploading file to the corresponding url address
 *
 * @author Marko Katic
 * @version 1.0 - 20/02/2018.
 */

public class HyperUploadTask {

    private static final String TAG = HyperUploadTask.class.getSimpleName();

    /**
     * Upload File To S3
     * @param address Pre-signed URL to upload
     * @param filePath File Path
     * @param requestProperty Request Property (e.g. "Content-Type")
     * @param requestMethod Request Method (e.g. "PUT")
     */
    public void startUpload(String address, String filePath,
                            String requestProperty, String requestMethod) {

        HttpURLConnection connection;
        try {
            File file = new File(filePath);
            URL url = new URL(address);
            String extension = filePath.substring(filePath.lastIndexOf('.') + 1);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            InputStream inputStream = new FileInputStream(file);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty(requestProperty, mimeType);
            connection.setDoOutput(true);
            connection.setRequestMethod(requestMethod);
            OutputStream out = connection.getOutputStream();

            byte[] buf = new byte[1024];
            int count;
            int total = 0;
            long fileSize = file.length();

            while ((count = inputStream.read(buf)) != -1) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                out.write(buf, 0, count);
                total += count;
                int pctComplete = Double.valueOf((double) total / (double) fileSize * 100)
                        .intValue();
            }
            out.close();
            inputStream.close();

            int responseCode = connection.getResponseCode();

        } catch (FileNotFoundException e) {
            HyperLog.getInstance().e(TAG, "startUpload", e);
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "startUpload", e);
        } catch (OutOfMemoryError e) {
            HyperLog.getInstance().e(TAG, "startUpload", e.getMessage());
        }
    }
}
