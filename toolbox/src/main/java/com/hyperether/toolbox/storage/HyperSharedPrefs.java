package com.hyperether.toolbox.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.hyperether.toolbox.HyperApp;
import com.hyperether.toolbox.HyperLog;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * SharedPrefs storage
 *
 * @author Marko Katic
 * @version 1.0 - 28/11/2018.
 */

public class HyperSharedPrefs {

    private static final String TAG = HyperSharedPrefs.class.getSimpleName();
    private static final String PREFS_KEY_CURRENT_USER = "currentUser";

    public static void clear(String prefName) {
        SharedPreferences prefs = getUserSharedPrefs(prefName);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * save String
     *
     * @param map map
     * @param prefName Shared preference name
     */
    public static void savePrefString(HashMap<String, String> map, String prefName) {
        try {
            SharedPreferences prefs = getUserSharedPrefs(prefName);
            SharedPreferences.Editor editor = prefs.edit();
            for (String key : map.keySet()) {
                editor.putString(key, map.get(key));
            }
            editor.apply();
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "savePrefString", e);
        }
    }

    /**
     * save Long
     *
     * @param map map
     * @param prefName Shared preference name
     */
    public static void savePrefLong(HashMap<String, Long> map, String prefName) {
        try {
            SharedPreferences prefs = getUserSharedPrefs(prefName);
            SharedPreferences.Editor editor = prefs.edit();
            for (String key : map.keySet()) {
                editor.putLong(key, map.get(key));
            }
            editor.apply();
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "savePrefString", e);
        }
    }

    /**
     * save Boolean
     *
     * @param map map
     * @param prefName Shared preference name
     */
    public static void savePrefBoolean(HashMap<String, Boolean> map, String prefName) {
        try {
            SharedPreferences prefs = getUserSharedPrefs(prefName);
            SharedPreferences.Editor editor = prefs.edit();
            for (String key : map.keySet()) {
                editor.putBoolean(key, map.get(key));
            }
            editor.apply();
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "savePrefString", e);
        }
    }

    public static void savePrefInt(HashMap<String, Integer> map, String prefName) {
        try {
            SharedPreferences prefs = getUserSharedPrefs(prefName);
            SharedPreferences.Editor editor = prefs.edit();
            for (String key : map.keySet()) {
                editor.putInt(key, map.get(key));
            }
            editor.apply();
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "savePrefString", e);
        }
    }

    public static int getPrefInt(String key, String prefName) {
        try {
            SharedPreferences prefs = getUserSharedPrefs(prefName);
            return prefs.getInt(key, -1);
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "getPrefString", e);
        }
        return -1;
    }

    /**
     * get Long
     *
     * @param key map
     * @param prefName Shared preference name
     */
    public static Long getPrefLong(String key, String prefName) {
        try {
            SharedPreferences prefs = getUserSharedPrefs(prefName);
            return prefs.getLong(key, 0);
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "getPrefString", e);
        }
        return 0L;
    }

    /**
     * get String
     *
     * @param key map
     * @param prefName Shared preference name
     */
    public static String getPrefString(String key, String prefName) {
        try {
            SharedPreferences prefs = getUserSharedPrefs(prefName);
            return prefs.getString(key, "");
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "getPrefString", e);
        }
        return "";
    }

    /**
     * get Boolean
     *
     * @param key map
     * @param prefName Shared preference name
     */
    public static Boolean getPrefBoolean(String key, String prefName) {
        try {
            SharedPreferences prefs = getUserSharedPrefs(prefName);
            return prefs.getBoolean(key, false);
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "getPrefString", e);
        }
        return false;
    }

    /**
     * getSharedPrefs for User
     *
     * @param prefName prefName
     *
     * @return shared preference
     */
    private static SharedPreferences getUserSharedPrefs(String prefName) {
        String username = "";
        try {
            username = getCurrentUser();
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "getUserSharedPrefs", e);
        }
        Context context = HyperApp.getInstance().getApplicationContext();
        return context.getSharedPreferences(username + prefName, MODE_PRIVATE);
    }

    /**
     * getCurrentUser
     *
     * @return user
     */
    private static String getCurrentUser() {
        try {
            Context context = HyperApp.getInstance().getApplicationContext();
            SharedPreferences prefs = context
                    .getSharedPreferences(PREFS_KEY_CURRENT_USER, Context.MODE_PRIVATE);
            return prefs.getString(PREFS_KEY_CURRENT_USER, "");
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "getCurrentUser", e);
        }
        return "";
    }
}

