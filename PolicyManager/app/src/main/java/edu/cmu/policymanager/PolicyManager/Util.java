package edu.cmu.policymanager.PolicyManager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import edu.cmu.policymanager.application.PolicyManagerApplication;

/**
 * Created by swarupks on 6/14/2016.
 */
public class Util {

    static public String getAppCommonName(Context context, String packageKey) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    packageKey, PackageManager.GET_META_DATA);
            if (info != null) {
                return context.getPackageManager().getApplicationLabel(info).toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
            System.out.println("Package Name " + packageKey+ " not found... Expected behavior in some cases\n");
        }
        return packageKey;
    }
}