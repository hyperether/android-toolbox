package com.hyperether.toolbox.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import com.hyperether.toolbox.HyperApp;
import com.hyperether.toolbox.HyperLog;

import java.util.List;

/*
 * HyperConnection.java
 *
 * Created by Slobodan on 12/11/2017
 */

public class HyperConnection {

    private static final String TAG = HyperConnection.class.getSimpleName();

    /**
     * Check internet access
     */
    public static boolean hasInternetAccess() {
        boolean hasInternet = false;
        ConnectivityManager cm = (ConnectivityManager) HyperApp.getInstance()
                .getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            hasInternet = true;
        }

        return hasInternet;
    }

    /**
     * Get the network info
     */
    public static NetworkInfo getNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) HyperApp.getInstance()
                .getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity to a Wifi network
     */
    public static boolean isConnectedWifi() {
        NetworkInfo info = getNetworkInfo();
        return (info != null && info.isConnected() &&
                info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     */
    public static boolean isConnectedMobile() {
        NetworkInfo info = getNetworkInfo();
        return (info != null && info.isConnected() &&
                info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     */
    public static boolean isConnectedFast() {
        NetworkInfo info = getNetworkInfo();
        return (info != null && info.isConnected() &&
                isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps

                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Check if the connection is fast
     */
    public static boolean isConnection4G() {
        NetworkInfo info = getNetworkInfo();
        if (info != null && info.isConnected()) {
            int type = info.getType();
            int subType = info.getSubtype();
            if (type == ConnectivityManager.TYPE_MOBILE) {
                switch (subType) {
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return true; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return true; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return true; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                        return true; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                        return true; // ~ 10+ Mbps
                    default:
                        return false;
                }
            }
        }
        return false;
    }

    public static boolean isSIMPresent() {
        TelephonyManager tm = getTelephonyManager();
        if (tm != null) {
            int phoneType = tm.getPhoneType();
            switch (phoneType) {
                case TelephonyManager.PHONE_TYPE_NONE:
                    return false;
                case TelephonyManager.PHONE_TYPE_GSM:
                case TelephonyManager.PHONE_TYPE_CDMA:
                case TelephonyManager.PHONE_TYPE_SIP:
                    return true;
            }
        }
        return false;
    }

    public static boolean isSIMReady() {
        TelephonyManager tm = getTelephonyManager();
        if (tm != null) {
            int simState = tm.getSimState();
            switch (simState) {
                case TelephonyManager.SIM_STATE_READY:
                    return true;
                case TelephonyManager.SIM_STATE_ABSENT:
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    break;
            }
        }
        return false;
    }

    /**
     * Check if network is roaming
     */
    public static boolean isRoaming() {
        TelephonyManager tm = getTelephonyManager();
        if (tm != null) {
            return tm.isNetworkRoaming();
        }
        // default returned value
        return false;
    }

    public static int getWifiLevel(Context context) {
        int signalLevel = -1;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiManager.isWifiEnabled() && wifiInfo != null) {
            signalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
        }
        return signalLevel;
    }

    public static int getGsmStrength(Context context) {
        int signalLevel = -1;
        TelephonyManager tManager = getTelephonyManager();
        if (tManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                ActivityCompat.checkSelfPermission(
                        HyperApp.getInstance().getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            List<CellInfo> cellInfoList = tManager.getAllCellInfo();
            if (cellInfoList != null) {
                if (!cellInfoList.isEmpty()) {
                    for (CellInfo cellInfo : cellInfoList) {
                        if (cellInfo != null && cellInfo.isRegistered())
                            if (cellInfo instanceof CellInfoGsm) {
                                signalLevel = ((CellInfoGsm) cellInfo)
                                        .getCellSignalStrength()
                                        .getLevel();
                            } else if (cellInfo instanceof CellInfoLte) {
                                signalLevel = ((CellInfoLte) cellInfo)
                                        .getCellSignalStrength()
                                        .getLevel();
                            } else if (cellInfo instanceof CellInfoCdma) {
                                signalLevel = ((CellInfoCdma) cellInfo)
                                        .getCellSignalStrength()
                                        .getLevel();
                            } else if (cellInfo instanceof CellInfoWcdma &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                signalLevel = ((CellInfoWcdma) cellInfo)
                                        .getCellSignalStrength()
                                        .getLevel();
                            }
                    }
                }
            }
        }
        return signalLevel;
    }

    public static TelephonyManager getTelephonyManager() {
        Context context = HyperApp.getInstance().getApplicationContext();
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static String getNetworkCountryIso() {
        TelephonyManager tm = getTelephonyManager();
        if (tm != null) {
            return tm.getNetworkCountryIso();
        }
        // default returned value for getNetworkCountryIso
        return "";
    }

    private static void log(String method, String msg) {
        HyperLog.getInstance().d(TAG, method, msg);
    }
}
