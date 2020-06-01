package edu.cmu.chimpslab.stacktracetest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.privacy.PermissionRequestManager;
import android.privatedata.DataRequest;
import android.privatedata.PrivateDataManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;


public class MainActivity extends Activity implements MoPubView.BannerAdListener {
    private final Context activityContext = this;
    private MoPubView moPubView;
    private static final String AD_ID = "b195f8dd8ded45fe847ad89ed1d016da";

    private Thread rapidLocationCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        advertise();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(moPubView != null) {
            moPubView.destroy();
        }
    }

    public String appLocation(View v) {
        Location l = getLocation();

        if(l != null) { return String.valueOf(l); }

        return "no location";
    }

    public void testMicroPAL(View v) {
        PrivateDataManager pdm = PrivateDataManager.getInstance();

        Bundle params =
                new DataRequest.LocationParamsBuilder()
                               .setUpdateMode(DataRequest.LocationParamsBuilder.MODE_LAST_LOCATION)
                               .setProvider(LocationManager.GPS_PROVIDER)
                               .setTimeoutMillis(0)
                               .build();

        DataRequest palRequest =
                new DataRequest(
                        activityContext,
                        DataRequest.DataType.LOCATION,
                        params,
                        "com.twosixlabs.testmicropal.TestMicroPalService",
                        new Bundle(),
                        DataRequest.Purpose.GAME(""),
                        mReceiver
                );

        pdm.requestData(palRequest);
    }

    private Location getLocation() {
        LocationManager m = (LocationManager)getSystemService(LOCATION_SERVICE);

        try {
            if(m != null) {
                TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                PermissionRequestManager.setPrivacyPurpose("Search Nearby Places");
                tm.getImei();
                //Location l = m.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                runOnUiThread(displayAllowed());
                return null;
            }
        }
        catch(SecurityException se) {
            runOnUiThread(displayDenied());
            se.printStackTrace();
        }

        return null;
    }

    public void testLocationAnonymousClass(View v) {
        (new Thread() {
            public void run() {
                LocationManager m = (LocationManager)getSystemService(LOCATION_SERVICE);

                try {
                    if(m != null) {
                        Location l = m.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        runOnUiThread(displayAllowed());
                        if(l != null) { l.describeContents(); }
                    }
                }
                catch(SecurityException se) {
                    runOnUiThread(displayDenied());
                    se.printStackTrace();
                }
            }
        }).start();
    }

    public void startRapidLocationCollection(View v) {
        findViewById(R.id.start_rapid_location_collection_button).setEnabled(false);
        findViewById(R.id.end_rapid_location_collection_button).setEnabled(true);

        rapidLocationCollection = createLocationCollectionTask();
        rapidLocationCollection.start();
    }

    public void stopRapidLocationCollection(View v) {
        findViewById(R.id.start_rapid_location_collection_button).setEnabled(true);
        findViewById(R.id.end_rapid_location_collection_button).setEnabled(false);

        rapidLocationCollection.interrupt();
    }

    public void testLocationService(View v) {
        Intent locationService = new Intent(this, PELocationService.class);
        startService(locationService);
    }

    private void advertise() {
        SdkConfiguration config = new SdkConfiguration.Builder(AD_ID)
                                                      .withLogLevel(MoPubLog.LogLevel.DEBUG)
                                                      .build();
        
        SdkInitializationListener listener = new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                moPubView = findViewById(R.id.adview);
                moPubView.setAdUnitId(AD_ID);
                moPubView.loadAd();
            }
        };

        MoPub.initializeSdk(activityContext, config, listener);
    }

    private Runnable displayAllowed() {
        return new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activityContext, "Permission Allowed", Toast.LENGTH_LONG).show();
            }
        };
    }

    private Runnable displayDenied() {
        return new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activityContext, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        };
    }

    private Runnable displayError(final String errorMessage) {
        return new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activityContext, errorMessage, Toast.LENGTH_LONG).show();
            }
        };
    }

    private Thread createLocationCollectionTask() {
        return new Thread() {
            @Override
            public void run() {
                LocationManager m = (LocationManager)getSystemService(LOCATION_SERVICE);

                try {
                    if(m != null) {
                        while(!Thread.currentThread().isInterrupted()) {
                            Location l = m.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            Log.d("pm-debug", "Allowed access to location");
                            Thread.sleep(250);
                        }
                    }
                    else {
                        runOnUiThread(displayError("Cannot get location manager instance"));
                    }
                }
                catch(SecurityException se) {
                    Log.d("pm-debug", "Denied access to location");
                } catch(InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        };
    }

    // Sent when the banner has successfully retrieved an ad.
    public void onBannerLoaded(MoPubView banner) {
        findViewById(R.id.mopub_error_display).setVisibility(View.GONE);
    }

    // Sent when the banner has failed to retrieve an ad. You can use the MoPubErrorCode value to diagnose the cause of failure.
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        Log.d("pm-debug", "mopub failed with error code: " + errorCode.getIntCode());
    }

    // Sent when the user has tapped on the banner.
    public void onBannerClicked(MoPubView banner) {}

    // Sent when the banner has just taken over the screen.
    public void onBannerExpanded(MoPubView banner) {}

    // Sent when an expanded banner has collapsed back to its original size.
    public void onBannerCollapsed(MoPubView banner) {}

    private ResultReceiver mReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(activityContext.getPackageName(), "Received result");

            if (resultCode == PrivateDataManager.RESULT_SUCCESS) {
                Log.d(activityContext.getPackageName(), "Successfully called PAL module");
            }
        }
    };
}