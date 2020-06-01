package edu.cmu.policymanager.PolicyManager;

import android.content.Context;
import android.content.Intent;

import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;

import static edu.cmu.policymanager.ui.notification.PolicyManagerNotificationService.DATA_ACCESSED;
import static edu.cmu.policymanager.ui.notification.PolicyManagerNotificationService.KEY_POLICY_RESULT;

/**
 * Sends broadcast to notification service that some policy action has occurred. Can send
 * allow and deny notification broadcasts.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 */
public class PolicyNotification {
    public static final String KEY_PACKAGE_NAME = "app",
                               KEY_PERMISSION_NAME = "permission",
                               KEY_PURPOSE = "purpose",
                               KEY_POLICY_SOURCE = "policy_source";

    public static final String POLICY_ALLOWED =
            "edu.cmu.policymanager.PolicyManager.action.ACCESS_ALLOWED",
                               POLICY_DENIED =
            "edu.cmu.policymanager.PolicyManager.action.ACCESS_DENIED";

    /**
     * Send a broadcast to the notification service to push a notification that this
     * permission request was allowed.
     *
     * @param context the Context
     * @param source where the in the policy enforcement process the decision was made
     * @param request the permission request that was subject to policy enforcement
     * */
    public static void sendAllowedNotification(Context context,
                                               CharSequence source,
                                               PermissionRequest request) {
        if(context != null) {
            Intent allowedNotification = createNotificationIntent(request);
            allowedNotification.putExtra(KEY_POLICY_RESULT, POLICY_ALLOWED);
            allowedNotification.putExtra(KEY_POLICY_SOURCE, source);

            context.sendBroadcast(allowedNotification);
        }
    }

    /**
     * Send a broadcast to the notification service to push a notification that this
     * permission request was denied.
     *
     * @param context the Context
     * @param source where the in the policy enforcement process the decision was made
     * @param request the permission request that was subject to policy enforcement
     * */
    public static void sendDeniedNotification(Context context,
                                              CharSequence source,
                                              PermissionRequest request) {
        if(context != null) {
            Intent deniedNotification = createNotificationIntent(request);
            deniedNotification.putExtra(KEY_POLICY_RESULT, POLICY_DENIED);
            deniedNotification.putExtra(KEY_POLICY_SOURCE, source);

            context.sendBroadcast(deniedNotification);
        }
    }

    private static Intent createNotificationIntent(final PermissionRequest request) {
        Intent notificationIntent = new Intent(DATA_ACCESSED);
        notificationIntent.putExtra(KEY_PACKAGE_NAME, request.packageName);
        notificationIntent.putExtra(KEY_PERMISSION_NAME, request.permission);
        notificationIntent.putExtra(KEY_PURPOSE, request.purpose);

        return notificationIntent;
    }
}