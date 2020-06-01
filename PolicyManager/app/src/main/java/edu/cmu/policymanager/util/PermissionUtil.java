package edu.cmu.policymanager.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.application.PolicyManagerApplication;

/**
 * Created by shawn on 9/18/2017.
 */

public class PermissionUtil {
    private static final String TAG = PermissionUtil.class.getName();

    public static String filterPermission(String permissionName) {
        String modifiedPermissionName = permissionName;
        if (modifiedPermissionName.startsWith("android.permission.")) {
            modifiedPermissionName = modifiedPermissionName.replace("android.permission.", "");
        } else {
            return permissionName;
        }
        modifiedPermissionName = modifiedPermissionName.replaceAll("_"," ");
        return modifiedPermissionName;
    }

    public static void incrementDataAccessCount(final String permission) {
        (new Thread() {
            public void run() {
                try {
                    Map<String, String> metadata = DataRepository.getInstance().getMetadataByOwner(permission);
                    String accessCount = ("" + (Integer.parseInt(metadata.get("accessCount")) + 1));
                    metadata.put("accessCount", accessCount);
                    DataRepository.getInstance().updateMetadata(permission, metadata);
                }
                catch(NullPointerException e) {
                    Log.d("Error", "No config for " + permission);
                    Log.d("Error", e.getMessage());
                }
            }
        }).start();
    }

    public static String getDisplayPermission(String permission) {
        if(permission.contains(PolicyManagerApplication.SYMBOL_ALL)) { return "All Permissions"; }

        String[] permissionEntry = DataRepository.getPermissionMap().get(permission);
        if(permissionEntry != null) {
            String display = permissionEntry[0];
            return display == null ? filterPermission(permission) : display;
        }

        return "";
    }

    public static String getPermissionSensitivity(String permission) {
        String[] sensitivity = DataRepository.getPermissionMap().get(permission);
        if (sensitivity == null) {
            Log.e(TAG, "couldn't find permission name: "+permission+" in permission map. assuming sensitivity 1");
            return "1";
        }
        return sensitivity[1];
    }
}