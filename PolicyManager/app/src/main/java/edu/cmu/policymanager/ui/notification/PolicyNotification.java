package edu.cmu.policymanager.ui.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import edu.cmu.policymanager.PolicyManager.Util;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.configure.ActivityAppSettings;
import edu.cmu.policymanager.ui.configure.cards.globalsetting.GroupCardPermissionOption;
import edu.cmu.policymanager.ui.configure.globalsettings.ActivityGlobalConfigurePurpose;
import edu.cmu.policymanager.ui.configure.profiles.ActivityPolicyProfileMain;
import edu.cmu.policymanager.validation.Precondition;

import static edu.cmu.policymanager.ui.configure.ActivityAppSettings.INTENT_KEY_PACKAGE_NAME;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 1/10/2019.
 *
 * Builds a policy notification that displays to the user whether or not an app
 * was allowed to access data for a purpose, and where in the policy enforcement step this
 * decision was made.
 *
 * All fields must be set before a notification can be built.
 */

public class PolicyNotification {
    private final Context mContext;
    private SensitiveData mPermission;
    private Purpose mPurpose;
    private String mPackageName, mPolicySource;
    private boolean mAccessIsPermitted = false;

    private PolicyNotification(Context context) {
        mContext = context;
    }

    /**
     * Creates a PolicyNotification class, which is a sort of wrapper for Android notifications.
     *
     * @param context the Context
     * @return new PolicyNotification instance
     * */
    public static PolicyNotification create(Context context) {
        Precondition.checkIfNull(context, "Cannot create from null context");
        return new PolicyNotification(context);
    }

    /**
     * Specify the package name that just had a policy enforced.
     *
     * @param packageName the package name of the app that requested the data
     * @return this PolicyNotification instance
     * */
    public PolicyNotification forPackage(CharSequence packageName) {
        Precondition.checkEmptyCharSequence(packageName);

        mPackageName = packageName.toString();
        return this;
    }

    /**
     * Specify the purpose of the recent data access.
     *
     * @param purpose the purpose of data access
     * @return this PolicyNotification instance
     * */
    public PolicyNotification setPurpose(Purpose purpose) {
        Precondition.checkIfNull(purpose, "Purpose cannot be null");

        mPurpose = purpose;
        return this;
    }

    /**
     * Specify the permission that just had a policy enforced.
     *
     * @param permission the permission that was being requested
     * @return this PolicyNotification instance
     * */
    public PolicyNotification withPermission(SensitiveData permission) {
        Precondition.checkIfNull(permission, "Permission cannot be null");

        mPermission = permission;
        return this;
    }

    /**
     * Specify if the access to the requested permission was allowed or not.
     *
     * @param accessIsPermitted if access was permitted
     * @return this PolicyNotification instance
     * */
    public PolicyNotification isPermitted(boolean accessIsPermitted) {
        mAccessIsPermitted = accessIsPermitted;
        return this;
    }

    /**
     * Specify where in the policy enforcement algorithm this policy was enforced. This takes
     * any CharSequence, but should be one of profile name, "Quick Settings", "User Settings" or
     * "Global Settings".
     *
     * @param policySource the place in the enforcement algorithm the policy was enforced
     * @return this PolicyNotification instance
     * */
    public PolicyNotification from(CharSequence policySource) {
        Precondition.checkEmptyCharSequence(policySource);

        mPolicySource = policySource.toString();
        return this;
    }

    /**
     * Builds the Android notification for display in the notification tray from the values
     * specified while building the PolicyNotification.
     *
     * @return Android notification
     * */
    public Notification buildAndroidNotification() {
        Precondition.checkEmptyCharSequence(mPackageName);
        Precondition.checkState(mPurpose != null, "Must set a purpose");
        Precondition.checkState(mPermission != null, "Must set permission");
        Precondition.checkEmptyCharSequence(mPolicySource);

        Notification.Builder builder = new Notification.Builder(
                mContext,
                PolicyManagerNotificationService.CHANNEL_ID
        );

        String buttonString = mContext.getString(
                R.string.allysiqi_notification_button_text, mPolicySource
        );
        String appName = Util.getAppCommonName(mContext, mPackageName);

        String action = mAccessIsPermitted ? "on" : "off";

        String displayPermission = mPermission.getDisplayPermission().toString();

        PendingIntent activityToNavTo = createPolicyProfilePendingIntent();

        String title = "",
               body = mContext.getResources()
                              .getString(R.string.notification_body, mPurpose.name,
                                                                    action,
                                                                    mPolicySource);

        if(mPolicySource.equalsIgnoreCase("Global Settings")) {
            activityToNavTo = createGlobalSettingPendingIntent(mPackageName, mPermission);
        } else if(mPolicySource.equalsIgnoreCase("User Settings")) {
            activityToNavTo = createGeneralSettingPendingIntent(mPackageName, mPermission);
        }

        Notification.Action a = new Notification.Action.Builder(null,
                                                                buttonString,
                                                                activityToNavTo)
                                                                .build();

        int icon = R.drawable.ic_check_black_24dp;

        if(mAccessIsPermitted) {
            title = appName + " access to " + displayPermission + " was allowed";
        } else {
            title = appName + " access to " + displayPermission + " was denied";
            icon = R.drawable.ic_remove_circle_black_24dp;
        }

        Drawable appIconDrawable = PolicyManagerApplication.ui
                                                           .getIconManager()
                                                           .getAppIcon(mPackageName)
                                                           .asDrawable();

        BitmapDrawable appIconBitmap = PolicyManagerApplication.ui
                                                               .getIconManager()
                                                               .drawableToBitmap(appIconDrawable);

        builder.setContentTitle(title)
               .setStyle(new Notification.BigTextStyle().bigText(body))
               .setContentText(body)
               .setSmallIcon(icon)
               .setLargeIcon(appIconBitmap.getBitmap())
               .setGroup(PolicyManagerNotificationService.NOTIFICATION_GROUP_ID)
               .setShowWhen(true);

        if(!mPolicySource.equalsIgnoreCase("Quick Settings")) {
            builder.addAction(a);
            builder.setContentIntent(activityToNavTo);
        }

        return builder.build();
    }

    private PendingIntent createGlobalSettingPendingIntent(String packageName,
                                                           SensitiveData permission) {
        String systemPermission = permission.androidPermission.toString();
        String displayPermission = permission.getDisplayPermission().toString();

        Intent activityIntent = new Intent(mContext, ActivityGlobalConfigurePurpose.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.setAction(packageName + "." + systemPermission);
        activityIntent.putExtra(
                GroupCardPermissionOption.DISPLAY_PERMISSION_KEY,
                displayPermission
        );
        activityIntent.putExtra(GroupCardPermissionOption.PERMISSION_KEY, permission);

        return createPendingIntent(activityIntent);
    }

    private PendingIntent createGeneralSettingPendingIntent(String packageName,
                                                            SensitiveData permission) {
        String systemPermission = permission.androidPermission.toString();
        Intent activityIntent = new Intent(mContext, ActivityAppSettings.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.setAction(packageName + "." + systemPermission);
        activityIntent.putExtra(INTENT_KEY_PACKAGE_NAME, packageName);

        return createPendingIntent(activityIntent);
    }

    private PendingIntent createPolicyProfilePendingIntent() {
        Intent activityIntent = new Intent(mContext, ActivityPolicyProfileMain.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return createPendingIntent(activityIntent);
    }

    private PendingIntent createPendingIntent(Intent activity) {
        return PendingIntent.getActivity(
                mContext,
                1337,
                activity,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}