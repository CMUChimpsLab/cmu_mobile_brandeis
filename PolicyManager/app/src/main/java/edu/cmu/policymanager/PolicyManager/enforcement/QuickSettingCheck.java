package edu.cmu.policymanager.PolicyManager.enforcement;

import android.Manifest;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.policymanager.PolicyManager.PolicyNotification;
import edu.cmu.policymanager.peandroid.PEAndroid;

/**
 * References the current state of quick setting switches to determine if access
 * to requested data is allowed or not.
 *
 * If quick settings allow access to data, the policy enforcement algorithm then checks
 * user settings to see what to do. Quick settings are overrode by policy profile settings if
 * those settings deny access.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class QuickSettingCheck extends PolicyEnforcementDecorator {
    public static final Map<String, Boolean> quickSettings = new HashMap<String, Boolean>();

    static {
        quickSettings.put(Manifest.permission.ACCESS_FINE_LOCATION, true);
        quickSettings.put(Manifest.permission.ACCESS_COARSE_LOCATION, true);
        quickSettings.put(Manifest.permission.RECORD_AUDIO, true);
        quickSettings.put(Manifest.permission.CAMERA, true);
    }

    public QuickSettingCheck(final PolicyEnforcement policyEnforcer) { super(policyEnforcer); }

    /**
     * References quick settings to see if a data access is allowed or denied.
     *
     * @return EnforcementStatus.Code.TERMINATED if a previous step completed the check process
     *         EnforcementStatus.Code.QUICKSETTING_DISABLED if access is denied
     *         EnforcementStatus Code of the next step of the enforcement process if neither
     *         of these apply
     * */
    public EnforcementStatus.Code isAllowed() {
        if(didTerminate()) {
            return EnforcementStatus.Code.TERMINATED;
        }
        else {
            String permission = getPermissionRequest().permission.androidPermission.toString();

            if(quickSettings.containsKey(permission)) {
                boolean quickSettingsAllow = quickSettings.get(permission);

                if(quickSettingsAllow) {
                    return super.isAllowed();
                }
                else {
                    terminate();
                    PermissionRequest req = getPermissionRequest();
                    PolicyNotification.sendDeniedNotification(req.context,
                                                       "Quick Settings",
                                                              req);

                    PEAndroid.connectToReceiver(req.recv).denyPermission();
                    return EnforcementStatus.Code.QUICKSETTING_DISABLED;
                }
            }
            else {
                return super.isAllowed();
            }
        }
    }
}