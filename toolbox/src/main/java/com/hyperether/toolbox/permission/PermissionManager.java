package com.hyperether.toolbox.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hyperether.toolbox.HyperApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to manage permission requests from different app classes
 */
public class PermissionManager {

    private static final String TAG = PermissionManager.class.getSimpleName();

    // codes that require permissions in app:
    public static final int FILE_MANAGER_REQUEST_WRITE_EXTERNAL = 1;
    public static final int PERMISSIONS_REQUEST_CAMERA = 2;
    public static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 3;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 4;

    private List<PermissionRequest> permissionRequests = new ArrayList<>();

    private static PermissionManager instance;

    public static PermissionManager getInstance() {
        if (instance == null)
            instance = new PermissionManager();
        return instance;
    }

    private PermissionManager() {
    }

    public void processPermission(int requestCode,
                                  String permissions[],
                                  int[] grantResults) {

        for (int i = permissionRequests.size() - 1; i >= 0; i--) {
            PermissionRequest req = permissionRequests.get(i);
            if (requestCode == req.getCode()) {
                // If request is cancelled, the result arrays are empty.
                //if request does not have callback do not return anything but remove from list
                if (req.getOnPermissionRequestCallback() != null) {
                    if (grantResults.length > 0 &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        req.getOnPermissionRequestCallback().onGranted(requestCode);
                    } else {
                        req.getOnPermissionRequestCallback().onDenied(requestCode);
                    }
                }
                permissionRequests.remove(req);
            }
        }
    }

    public boolean getPhoneStatePermission(Activity act,
                                           int code,
                                           OnPermissionRequest callback) {
        return requestPermission(act, code, callback, Manifest.permission.READ_PHONE_STATE);
    }

    public boolean getCallPhonePermission(Activity act,
                                          int code,
                                          OnPermissionRequest callback) {
        return requestPermission(act, code, callback, Manifest.permission.CALL_PHONE);
    }

    public boolean getCameraPermission(Activity act,
                                       int code,
                                       OnPermissionRequest callback) {
        return requestPermission(act, code, callback, Manifest.permission.CAMERA);
    }

    public boolean getReadExternalPermission(Activity activity,
                                             int code,
                                             OnPermissionRequest callback) {
        return requestPermission(activity, code, callback,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public boolean getWriteExternalPermission(Activity activity,
                                              int code,
                                              OnPermissionRequest callback) {
        return requestPermission(activity, code, callback,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public boolean getLocationPermission(Activity activity,
                                         int code,
                                         OnPermissionRequest callback) {
        return requestPermission(activity, code, callback,
                Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public boolean getReadContactsPermission(Activity activity,
                                             int code,
                                             OnPermissionRequest callback) {
        return requestPermission(activity, code, callback, Manifest.permission.READ_CONTACTS);
    }

    public boolean getAudioPermission(Activity activity,
                                      int code,
                                      OnPermissionRequest callback) {
        return requestPermission(activity, code, callback, Manifest.permission.RECORD_AUDIO);
    }

    public boolean getWriteCallLogsPermission(Activity activity,
                                              int code,
                                              OnPermissionRequest callback) {
        return requestPermission(activity, code, callback, Manifest.permission.WRITE_CALL_LOG);
    }

    public boolean checkPermissionAudio(Activity activity) {
        return checkPermission(activity, Manifest.permission.RECORD_AUDIO);
    }

    public boolean checkPermissionCamera(Activity activity) {
        return checkPermission(activity, Manifest.permission.CAMERA);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean requestPermission(Activity activity,
                                      int code,
                                      OnPermissionRequest callback,
                                      String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(
                        HyperApp.getInstance().getApplicationContext(), permission) ==
                        PackageManager.PERMISSION_GRANTED) {
            if (callback != null)
                callback.onGranted(code);
            return true;
        } else {
            boolean permissionRequested = false;
            for (PermissionRequest req : permissionRequests) {
                if (req.getCode() == code) {
                    permissionRequested = true;
                    break;
                }
            }
            if (!permissionRequested && activity != null)
                activity.requestPermissions(new String[]{permission}, code);
//            if (callback != null)
            //we need to add all permission request
            permissionRequests.add(new PermissionRequest(callback, code));
            return false;
        }
    }

    private boolean checkPermission(Activity activity,
                                    String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(activity, permission) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    public boolean getPermissions(Activity activity,
                                  int code,
                                  OnPermissionRequest callback,
                                  List<String> permissionsNeeded) {
        List<String> permissionsList = new ArrayList<>();
        boolean hasPermission = true;
        for (String permission : permissionsNeeded) {
            if (ContextCompat.checkSelfPermission(
                    HyperApp.getInstance().getApplicationContext(), permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                hasPermission = false;
            }
        }

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity,
                    permissionsList.toArray(new String[permissionsList.size()]), code);
            if (callback != null)
                permissionRequests.add(new PermissionRequest(callback, code));
        } else {
            if (callback != null)
                callback.onGranted(code);
        }

        return hasPermission;
    }
}