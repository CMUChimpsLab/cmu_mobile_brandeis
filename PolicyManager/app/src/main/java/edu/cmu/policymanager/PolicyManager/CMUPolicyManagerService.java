package edu.cmu.policymanager.PolicyManager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.ResultReceiver;
import android.util.Log;
import android.app.policy.PolicyManagerService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyEnforcement;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyProfileCheck;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyStub;
import edu.cmu.policymanager.PolicyManager.enforcement.QuickSettingCheck;
import edu.cmu.policymanager.PolicyManager.enforcement.StackTraceAnalysis;
import edu.cmu.policymanager.PolicyManager.enforcement.UserSettingCheck;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.policies.OffDevicePolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.peandroid.PEAndroid;
import edu.cmu.policymanager.ui.notification.PolicyManagerNotificationService;
import edu.cmu.policymanager.ui.phonespies.PhoneSpyNotification;
import edu.cmu.policymanager.ui.phonespies.SpyApps;
import edu.cmu.policymanager.util.PolicyManagerDebug;

import static edu.cmu.policymanager.PolicyManager.enforcement.QuickSettingCheck.quickSettings;

public class CMUPolicyManagerService extends PolicyManagerService {
    private final Context mServiceContext = this;

    public static final String POLICY_PROFILE_ADDED = "profile_added",
                               POLICY_PROFILE_NAME = "profile_name";

    private NotificationManager mManager;
    private NotificationChannel mChannel;
    private final int mSpyNotificationID = 3;

    private final Consumer<Void> mLaunchInstallUI = new Consumer<Void>() {
        @Override
        public void accept(Void aVoid) {
            PolicyManagerApplication.ui.launchInstallUI(mServiceContext);
        }
    };

    private Function<String, String> resolveODP(final String packageName) {
        return new Function<String, String>() {
            @Override
            public String apply(String odpString) {
                if(odpString == null) {
                    odpString = getOffDevicePolicy(packageName);

                    if(odpString == null) {
                        odpString =
                                OffDevicePolicy.createODPStringFromManifest(
                                        packageName,
                                        mServiceContext
                                );
                    }
                }

                return odpString;
            }
        };
    }

    private Function<String, CompletableFuture<Void>> logInstallInfo(final String packageName) {
        return new Function<String, CompletableFuture<Void>>() {
            @Override
            public CompletableFuture<Void> apply(String odpString) {
                return DataRepository.fromDisk().logInstallInfo(packageName, odpString);
            }
        };
    }

    BroadcastReceiver appInstalledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String packageName = intent.getDataString().substring(8);
            boolean isNotCriticalApp = !CriticalSystemApps.packageIsSystemApp(packageName);

            if(isNotCriticalApp) {
                DataRepository.fromDisk()
                              .getODPForPackage(packageName)
                              .thenApplyAsync(resolveODP(packageName))
                              .thenComposeAsync(logInstallInfo(packageName))
                              .thenAccept(mLaunchInstallUI);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mManager = getSystemService(NotificationManager.class);
        mChannel = new NotificationChannel(PhoneSpyNotification.SPY_CHANNEL_ID,
                                          "spy",
                                           NotificationManager.IMPORTANCE_HIGH);

        mManager.createNotificationChannel(mChannel);

        addQuickSettings();

        IntentFilter filter = new IntentFilter();
        filter.addAction(POLICY_PROFILE_ADDED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPolicyProfileListener, filter);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");

        registerReceiver(appInstalledReceiver, intentFilter);

        Intent notificationService = new Intent(
                mServiceContext,
                PolicyManagerNotificationService.class
        );

        startService(notificationService);

        Log.d("Policy Manager", "Policy manager is running");
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(appInstalledReceiver);
    }

    @Override
    public boolean onAppInstall(final String packageName, final String odp) {
        Log.d("cmu-debug", "onAppInstalled has been called for " + packageName);
        return true;
    }

    public void onPrivateDataRequest(String packageName,
                                     String permission,
                                     String purpose,
                                     String pal,
                                     String description,
                                     ResultReceiver recv) {
        Log.d("pal-dbg", "micro PAL request from " + packageName + ":" + pal +
                                   " requesting " + permission + " for " + purpose +
                                   " because " + description);

        try {
            PermissionRequest request =
                    PermissionRequest.builder(mServiceContext)
                                     .setPackageName(packageName)
                                     .setPermission(permission)
                                     .setPurpose(Purposes.convertFromMicroPal(purpose))
                                     .setResultReceiver(recv)
                                     .setPalModule(pal)
                                     .setPalRequestDescription(description)
                                     .setLibrary(ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE)
                                     .build();

            PolicyEnforcement palPolicyEnforcementAlgorithm = new PolicyProfileCheck(
                    new QuickSettingCheck(
                            new UserSettingCheck(
                                    new PolicyStub(request)
                            )
                    )
            );

            palPolicyEnforcementAlgorithm.isAllowed();
        } catch(Exception e) {
            PolicyManagerDebug.logException(e);
            PEAndroid.connectToReceiver(recv).denyPermission();
        }
    }

    @Override
    public void onDangerousPermissionRequest(String packageName,
                                             String permission,
                                             String purpose,
                                             List<StackTraceElement[]> stacktraces,
                                             int flags,
                                             ComponentName callingComponent,
                                             ComponentName topActivity,
                                             ResultReceiver recv) {
        PolicyManagerDebug.debugOnDangerousPermissionRequest(packageName, permission, purpose);
        PolicyManagerDebug.debugMainThreadStacktraces(stacktraces);

        boolean isSystemApp = ((flags & PolicyManagerService.FROM_SYS_APP_REQ) != 0 ||
                              (flags & PolicyManagerService.FROM_PRIV_APP_REQ) != 0 ||
                              (flags & PolicyManagerService.FROM_ANDROID_REQ) != 0)
                              || CriticalSystemApps.packageIsSystemApp(packageName);

        if(isSystemApp) {
            PEAndroid.connectToReceiver(recv).allowPermission();
            return;
        } else if(appIsTryingToLoad(permission, stacktraces)) {
            PEAndroid.connectToReceiver(recv).allowPermission();
            return;
        }

        PolicyManagerDebug.debugWithMessage("Starting policy enforcement algorithm.");

        try {
            if(SpyApps.appIsSpying(packageName)) {
                PhoneSpyNotification notification =
                        new PhoneSpyNotification(mServiceContext, packageName);

                if(mManager != null) {
                    mManager.notify(mSpyNotificationID, notification.create());
                }
            }

            PermissionRequest request = PermissionRequest.builder(mServiceContext)
                                                         .setPackageName(packageName)
                                                         .setPermission(permission)
                                                         .setStacktraces(stacktraces)
                                                         .setPurpose(purpose)
                                                         .setResultReceiver(recv)
                                                         .setTopActivity(topActivity)
                                                         .build();

            PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(request);

            PolicyEnforcement policyEnforcementAlgorithm = new PolicyProfileCheck(
                    new QuickSettingCheck(
                            new UserSettingCheck(
                                    new PolicyStub(analyzedRequest)
                            )
                    )
            );

            policyEnforcementAlgorithm.isAllowed();
        } catch(Exception e) {
            System.out.println("We got some error and are unable to handle this request - denying");
            PolicyManagerDebug.logException(e);
            PEAndroid.connectToReceiver(recv).denyPermission();
        }
    }

    private boolean appIsTryingToLoad(CharSequence permission,
                                      List<StackTraceElement[]> stacktraces) {
        String permissionString = permission.toString();

        return ((permissionString.equalsIgnoreCase(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                 permissionString.equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE)) &&
                (stacktraces == null || stacktraces.isEmpty()));
    }

    private Drawable getIcon(int resourceId) {
        return getResources().getDrawable(resourceId);
    }

    private void addQuickSettings() {
        addPrivacyQuickSetting("policymanager.cmu.camera",
                "Camera",
                getIcon(R.drawable.camera),
                getIcon(R.drawable.camera),
                new PolicyManagerService.PrivacySettingListener() {
                    @Override
                    public void onSettingChanged(String s, boolean b) {
                        quickSettings.put(Manifest.permission.CAMERA, b);
                    }
                }
        );

        addPrivacyQuickSetting("policymanager.cmu.location",
                "Location",
                getIcon(R.drawable.location),
                getIcon(R.drawable.location),
                new PolicyManagerService.PrivacySettingListener() {
                    @Override
                    public void onSettingChanged(String s, boolean b) {
                        quickSettings.put(Manifest.permission.ACCESS_FINE_LOCATION, b);
                        quickSettings.put(Manifest.permission.ACCESS_COARSE_LOCATION, b);
                    }
                });

        addPrivacyQuickSetting("policymanager.cmu.mic",
                "Microphone",
                getIcon(R.drawable.microphone),
                getIcon(R.drawable.microphone),
                new PolicyManagerService.PrivacySettingListener() {
                    @Override
                    public void onSettingChanged(String s, boolean b) {
                        quickSettings.put(Manifest.permission.RECORD_AUDIO, b);
                    }
                });
    }

    public void addProfileQuickSetting(CharSequence profile) {
        final String profileString = profile.toString();

        addPrivacyQuickSetting("policymanager.cmu." + profileString,
                profileString,
                getIcon(R.drawable.privacy_profile),
                getIcon(R.drawable.privacy_profile),
                new PrivacySettingListener() {
                    @Override
                    public void onSettingChanged(String s, boolean b) {
                        if(b) {
                            PolicyManager.getInstance().activatePolicyProfile(profileString);
                        }
                        else {
                            PolicyManager.getInstance()
                                         .activatePolicyProfile(PolicyProfile.DEFAULT);
                        }
                    }
                });
    }

    private BroadcastReceiver mPolicyProfileListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String profileName = intent.getStringExtra(POLICY_PROFILE_NAME);

            switch(action) {
                case POLICY_PROFILE_ADDED: {
                    addProfileQuickSetting(profileName);
                }
            }
        }
    };
}