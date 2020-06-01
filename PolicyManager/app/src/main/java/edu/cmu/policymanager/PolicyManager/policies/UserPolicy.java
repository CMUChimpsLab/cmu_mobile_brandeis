package edu.cmu.policymanager.PolicyManager.policies;

import android.util.Log;

import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfileSetting;
import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Models a user policy, which is the action a user has determined
 * an access to data should take upon its request. A data access may
 * take one of three actions:
 *  - ALLOW (or On): The app is granted access to the data it is requesting.
 *  - DENY (or Off): The app is denied access to the data it is requesting.
 *  - ASK: The user will be immediately prompted for an action.
 *
 *  There are two types of user policies: an app-level policy (not to be confused with
 *  app policy files) and global policies. App-level policies dictate how an individual
 *  non-system app may or may not access sensitive data. Global policies affect all
 *  non-system apps installed on a device.
 *
 *  Individual app-level policies do not affect each other, but policy enforcement is different
 *  when a global setting is configured. The policy manager will enforce the most recently
 *  configured policy. If a global setting is configured first, and then an app's policy is
 *  set right after, then the policy manager will enforce the app's policy. If some other
 *  app requests the same data, the global policy's setting will be enforced.
 *
 *  When data is requested by an app, the policy manager sends the data request through
 *  an algorithm to determine what the outcome should be. The process of this at a high-level
 *  is: policy profile -> quick settings -> user/global settings.
 *
 *  The incoming request will also be subjected to analysis before being handed off.
 *  Please see other research documents for more detail on this process.
 *
 *  Created by Mike Czapik (Carnegie Mellon University).
 */
public class UserPolicy {
    public String profile = PolicyProfile.DEFAULT,
                  app = "";

    public SensitiveData permission;
    public Purpose purpose;
    public ThirdPartyLibrary thirdPartyLibrary;

    private long mLastUpdated = -1L;

    private Policy mAction = Policy.ALLOW;

    /**
     * The policy that should be enforced for when sensitive user data is accessed.
     * Allow - grants access to the app requesting data
     * Deny - denies access to the app requesting data
     * Ask - prompt the user for an allow or deny decision at the time data is accessed
     * */
    public enum Policy { ALLOW, DENY, ASK }

    /**
     * Converts a PolicyProfileSetting Room object into a UserPolicy.
     *
     * @param setting the PolicyProfileSetting database object to convert
     * @return the UserPolicy representing this setting
     * */
    public static UserPolicy convert(PolicyProfileSetting setting) {
        return new UserPolicy(setting);
    }

    /**
     * Create a user policy from a permission request. By default, this policy's last updated
     * time is -1.
     *
     * @param request the PermissionRequest object to create a UserPolicy from
     * @return the UserPolicy for an app
     * */
    public static UserPolicy fromPermissionRequest(PermissionRequest request) {
        Precondition.checkIfNull(request, "Cannot create from null request");

        return new UserPolicy(
                request.packageName,
                request.permission,
                request.purpose,
                request.thirdPartyLibrary,
                -1L
        );
    }

    /**
     * Create an app policy (policy that controls an individual app). By default,
     * this policy's last updated time is -1.
     *
     * @param packageName the package name of the app to model a setting for
     * @param permission the SensitiveData being accessed
     * @param purpose the Purpose for the access of SensitiveData
     * @param thirdPartyLibrary the ThirdPartyLibrary using this data
     * @return the UserPolicy for an app
     * */
    public static UserPolicy createAppPolicy(CharSequence packageName,
                                             SensitiveData permission,
                                             Purpose purpose,
                                             ThirdPartyLibrary thirdPartyLibrary) {
        return new UserPolicy(
                packageName.toString(),
                permission,
                purpose,
                thirdPartyLibrary,
                -1L
        );
    }

    /**
     * Create a global policy (affects all apps at the time it is modified). By default,
     * this policy's last updated time is -1.
     *
     * @param permission the SensitiveData being accessed
     * @param purpose the Purpose for the access of SensitiveData
     * @param thirdPartyLibrary the ThirdPartyLibrary using this data
     * @return the UserPolicy for all non-system apps on this device
     * */
    public static UserPolicy createGlobalPolicy(SensitiveData permission,
                                                Purpose purpose,
                                                ThirdPartyLibrary thirdPartyLibrary) {
        return new UserPolicy(
                PolicyManagerApplication.SYMBOL_ALL,
                permission,
                purpose,
                thirdPartyLibrary,
                -1L
        );
    }

    public UserPolicy() { }

    private UserPolicy(PolicyProfileSetting setting) {
        if(setting.app == null || setting.app.isEmpty()) {
            throw new IllegalArgumentException("Cannot create user policy with no app");
        } else if(setting.permission == null || setting.permission.isEmpty()) {
            throw new IllegalArgumentException("Cannot create user policy with no permission");
        } else if(setting.purpose == null || setting.purpose.isEmpty()) {
            throw new IllegalArgumentException("Cannot create user policy with no purpose");
        }

        profile = setting.profileName;
        app = setting.app;
        permission = DangerousPermissions.from(setting.permission);
        purpose = Purposes.from(setting.purpose);
        thirdPartyLibrary = ThirdPartyLibraries.from(setting.thirdPartyLibrary);
        mLastUpdated = setting.lastUpdated;
        mAction = Policy.valueOf(setting.policyAction);
    }

    private UserPolicy(String app,
                       SensitiveData permission,
                       Purpose purpose,
                       ThirdPartyLibrary thirdPartyLibrary,
                       long lastUpdated) {
        if(app == null || app.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an user policy with no app");
        } else if(permissionIsNotValid(permission)) {
            throw new IllegalArgumentException("Cannot create user policy with no permission");
        } else if(purpose == null) {
            throw new IllegalArgumentException("Cannot create an app policy with no purpose");
        }

        this.app = app;
        this.permission = permission;
        this.purpose = purpose;
        this.thirdPartyLibrary = thirdPartyLibrary;
        this.mLastUpdated = lastUpdated;
    }

    /**
     * Checks if SensitiveData is allowed to be accessed for Purpose when used by
     * ThirdPartyLibrary.
     *
     * @return if the policy to enforce is to allow access to the requested data
     * */
    public boolean isAllowed() { return mAction.name().equals("ALLOW"); }

    /**
     * Checks if access is denied to SensitiveData for Purpose when used by
     * ThirdPartyLibrary.
     *
     * @return if the policy to enforce is to deny access to the requested data
     * */
    public boolean isDenied() { return mAction.name().equals("DENY"); }

    /**
     * Checks if the user is to be prompted for a policy decisions whenever SensitiveData
     * is accessed for Purpose when used by ThirdPartyLibrary.
     *
     * @return if the policy to enforce is 'ask', or prompt the user for their policy decision
     */
    public boolean isAsk() { return mAction.name().equals("ASK"); }

    /**
     * Allow access to the given SensitiveData for Purpose when used by ThirdPartyLibrary.
     * Will also update the timestamp for when the last time this policy changed.
     * */
    public void allow() {
        mLastUpdated = System.currentTimeMillis();
        mAction = Policy.ALLOW;
    }

    /**
     * Deny access to the given SensitiveData for Purpose when used by ThirdPartyLibrary.
     * Will also update the timestamp for when the last time this policy changed.
     * */
    public void deny() {
        mLastUpdated = System.currentTimeMillis();
        mAction = Policy.DENY;
    }

    /**
     * Prompt the user for their policy decision when SensitiveData is accessed for Purpose
     * when used by ThirdPartyLibrary.
     * Will also update the timestamp for when the last time this policy changed.
     * */
    public void ask() {
        mLastUpdated = System.currentTimeMillis();
        mAction = Policy.ASK;
    }

    /**
     * Get the timestamp represented by System.currentTimeMillis() of the last time
     * this policy was changed.
     *
     * @return the timestamp.
     * */
    public long getLastUpdatedMillis() { return mLastUpdated; }

    public boolean equals(Object otherPolicy) {
        UserPolicy other = (UserPolicy)otherPolicy;

        return (other.app.equals(app) &&
                other.permission.equals(permission) &&
                other.purpose.equals(purpose) &&
                other.thirdPartyLibrary.equals(thirdPartyLibrary));
    }

    public String toString() {
        String appString = (app == null ? "unknown" : app),
               permissionString = (permission == null ?
                       "unknown permission" : permission.androidPermission.toString()),
               purposeString = (purpose == null ?
                       "unknown purpose" : purpose.name.toString()),
               libraryString = (thirdPartyLibrary == null ?
                       "unknown library or internal" : thirdPartyLibrary.qualifiedName);

        return appString + " [" + permissionString + " -> (" + purposeString +
                ", " + libraryString + ") : " + mAction + "] @ " + mLastUpdated;
    }

    /**
     * Makes a clone of this current UserPolicy - with the exceptions that lastUpdated is
     * -1 and the policy action is not copied either.
     *
     * @return copy of this UserPolicy without lastUpdated or policy action
     * */
    public Object clone() {
        return new UserPolicy(app, permission, purpose, thirdPartyLibrary, -1L);
    }

    private boolean permissionIsNotValid(final SensitiveData permission) {
        return permission == null ||
               permission.androidPermission == null ||
               permission.androidPermission.length() == 0;
    }
}