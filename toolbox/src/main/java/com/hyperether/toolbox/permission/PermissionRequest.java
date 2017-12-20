package com.hyperether.toolbox.permission;

/**
 * Class that represent permission requests sent to permission manager
 *
 * @author Slobodan Prijic
 * @version 1.0 - 12/20/2017
 */
public class PermissionRequest {

    private OnPermissionRequest onPermissionRequestCallback;

    private int code;

    private String tag;

    public PermissionRequest(OnPermissionRequest onPermissionRequestCallback,
                             int code,
                             String tag) {
        this.onPermissionRequestCallback = onPermissionRequestCallback;
        this.code = code;
        this.tag = tag;
    }

    public PermissionRequest(OnPermissionRequest onPermissionRequestCallback,
                             int code) {
        this.onPermissionRequestCallback = onPermissionRequestCallback;
        this.code = code;
    }

    public OnPermissionRequest getOnPermissionRequestCallback() {
        return onPermissionRequestCallback;
    }

    public void setOnPermissionRequestCallback(OnPermissionRequest onPermissionRequestCallback) {
        this.onPermissionRequestCallback = onPermissionRequestCallback;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}