package com.hyperether.toolbox.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.hyperether.toolbox.HyperApp;

import java.util.List;

/*
 * HyperLocationHandler.java
 *
 * In order to have proper work, call:
 * 1.setLocationUpdateCallback
 * 2.createLocationRequest
 * 3.requestLocationUpdates
 *
 * Created by Slobodan on 12/11/2017
 */

public class HyperLocationHandler {

    private static final String TAG = HyperLocationHandler.class.getSimpleName();

    /**
     * Set up this one according to battery saving - used by device location provider
     */
    private static final float LOCATION_DISTANCE = 0;

    private static HyperLocationHandler instance;

    private Location lastLocation;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    private float locDistanceTrigger = LOCATION_DISTANCE;

    private OnLocationUpdate locationUpdateCallback;


    private HyperLocationHandler() {
        Context context = HyperApp.getInstance().getApplicationContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public static HyperLocationHandler getInstance() {
        if (instance == null)
            instance = new HyperLocationHandler();
        return instance;
    }

    public void processLocation(LocationResult result) {
        if (result != null) {
            List<Location> locations = result.getLocations();
            Location newLocation = result.getLastLocation();
            if (lastLocation == null) {
                lastLocation = newLocation;
                if (locationUpdateCallback != null)
                    locationUpdateCallback.onLocationUpdate(newLocation);
            } else {
                double distance = newLocation.distanceTo(lastLocation);
                if (distance > locDistanceTrigger) {
                    lastLocation = newLocation;
                    if (locationUpdateCallback != null)
                        locationUpdateCallback.onLocationUpdate(newLocation);
                }
            }
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    public void createLocationRequest(long interval, long fastestUpdate, float distance) {
        locDistanceTrigger = distance;

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

    private PendingIntent getPendingIntent() {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(context, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Context context = HyperApp.getInstance().getApplicationContext();
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    public void setLocationUpdateCallback(OnLocationUpdate locationUpdateCallback) {
        this.locationUpdateCallback = locationUpdateCallback;
    }

    interface OnLocationUpdate {
        void onLocationUpdate(Location location);
    }
}
