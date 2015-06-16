package com.liebwerks.PocketRocketApp;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by klieberman on 6/11/15.
 */
public class LogDataToWebsite implements Runnable {
    double lat = 0.0f;
    double lon = 0.0f;
    double speed = 0.0f;
    double accuracy = 0.0f;

    LogDataToWebsite(double lat, double lon, double speed, double accuracy) {
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.accuracy = accuracy;
    }

    public void run() {
        try {
            URL url = new URL("http://leaflogger.com/logit.php?lat=" + lat + "&lon=" + lon + "&speed=" + speed + "&accuracy=" + accuracy);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            Log.d("URL", "Response code: " + urlConnection.getResponseCode());
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Log.d("URL", "Read: " + in.read());
            urlConnection.disconnect();
        }
        catch(Exception e) {
            Log.e("URL", "Error connecting to leaflogger.com: " + e);
        }
    }

}
