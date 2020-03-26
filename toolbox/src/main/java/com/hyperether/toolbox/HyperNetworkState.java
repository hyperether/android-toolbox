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

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class HyperNetworkState extends ConnectivityManager.NetworkCallback {
    private static HyperNetworkState instance = null;

    private final NetworkRequest networkRequest;

    private List<OnNetworkAvailableListener> availableListenerList = new ArrayList<>();
    private List<OnNetworkLosingListener> losingListenerList = new ArrayList<>();
    private List<OnNetworkLostListener> lostListenerList = new ArrayList<>();
    private List<OnNetworkUnavailableListener> unavailableListenerList = new ArrayList<>();

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
        availableListenerList.add(listener);
    }

    public void setOnNetworkLosingListener(OnNetworkLosingListener listener) {
        losingListenerList.add(listener);
    }

    public void setOnNetworkLostListener(OnNetworkLostListener listener) {
        lostListenerList.add(listener);
    }

    public void setOnNetworkUnavailableListener(OnNetworkUnavailableListener listener) {
        unavailableListenerList.add(listener);
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        for (OnNetworkAvailableListener listener: availableListenerList) {
            listener.onAvailable(network);
        }
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
        for (OnNetworkLosingListener listener: losingListenerList) {
            listener.onLosing(network, maxMsToLive);
        }
    }

    @Override
    public void onLost(@NonNull Network network) {
        for (OnNetworkLostListener listener: lostListenerList) {
            listener.onLost(network);
        }
    }

    @Override
    public void onUnavailable() {
        for (OnNetworkUnavailableListener listener: unavailableListenerList) {
            listener.onUnavailable();
        }
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
