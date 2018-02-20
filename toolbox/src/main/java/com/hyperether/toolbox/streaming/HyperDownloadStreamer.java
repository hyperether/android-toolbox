package com.hyperether.toolbox.streaming;

import com.hyperether.toolbox.HyperLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Create stream from url for downloading file
 *
 * @author Marko Katic
 * @version 1.0 - 20/02/2018.
 */

public class HyperDownloadStreamer {

    private static final String TAG = HyperDownloadStreamer.class.getSimpleName();

    /**
     * Get Input Stream
     *
     * @param urlString url
     * @return input stream
     * @throws IOException exception
     */
    public static InputStream getInputStream(String urlString) throws IOException {

        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        } catch (Exception ex) {
            HyperLog.getInstance().e(TAG, "getInputStream", ex);
        }
        return stream;
    }
}
