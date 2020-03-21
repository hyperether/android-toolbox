package com.hyperether.toolbox;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * HyperLog.java
 * Android toolbox class for error logging. If user needs to send error to server,
 * should provide OnLogListener listener
 *
 * Created by Slobodan on 12/09/2017
 * Version 1.1 - 21.03.2020. - added listener
 */

public class HyperLog {

    private static final String TAG = HyperLog.class.getSimpleName();

    private static final int ERROR = 1;
    private static final int WARN = 2;
    private static final int DEBUG = 3;
    private static final int VERBOSE = 4;
    private static final int INFO = 5;

    private static HyperLog instance = null;
    private OnLogListener onLogListener;

    private HyperLog() {
    }

    public static HyperLog getInstance() {
        if (instance == null) {
            instance = new HyperLog();
        }
        return instance;
    }

    public void d(String tag, String method, String msg) {
        add(DEBUG, tag, method, msg);
    }

    public void e(String tag, String method, String msg) {
        add(ERROR, tag, method, msg);
    }

    public void e(String tag, String method, Exception msg) {
        add(ERROR, tag, method, msg);
    }

    public void i(String tag, String method, String msg) {
        add(INFO, tag, method, msg);
    }

    public void v(String tag, String method, String msg) {
        add(VERBOSE, tag, method, msg);
    }

    public void w(String tag, String method, String msg) {
        add(WARN, tag, method, msg);
    }

    public void setOnLogListener(OnLogListener onLogListener) {
        this.onLogListener = onLogListener;
    }

    private void add(int level, String tag, String method, Throwable ex) {
        String log = "";

        if (ex != null && ex.getMessage() != null)
            log = ex.getMessage();
        else if (ex != null && ex.toString() != null)
            log = ex.toString();

        add(level, tag, method, log);
    }

    private void add(int level, String tag, String methodName, String log) {
        if (log == null)
            return;

        String sLevel = "DEBUG";
        String color = "#ffffff";
        String logTag = tag + "." + methodName;
        switch (level) {
            case ERROR:
                forwardLog(level, "Class: " + tag + " Method: " +
                        methodName + " msg: " + log);
                Log.e(logTag, log);
                color = "#ffb3b4";
                sLevel = "ERROR";
                break;
            case WARN:
                forwardLog(level, "Class: " + tag + " Method: " +
                        methodName + " msg: " + log);
                if (HyperApp.getInstance().isDebugActive())
                    Log.w(logTag, log);
                color = "#ffffb4";
                sLevel = "WARN";
                break;
            case DEBUG:
                if (HyperApp.getInstance().isDebugActive())
                    Log.d(logTag, log);
                color = "#ffccfe";
                sLevel = "DEBUG";
                break;
            case VERBOSE:
                if (HyperApp.getInstance().isDebugActive())
                    Log.v(logTag, log);
                sLevel = "VERBOSE";
                color = "#b3ffb4";
                break;
            case INFO:
                forwardLog(level, "Class: " + tag + " Method: " +
                        methodName + " msg: " + log);
                if (HyperApp.getInstance().isDebugActive())
                    Log.i(logTag, log);
                color = "#b3b3fe";
                sLevel = "INFO";
                break;
        }

        try {
            if (HyperApp.getInstance().isDebugActive())
                saveToFile(sLevel, tag, methodName, log, color);
        } catch (IOException e) {
            if (HyperApp.getInstance().isDebugActive()) {
                e.printStackTrace();
            }
        }
    }

    private void forwardLog(int level, String msg) {
        String msgType = " ";
        switch (level) {
            case ERROR:
                msgType = " ERROR ";
                break;
            case WARN:
                msgType = " WARN ";
                break;
            case DEBUG:
                msgType = " DEBUG ";
                break;
            case VERBOSE:
                msgType = " VERBOSE ";
                break;
            case INFO:
                msgType = " INFO ";
                break;
        }

        String formattedMsg = "DEVICE: " + Build.MODEL + msgType + "MESSAGE: " + msg;
        if (onLogListener != null) {
            onLogListener.onLog(formattedMsg);
        }
    }

    private void saveToFile(String sLevel,
                            String tag,
                            String method,
                            String log,
                            String color) throws IOException {

        StringBuilder htmlStringBuilder = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS", Locale.getDefault());
        Date resultdate = new Date(System.currentTimeMillis());
        String date = sdf.format(resultdate);

        Context context = HyperApp.getInstance().getApplicationContext();
        File logFile = new File(context.getExternalFilesDir(null), "log.html");
        if (!logFile.exists()) {
            htmlStringBuilder.append(htmlTemplate());
        } else if (logFile.length() > 10485760) {
            boolean del = logFile.delete();
            Log.i(TAG, "saveToFile: del:" + del);
            htmlStringBuilder.append(htmlTemplate());
        }
        String c = "<tr BGCOLOR=\"" + color + "\">";
        String d = "<td>" + date + "</td>";
        String lev = "<td>" + sLevel + "</td>";
        String t = "<td>" + tag + "</td>";
        String m = "<td>" + method + "</td>";
        String l = "<td>" + log + "</td></tr>";

        htmlStringBuilder.append(c);
        htmlStringBuilder.append(d);
        htmlStringBuilder.append(lev);
        htmlStringBuilder.append(t);
        htmlStringBuilder.append(m);
        htmlStringBuilder.append(l);

        BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
        bw.append(htmlStringBuilder.toString());
        bw.newLine();
        bw.close();
    }

    private StringBuilder htmlTemplate() {
        StringBuilder htmlStringBuilder = new StringBuilder();
        htmlStringBuilder.append("<html><head><title>Hyper logs</title></head>");
        htmlStringBuilder.append("<body><h1>Logs</h1>");
        htmlStringBuilder.append("<table border=\"1\" bordercolor=\"#000000\">");
        htmlStringBuilder.append("<tr BGCOLOR=\"#d9d9d9\"><td><b>Date</b></td>");
        htmlStringBuilder.append("<td><b>Level</b></td>");
        htmlStringBuilder.append("<td><b>Tag</b></td>");
        htmlStringBuilder.append("<td><b>Method</b></td>");
        htmlStringBuilder.append("<td><b>Message</b></td></tr>");
        return htmlStringBuilder;
    }

    public interface OnLogListener {
        void onLog(String msg);
    }
}
