package edu.cmu.policymanager.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.MainActivity;

import static edu.cmu.policymanager.PolicyManager.PolicyNotification.KEY_PACKAGE_NAME;
import static edu.cmu.policymanager.PolicyManager.PolicyNotification.KEY_PERMISSION_NAME;
import static edu.cmu.policymanager.PolicyManager.PolicyNotification.KEY_POLICY_SOURCE;
import static edu.cmu.policymanager.PolicyManager.PolicyNotification.KEY_PURPOSE;
import static edu.cmu.policymanager.PolicyManager.PolicyNotification.POLICY_ALLOWED;

/**
 * Notifies the user of some policy action that was enforced by the policy manager. All
 * policy decisions that are made by the policy manager will send a broadcast indicating
 * whether a data access was allowed or denied, and by what (user setting, quick setting etc).
 *
 * Notifications go live whenever the CMU policy manager is selected by PE for Android.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PolicyManagerNotificationService extends Service {
    private static boolean sIsRunning = false;
    public static final String CHANNEL_ID = "edu.cmu.policymanager.notifications",
                               DATA_ACCESSED = "edu.cmu.policymanager.action.DATA_ACCESSED",
                               NOTIFICATION_GROUP_ID = "policymanager.all.notifications",
                               KEY_POLICY_RESULT = "policyResult";

    private static final int DEFAULT_NOTIFICATION_ID = 1;

    private final Context mServiceContext = this;

    private NotificationManager mManager;

    private Map<String, Integer> mNotificationIDs = new HashMap<String, Integer>();

    public void onCreate() { }

    public void onDestroy() {
        sIsRunning = false;
    }

    public int onStartCommand(Intent intent,
                              int flags,
                              int startId) {
        if(!sIsRunning) {
            super.onStartCommand(intent, flags, startId);
            Notification.Builder notificationBuilder = null;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "notifications",
                    NotificationManager.IMPORTANCE_LOW
            );

            mManager = getSystemService(NotificationManager.class);

            if(mManager != null) {
                mManager.createNotificationChannel(channel);
                notificationBuilder = new Notification.Builder(mServiceContext, CHANNEL_ID);
            }

            if(notificationBuilder != null) {
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                                   .setContentTitle(
                                           getString(
                                                   R.string.sensitive_data_access_feed
                                           )
                                   )
                                   .setStyle(new Notification.InboxStyle())
                                   .setGroup(NOTIFICATION_GROUP_ID)
                                   .setContentText(getString(R.string.notification_main_text))
                                   .setGroupSummary(true)
                                   .setColor(0x4797ff)
                                   .setContentIntent(createHomescreenPendingIntent())
                                   .build();

                startForeground(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());

                IntentFilter filter = new IntentFilter(DATA_ACCESSED);
                registerReceiver(peAndroidUpdate, filter);
            } else {
                Log.d(
                        "NotificationService",
                        "Unable to get notification manager for policy manager apps " +
                              "foreground service"
                );
            }
        }

        sIsRunning = true;
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent intent) { return null; }

    private final BroadcastReceiver peAndroidUpdate = new BroadcastReceiver() {
        @Override public void onReceive(Context context,
                                        Intent intent) {
            final String packageName = intent.getStringExtra(KEY_PACKAGE_NAME),
                         policySource = intent.getStringExtra(KEY_POLICY_SOURCE);

            final SensitiveData permission = intent.getParcelableExtra(KEY_PERMISSION_NAME);
            final Purpose purpose  = intent.getParcelableExtra(KEY_PURPOSE);

            String allowedOrDenied = intent.getStringExtra(KEY_POLICY_RESULT);

            boolean permitted = allowedOrDenied.equalsIgnoreCase(POLICY_ALLOWED);

            PolicyNotification notification = PolicyNotification.create(context)
                                                                .forPackage(packageName)
                                                                .withPermission(permission)
                                                                .from(policySource)
                                                                .setPurpose(purpose)
                                                                .isPermitted(permitted);

            if (mManager != null) {
                Random r = new Random();
                int notificationId = r.nextInt() + 1;
                String idKey = permission.androidPermission.toString() + purpose;

                if(mNotificationIDs.containsKey(idKey)) {
                    notificationId = mNotificationIDs.get(idKey);
                } else {
                    mNotificationIDs.put(idKey, notificationId);
                }

                Notification androidNotification = notification.buildAndroidNotification();
                mManager.notify(notificationId, androidNotification);
            }
        }
    };

    private PendingIntent createHomescreenPendingIntent() {
        Intent activityIntent = new Intent(mServiceContext, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(
                mServiceContext,
                1337,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}