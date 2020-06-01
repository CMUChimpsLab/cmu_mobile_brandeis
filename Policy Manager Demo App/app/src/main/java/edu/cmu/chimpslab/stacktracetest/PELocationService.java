package edu.cmu.chimpslab.stacktracetest;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

public class PELocationService extends Service {
    @Override public void onCreate() {
        super.onCreate();
    }

    @Override public int onStartCommand(Intent intent,
                                        int flags,
                                        int startId) {
        super.onStartCommand(intent, flags, startId);

        LocationManager m = (LocationManager)getSystemService(LOCATION_SERVICE);

        try {
            if(m != null) {
                Location l = m.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(l != null) { l.describeContents(); }
            }
        }
        catch(SecurityException se) {
            se.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onDestroy() {
    }
}