package edu.cmu.policymanager.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.AppDatabaseSingleton;
import edu.cmu.policymanager.DataRepository.db.model.AppInfo;
import edu.cmu.policymanager.DataRepository.network.GooglePlayStore;
import edu.cmu.policymanager.PolicyManager.CriticalSystemApps;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.util.FetchUtil;

/**
 * Used to detect when an app is being uninstalled from the device.
 *
 * Original file by Shawn + Swaroop
 * */
public class PackageChangeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String packageName = intent.getDataString().substring(8);

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction()) ||
            Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
            Log.i("uninstall-dbg", "Application " + packageName + " uninstalled");

            DataRepository.removeAppPackageFromRepository(packageName);
        }
    }
}