package com.hyperether.toolbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class HyperNetworkState extends ConnectivityManager.NetworkCallback {

    private final NetworkRequest networkRequest;
    private final Activity activity;
    private AlertDialog networkErrorBuilder;

    public HyperNetworkState(Activity activity) {
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        this.activity = activity;
    }

    public void enable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, this);
        if (!HyperConnection.isNetworkActive()) {
            alertNetworkError(activity);
        }
    }

    @Override
    public void onAvailable(Network network) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeAlertNetworkError();
            }
        });
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertNetworkError(activity);
            }
        });
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertNetworkError(activity);
            }
        });
    }

    private void initErrorNetworkAlert(Activity activity, @Nullable Integer dialogStyle) {
        if (networkErrorBuilder == null) {
            if (dialogStyle != null)
                networkErrorBuilder = new AlertDialog.Builder(activity, dialogStyle).create();
            else
                networkErrorBuilder = new AlertDialog.Builder(activity).create();
            networkErrorBuilder
                    .setTitle(HyperApp.getInstance().getApplicationContext().getResources()
                            .getString(R.string.alert_error));
            networkErrorBuilder.setMessage(
                    HyperApp.getInstance().getApplicationContext().getResources().
                            getString(R.string.network_info_not_connected));
            networkErrorBuilder
                    .setButton(DialogInterface.BUTTON_NEUTRAL,
                            HyperApp.getInstance().getApplicationContext().getResources().
                                    getString(R.string.ok_btn),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
        }
    }

    public void alertNetworkError(Activity activity) {
        if (networkErrorBuilder == null) {
            initErrorNetworkAlert(activity, null);
        }
        try {
            if (!networkErrorBuilder.isShowing())
                networkErrorBuilder.show();
        } catch (WindowManager.BadTokenException e) {
            HyperLog.getInstance().e("AlertManager", "alertNetworkError", e);
        }
    }

    public void removeAlertNetworkError() {
        if (networkErrorBuilder != null && networkErrorBuilder.isShowing())
            try {
                networkErrorBuilder.dismiss();
            } catch (Exception e) {
                HyperLog.getInstance().e("AlertManager", "removeAlertNetworkError", e);
            }

    }
}
