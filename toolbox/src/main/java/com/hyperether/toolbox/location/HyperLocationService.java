package com.hyperether.toolbox.location;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.hyperether.toolbox.HyperConst;


/*
 * HyperLocationService.java
 *
 * Created by Slobodan on 12/11/2017
 */
public abstract class HyperLocationService extends Service {

    private static final String TAG = HyperLocationService.class.getSimpleName();

    /*
        Default update interval, if no value retrieved from service
     */
    private static final long UPDATE_INTERVAL_MS = 3000;
    /*
        Default fastest update interval, if no value retrieved from service
    */
    private static final long FASTEST_UPDATE_INTERVAL_MS = UPDATE_INTERVAL_MS / 2;
    /*
        Default distance. Set up this one according to battery saving - used by device location provider
     */
    private static final float LOCATION_DISTANCE = 0;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private Location lastLocation;
    private Location previousLocation;

    private float locDistanceUpdateLevel = LOCATION_DISTANCE;

    abstract protected void onLocationUpdate(Location location);

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        long interval = intent.getLongExtra(HyperConst.LOC_INTERVAL, UPDATE_INTERVAL_MS);
        long fastestUpdate = intent
                .getLongExtra(HyperConst.LOC_FASTEST_INTERVAL, FASTEST_UPDATE_INTERVAL_MS);
        float distance = intent.getFloatExtra(HyperConst.LOC_DISTANCE, LOCATION_DISTANCE);
        locDistanceUpdateLevel = distance;
        createLocationRequest(interval, fastestUpdate, distance);
        createLocationCallback();
        startLocationUpdates();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    protected void startForeground() {

    }

    private void startLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.myLooper());
        } catch (SecurityException ex) {
            throw ex;
        }
    }

    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void createLocationRequest(long interval, long fastestUpdate, float distance) {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(interval);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        // This is also important to prevent locations stored in db at same ms
        mLocationRequest.setFastestInterval(fastestUpdate);

        mLocationRequest.setSmallestDisplacement(distance);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lastLocation = locationResult.getLastLocation();
                if (previousLocation == null) {
                    previousLocation = lastLocation;
                    onLocationUpdate(lastLocation);
                } else {
                    double distance = lastLocation.distanceTo(previousLocation);
                    if (distance > locDistanceUpdateLevel) {
                        previousLocation = lastLocation;
                        onLocationUpdate(lastLocation);
                    }
                }
            }
        };
    }
}