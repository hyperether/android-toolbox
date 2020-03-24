package com.hyperether.toolbox;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.hyperether.toolbox.HyperApp;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class HyperNetworkState extends ConnectivityManager.NetworkCallback {
    private static HyperNetworkState instance = null;

    private final NetworkRequest networkRequest;

    private OnNetworkAvailableListener onNetworkAvailableListener;
    private OnNetworkLosingListener onNetworkLosingListener;
    private OnNetworkLostListener onNetworkLostListener;
    private OnNetworkUnavailableListener onNetworkUnavailableListener;

    public static HyperNetworkState getInstance() {
        if (instance == null) {
            instance = new HyperNetworkState();
        }
        return instance;
    }

    private HyperNetworkState() {
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
    }

    public void enable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) HyperApp.getInstance()
                .getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
            connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    public void disable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) HyperApp.getInstance()
                .getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
            connectivityManager.unregisterNetworkCallback(this);
    }

    public void setOnNetworkAvailableListener(OnNetworkAvailableListener listener) {
        this.onNetworkAvailableListener = listener;
    }

    public void setOnNetworkLosingListener(OnNetworkLosingListener listener) {
        this.onNetworkLosingListener = listener;
    }

    public void setOnNetworkLostListener(OnNetworkLostListener listener) {
        this.onNetworkLostListener = listener;
    }

    public void setOnNetworkUnavailableListener(OnNetworkUnavailableListener listener) {
        this.onNetworkUnavailableListener = listener;
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        onNetworkAvailableListener.onAvailable(network);
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
        onNetworkLosingListener.onLosing(network, maxMsToLive);
    }

    @Override
    public void onLost(@NonNull Network network) {
        onNetworkLostListener.onLost(network);
    }

    @Override
    public void onUnavailable() {
        onNetworkUnavailableListener.onUnavailable();
    }

    public interface OnNetworkAvailableListener {
        void onAvailable(Network network);
    }

    public interface OnNetworkLosingListener {
        void onLosing(Network network, int maxMsToLive);
    }

    public interface OnNetworkLostListener {
        void onLost(Network network);
    }

    public interface OnNetworkUnavailableListener {
        void onUnavailable();
    }
}
