package edu.cmu.policymanager.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.AppDatabaseSingleton;
import edu.cmu.policymanager.DataRepository.db.model.AppInfo;
import edu.cmu.policymanager.DataRepository.db.model.Metadata;
import edu.cmu.policymanager.DataRepository.db.model.PermissionInfo;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfileSetting;
import edu.cmu.policymanager.DataRepository.db.model.PolicySetting;
import edu.cmu.policymanager.DataRepository.db.model.PurposeInfo;
import edu.cmu.policymanager.DataRepository.db.model.ThirdPartyLibInfo;
import edu.cmu.policymanager.PolicyManager.CriticalSystemApps;
import edu.cmu.policymanager.PolicyManager.Util;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;

/**
 * Used to be for initializing the policy manager, and integrating with PrivacyGrade.
 *
 * Created by adi on 3/15/17.
 */

public class FetchUtil {

    private static final String TAG = "FetchUtil";

    public static ArrayList<AppInfo> getInstalledApps(Context context, boolean getSysPackages) {
        ArrayList<AppInfo> all = new ArrayList<AppInfo>();
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        Set<String> uniquePermissions = new HashSet<String>();

        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            String appName = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
            String packageName = p.packageName;
            String versionName = p.versionName;
            int versionCode = p.versionCode;
            int category = 0;

            if(CriticalSystemApps.packageIsSystemApp(packageName)) {
                continue;
            }

            AppInfo appInfoDb = new AppInfo();

            appInfoDb.appName = appName;
            appInfoDb.packageName = packageName;
            appInfoDb.versionCode = versionCode;
            appInfoDb.versionName = versionName;
            appInfoDb.category = category;

            /*
            if (AppDatabaseSingleton.getDB().appInfoDao().hasAppWithVersion(appInfoDb.packageName,
                                                                            appInfoDb.versionCode)){
                //Mike - for now lets just exit if we see the db is already initialized
                return null;
            }*/

            // check to see if there are any dangerous permissions ('sensitivity >= 2'). if not, don't add app to DB
            boolean shouldAddHasDangerous = false;

            ArrayList<PermissionInfo> permissionsOfThisApp = getGrantedPermissions(context, packageName);
            List<PermissionInfo> permInfosDb = new LinkedList<>();
            for (PermissionInfo info : permissionsOfThisApp){
                PermissionInfo permInfo = new PermissionInfo();
                permInfo.permissionName = info.permissionName;
                permInfo.permissionDescription = info.permissionDescription;
                permInfo.sensitivity = PermissionUtil.getPermissionSensitivity(info.permissionName);
                if (permInfo.sensitivity.equals("2")) {
                    Log.v(TAG, "inserting permission (sensitivity: " + permInfo.sensitivity +
                                    "): " + info.permissionName + " for app: " +
                                    appInfoDb.packageName);
                    permInfosDb.add(permInfo);
                    uniquePermissions.add(permInfo.permissionName);
                    shouldAddHasDangerous = true;

                    PolicyProfileSetting setting = new PolicyProfileSetting();
                    setting.app = appInfoDb.packageName;
                    setting.permission = permInfo.permissionName;
                    setting.purpose = Purposes.RUNNING_OTHER_FEATURES.name.toString();
                    setting.thirdPartyLibrary =
                            ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE.category;
                    setting.profileName = PolicyProfile.DEFAULT;
                    setting.policyAction = UserPolicy.Policy.ALLOW.name();

                    try {
                        DangerousPermissions.from(setting.permission);

                        AppDatabaseSingleton.getDB(DataRepository.StorageType.DISK)
                                            .policyProfileSettingDAO()
                                            .insert(setting);
                    } catch(IllegalArgumentException ex) {
                        /*
                         * Do not actually need to do anything here, as the permission
                         * is not dangerous. This logic is very old, but not enough time to
                         * refactor all of this.
                         * */
                    }
                }
            }

            if(!shouldAddHasDangerous) {
                Log.i(TAG, "Not adding app: " + packageName +" because there aren't any dangerous permissions that it uses");
                continue;
            }

            long appId = AppDatabaseSingleton.getDB(DataRepository.StorageType.DISK)
                                             .appInfoDao()
                                             .insert(appInfoDb);

            appInfoDb.id = appId;

            AppDatabaseSingleton.getDB(DataRepository.StorageType.DISK).permissionInfoDao().insertPermissionsForApp(appInfoDb, permInfosDb);
        }
        Collections.sort(all, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                return lhs.appName.compareTo(rhs.appName);
            }
        });

        Metadata[] metadataList = new Metadata[uniquePermissions.size()];
        int i = 0;

        for(String permission : uniquePermissions) {
            String jsonString = "{ \"accessCount\" : \"0\" }";
            Metadata metadata = new Metadata();
            metadata.dataOwner = permission;
            metadata.metadataJson = jsonString;
            metadataList[i++] = metadata;
        }

        AppDatabaseSingleton.getDB(DataRepository.StorageType.DISK).metadataDao().insert(metadataList);

        return all;
    }

    public static ArrayList<PermissionInfo> getGrantedPermissions(Context context, String appPackage) {
        ArrayList<PermissionInfo> granted = new ArrayList<PermissionInfo>();
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(appPackage, PackageManager.GET_PERMISSIONS);
            Log.v(TAG, "Getting permissions for app: " + pi.packageName);
            if (pi.requestedPermissions != null) {
                for (int i = 0; i < pi.requestedPermissions.length; i++) {
                    PermissionInfo permissionInfo = new PermissionInfo();
                    Log.v(TAG, "App: " + appPackage + " permission: " + pi.requestedPermissions[i]);

                    permissionInfo.permissionName = pi.requestedPermissions[i];

                    try{
                        permissionInfo.protectionLevel = context.getPackageManager().getPermissionInfo(pi.requestedPermissions[i], PackageManager.GET_META_DATA).protectionLevel;
                    }
                    catch (PackageManager.NameNotFoundException e){
                        Log.w(TAG, "PE android Package Manager doesn't know the permission level for: " + pi.requestedPermissions[i]);
                        permissionInfo.protectionLevel = 0;
                    }
                    CharSequence description = null;
                    try {
                        description = context.getPackageManager().getPermissionInfo(pi.requestedPermissions[i], PackageManager.GET_META_DATA).loadDescription(context.getPackageManager());
                    }
                    catch (PackageManager.NameNotFoundException e){
                        Log.w(TAG, "PE android Package Manager doesn't know the permission description: " + pi.requestedPermissions[i]);
                    }
                    if (description == null) {
                        permissionInfo.permissionDescription = "No description available for: " +
                                                               permissionInfo.permissionName;
                    }
                    else {
                        permissionInfo.permissionDescription = String.valueOf(description);
                    }

                    String permissionName = permissionInfo.permissionName;

                    if (permissionName != null) {
                        granted.add(permissionInfo);
                    }
                }
            }

            Collections.sort(granted, new Comparator<PermissionInfo>() {
                @Override
                public int compare(PermissionInfo lhs, PermissionInfo rhs) {
                    return lhs.permissionName.compareTo(rhs.permissionName);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return granted;
    }

    public static long insertAppIntoDB(Context context, String packageNameArg) {
        if(!DataRepository.getInstance().isAppInstalled(packageNameArg)) {
            PackageManager pm = context.getPackageManager();

            PackageInfo p = null;
            try {
                p = pm.getPackageInfo(packageNameArg, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }

            if( p == null) {
                Log.d(TAG, "PackageInfo is null");
            }

            if(p != null) {
                String appName = p.applicationInfo.loadLabel(pm).toString();
                String packageName = p.packageName;
                int versionCode = p.versionCode;
                long firstInstallTime = p.firstInstallTime;
                long lastUpdatetime = p.lastUpdateTime;

                AppInfo appInfoDb = new AppInfo();

                appInfoDb.appName = appName;
                appInfoDb.packageName = packageName;
                appInfoDb.versionCode = versionCode;
                appInfoDb.firstInstallTime = firstInstallTime;
                appInfoDb.lastUpdateTime = lastUpdatetime;
                appInfoDb.category = 0;

                if(packageName.contains("com.android")) {
                    appInfoDb.category = PolicyManagerApplication.CATEGORY_NATIVE;
                }

                if (AppDatabaseSingleton.getDB(DataRepository.StorageType.DISK).appInfoDao().hasAppWithVersion(appInfoDb.packageName,
                        appInfoDb.versionCode)) {
                    Log.d(TAG, "App already in database: " + appInfoDb.packageName);
                    return -1;
                }

                return AppDatabaseSingleton.getDB(DataRepository.StorageType.DISK).appInfoDao().insert(appInfoDb);
            }
        }

        return -1;
    }
}