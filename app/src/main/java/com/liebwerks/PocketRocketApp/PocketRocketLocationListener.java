package com.liebwerks.PocketRocketApp;

import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

import android.os.Bundle;
/**
 * Created by klieberman on 6/11/15.
 */
public class PocketRocketLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            Log.d("PocketRocketLocationListener", "Location changed: " + location.getLatitude() + ", " + location.getLongitude() + " Speed: " + location.getSpeed());
            LogDataToWebsite logger = new LogDataToWebsite(location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getAccuracy());
            new Thread(logger).start();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("PocketRocketLocationListener", "Status changed to: " + status);

        }

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}

}
