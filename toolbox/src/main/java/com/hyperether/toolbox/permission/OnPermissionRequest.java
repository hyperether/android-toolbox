package com.hyperether.toolbox.permission;

/**
 * Interface to represent permission requests
 *
 * @author Slobodan Prijic
 * @version 1.0 - 12/20/2017
 */
public interface OnPermissionRequest {
    void onGranted(int code);

    void onDenied(int code);
}