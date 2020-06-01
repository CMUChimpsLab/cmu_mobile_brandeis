package edu.cmu.policymanager.DataRepository;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import edu.cmu.policymanager.DataRepository.db.AppDatabaseSingleton;
import edu.cmu.policymanager.DataRepository.db.dao.AppInfoDAO;
import edu.cmu.policymanager.DataRepository.db.dao.AskPolicySettingDAO;
import edu.cmu.policymanager.DataRepository.db.dao.MetadataDAO;
import edu.cmu.policymanager.DataRepository.db.dao.OffDevicePolicyDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PermissionInfoDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PolicyProfileDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PolicyProfileSettingDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PurposeInfoDAO;
import edu.cmu.policymanager.DataRepository.db.dao.ThirdPartyLibDAO;
import edu.cmu.policymanager.DataRepository.db.model.AppInfo;
import edu.cmu.policymanager.DataRepository.db.model.AskPolicySetting;
import edu.cmu.policymanager.DataRepository.db.model.Metadata;
import edu.cmu.policymanager.DataRepository.db.model.OffDevicePolicyDBModel;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfileSetting;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.DataRepository.memory.MemoryDataStore;
import edu.cmu.policymanager.DataRepository.network.BrandeisBackend;
import edu.cmu.policymanager.PolicyManager.CriticalSystemApps;
import edu.cmu.policymanager.PolicyManager.Util;
import edu.cmu.policymanager.PolicyManager.enforcement.QuickSettingCheck;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.policies.OffDevicePolicy;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveDataGroup;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.util.FetchUtil;
import edu.cmu.policymanager.validation.Precondition;
import edu.cmu.policymanager.viewmodel.W4PData;
import edu.cmu.policymanager.viewmodel.W4PGraph;

/**
 * The data repository for the CMU Brandeis policy manager. All storage
 * operations go through the DataRepository. The DataRepository has two modes:
 * - In-Memory which is used strictly for automated unit testing
 * - Disk (or whatever persistent storage mechanism) which should only be used for non-testing
 *
 * The default mode of the DataRepository is to use persistent storage.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 3/16/2018.
 */

public class DataRepository {
    public enum StorageType {
        IN_MEMORY,
        DISK
    }

    private static StorageType storageMode = StorageType.DISK;

    private static DataRepository repository;

    private static AppInfoDAO appDAO;
    private static PolicyProfileSettingDAO policyDAO;
    private static MetadataDAO metadataDAO;
    private static BrandeisBackend backendServer;
    private static PolicyProfileDAO policyProfileDAO;
    private static AskPolicySettingDAO askPolicySettingDAO;
    private static OffDevicePolicyDAO offDevicePolicyDAO;

    private List<String> profilesAdded;

    private Context context;

    private static PolicyProfile activePolicyProfile;

    private DataRepository(final Context context) {
        this.context = context;
        AppDatabaseSingleton.createDatabases(context);
        backendServer = new BrandeisBackend();
        profilesAdded = new ArrayList<String>(5);
    }

    private void setStorageType(final StorageType type) {
        storageMode = type;

        appDAO = AppDatabaseSingleton.getDB(type).appInfoDao();
        policyDAO = AppDatabaseSingleton.getDB(type).policyProfileSettingDAO();
        metadataDAO = AppDatabaseSingleton.getDB(type).metadataDao();
        policyProfileDAO = AppDatabaseSingleton.getDB(type).policyProfileDAO();
        askPolicySettingDAO = AppDatabaseSingleton.getDB(type).askPolicySettingDAO();
        offDevicePolicyDAO = AppDatabaseSingleton.getDB(type).odpDAO();
    }

    /**
     * Initialize the DataRepository
     *
     * @param context the Context
     * @param storageType the storage mode you want to initalize the DataRepository with
     * */
    public static void init(final Context context,
                            final StorageType storageType) {
        Thread dbInit = new Thread() {
            public void run() {
                repository = new DataRepository(context);
                repository.setStorageType(storageType);

                List<AppInfo> apps = AppDatabaseSingleton.getDB(storageType).appInfoDao().getAll();

                if(apps == null || apps.isEmpty()) {
                    FetchUtil.getInstalledApps(context, false);
                }

                List<UserPolicy> globalSettings =
                        repository.getAllGlobalSettings(PolicyProfile.DEFAULT);

                if(globalSettings == null || globalSettings.isEmpty()) {
                    installGlobalSettings();
                }

                PolicyProfile profile = policyProfileDAO.getActiveProfile();

                if(profile == null) {
                    profile = new PolicyProfile();
                    profile.profileName = PolicyProfile.DEFAULT;
                    profile.isActive = true;

                    policyProfileDAO.insert(profile);
                }

                activePolicyProfile = profile;
            }
        };

        dbInit.start();

        try {
            dbInit.join();
        }
        catch(InterruptedException ie) {
            Log.d("DataRepository", "Unable to init database - interrupted");
            Log.d("DataRepository", ie.getMessage());
        }
    }

    private static void installGlobalSettings() {
        List<PolicyProfileSetting> settings = new LinkedList<>();

        for(SensitiveDataGroup group : DangerousPermissions.ALL_SENSITIVE_DATA_GROUPS) {
            for(SensitiveData permission : group.permissionsInGroup) {
                for(Purpose purpose : permission.purposes) {
                    PolicyProfileSetting setting = new PolicyProfileSetting();
                    setting.profileName = PolicyProfile.DEFAULT;
                    setting.app = PolicyManagerApplication.SYMBOL_ALL;
                    setting.permission = permission.androidPermission.toString();
                    setting.policyAction = UserPolicy.Policy.ALLOW.name();
                    setting.purpose = purpose.name.toString();
                    setting.thirdPartyLibrary = PolicyManagerApplication.SYMBOL_ALL;

                    settings.add(setting);
                }
            }
        }

        PolicyProfileSetting[] settingArray = new PolicyProfileSetting[settings.size()];

        for(int i = 0; i < settingArray.length; i++) {
            settingArray[i] = settings.get(i);
        }

        policyDAO.insert(settingArray);
    }

    /**
     * Get a 'disk' or persistent storage mode of the DataRepository. The DataRepository
     * must be initialized before calling this method as it is a form of getInstance.
     *
     * @return the DataRepository
     * */
    public static DataRepository fromDisk() {
        if(repository == null) {
            throw new IllegalStateException("Must initialize the repository first");
        }

        repository.setStorageType(StorageType.DISK);
        return repository;
    }

    /**
     * Get a in-memory storage mode of the DataRepository. The DataRepository
     * must be initialized before calling this method as it is a form of getInstance.
     *
     * @return the DataRepository
     * */
    public static DataRepository fromMemory() {
        Precondition.checkState(repository != null, "Must initialize repo");

        repository.setStorageType(StorageType.IN_MEMORY);
        return repository;
    }

    /**
     * Get the current instance of DataRepository. Returns the disk mode version by default.
     *
     * @return the DataRepository
     * */
    public static DataRepository getInstance() {
        if(storageMode == StorageType.DISK) {
            return fromDisk();
        }

        if(storageMode == StorageType.IN_MEMORY) {
            return fromMemory();
        }

        return fromDisk();
    }

    public static Map<String, String[]> getPermissionMap() {
        return MemoryDataStore.permissionMap;
    }

    public static String activeProfileName() {
        return activePolicyProfile.profileName;
    }

    /**
     * Data structure that contains relevant data when an app is first installed:
     * package name and the off-device policy JSON string.
     * */
    public static class InstallInfo {
        public String packageName;
        public OffDevicePolicy odp;
    }

    private static InstallInfo install = new InstallInfo();

    /**
     * Installs the app synchronously (intended for automated unit tests).
     *
     * @param packageName the package name of the app to install
     * @param odp the off-device policy JSON String
     * */
    public void syncLogInstallInfo(CharSequence packageName,
                                   String odp) {
        Precondition.checkEmptyCharSequence(packageName);

        install.packageName = packageName.toString();
        final Set<PolicyProfileSetting> appPermissionPurposes =
                new HashSet<PolicyProfileSetting>();

        try {
            install.odp = new OffDevicePolicy(odp);

            Map<String, PolicyProfileSetting> appPermissionGlobalSettings =
                    new HashMap<String, PolicyProfileSetting>();

            for (final OffDevicePolicy.SubPolicy policy : install.odp.SUBPOLICIES) {
                PolicyProfileSetting setting = new PolicyProfileSetting();
                setting.profileName = PolicyProfile.DEFAULT;
                setting.app = packageName.toString();
                setting.permission = policy.permission;
                setting.purpose = policy.purpose;

                ThirdPartyLibrary lib =
                        ThirdPartyLibraries.getLibraryByOffendingClass(policy.className);

                if (lib != null) {
                    setting.thirdPartyLibrary = lib.qualifiedName;
                } else {
                    setting.thirdPartyLibrary = ThirdPartyLibraries.APP_INTERNAL_USE;
                }

                setting.policyAction = UserPolicy.Policy.ALLOW.name();

                PolicyProfileSetting masterSetting = copyProfileSetting(setting);
                masterSetting.purpose = PolicyManagerApplication.SYMBOL_ALL;

                if(lib != null) {
                    masterSetting.thirdPartyLibrary = ThirdPartyLibraries.THIRD_PARTY_USE;
                } else {
                    masterSetting.thirdPartyLibrary = ThirdPartyLibraries.APP_INTERNAL_USE;
                }

                if(!appPermissionGlobalSettings.containsKey(
                        masterSetting.permission + masterSetting.purpose
                )) {
                    appPermissionPurposes.add(masterSetting);
                    appPermissionGlobalSettings.put(masterSetting.permission, masterSetting);
                }

                appPermissionPurposes.add(setting);
            }
        } catch(JSONException jse) {
            jse.printStackTrace();
        }

        if(!DataRepository.getInstance().isAppInstalled(packageName.toString())) {
            FetchUtil.insertAppIntoDB(context, install.packageName);

            OffDevicePolicyDBModel odpDB = new OffDevicePolicyDBModel();
            odpDB.packageName = install.packageName;
            odpDB.odp = odp;
            offDevicePolicyDAO.insert(odpDB);

            for (final PolicyProfileSetting setting : appPermissionPurposes) {
                setting.profileName = PolicyProfile.DEFAULT;
                policyDAO.insert(setting);
            }
        }
    }

    private PolicyProfileSetting copyProfileSetting(PolicyProfileSetting setting) {
        PolicyProfileSetting copiedSetting = new PolicyProfileSetting();
        copiedSetting.app = setting.app;
        copiedSetting.permission = setting.permission;
        copiedSetting.purpose = setting.purpose;
        copiedSetting.thirdPartyLibrary = setting.thirdPartyLibrary;
        copiedSetting.lastUpdated = setting.lastUpdated;
        copiedSetting.policyAction = setting.policyAction;

        return copiedSetting;
    }

    /**
     * Asynchronously logs install information for this app. This allows the broadcast receivers
     * to quickly return, especially when in the onAppInstall PE for Android hook.
     *
     * @param packageName the package name of the app being installed
     * @param odp the off-device policy JSON String
     * */
    public CompletableFuture<Void> logInstallInfo(final CharSequence packageName,
                                                  final String odp) {
        return CompletableFuture.supplyAsync(new Supplier<Void>() {
            @Override
            public Void get() {
                install.packageName = packageName.toString();
                final Set<PolicyProfileSetting> appPermissionPurposes =
                        new HashSet<PolicyProfileSetting>();

                try {
                    install.odp = new OffDevicePolicy(odp);

                    Map<String, PolicyProfileSetting> appPermissionGlobalSettings =
                            new HashMap<String, PolicyProfileSetting>();

                    for (final OffDevicePolicy.SubPolicy policy : install.odp.SUBPOLICIES) {
                        PolicyProfileSetting setting = new PolicyProfileSetting();
                        setting.profileName = PolicyProfile.DEFAULT;
                        setting.app = packageName.toString();
                        setting.permission = policy.permission;
                        setting.purpose = policy.purpose;

                        ThirdPartyLibrary lib =
                                ThirdPartyLibraries.getLibraryByOffendingClass(policy.className);

                        if (lib != null) {
                            setting.thirdPartyLibrary = lib.qualifiedName;
                        } else {
                            setting.thirdPartyLibrary = ThirdPartyLibraries.APP_INTERNAL_USE;
                        }

                        setting.policyAction = UserPolicy.Policy.ALLOW.name();

                        PolicyProfileSetting masterSetting = copyProfileSetting(setting);
                        masterSetting.purpose = PolicyManagerApplication.SYMBOL_ALL;

                        if(lib != null) {
                            masterSetting.thirdPartyLibrary = ThirdPartyLibraries.THIRD_PARTY_USE;
                        } else {
                            masterSetting.thirdPartyLibrary = ThirdPartyLibraries.APP_INTERNAL_USE;
                        }

                        if(!appPermissionGlobalSettings.containsKey(
                                masterSetting.permission + masterSetting.purpose
                        )) {
                            appPermissionPurposes.add(masterSetting);
                            appPermissionGlobalSettings.put(
                                    masterSetting.permission, masterSetting
                            );
                        }

                        appPermissionPurposes.add(setting);
                    }
                } catch(JSONException jse) {
                    jse.printStackTrace();
                }

                if(!DataRepository.getInstance().isAppInstalled(packageName.toString())) {
                    FetchUtil.insertAppIntoDB(context, install.packageName);

                    if(offDevicePolicyDAO.getPolicy(packageName.toString()) == null) {
                        OffDevicePolicyDBModel dbModel = new OffDevicePolicyDBModel();
                        dbModel.packageName = install.packageName;
                        dbModel.odp = odp;
                        offDevicePolicyDAO.insert(dbModel);
                    }

                    for (final PolicyProfileSetting setting : appPermissionPurposes) {
                        setting.profileName = PolicyProfile.DEFAULT;
                        policyDAO.insert(setting);
                    }
                }

                return null;
            }
        });
    }

    /**
     * Adds an off-device policy JSON string to the DataRepository. This action is completed
     * in a separate thread.
     *
     * @param packageName the package name of the app to add an off-device policy for
     * @param odpString the off-device policy JSON String
     * */
    public void addODP(final CharSequence packageName,
                       final String odpString) {
        Precondition.checkEmptyCharSequence(packageName);

        (new Thread() {
            public void run() {
                OffDevicePolicyDBModel odpDB = new OffDevicePolicyDBModel();
                odpDB.packageName = packageName.toString();
                odpDB.odp = odpString;
                offDevicePolicyDAO.insert(odpDB);
            }
        }).start();
    }

    /**
     * Asynchronously get the off-device policy JSON string that belongs to an app.
     *
     * @param packageName the package name of the app to add an off-device policy for
     * @return CompletableFuture that will return the JSON String
     * */
    public CompletableFuture<String> getODPForPackage(final CharSequence packageName) {
        Precondition.checkEmptyCharSequence(packageName);

        return CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                OffDevicePolicyDBModel model = offDevicePolicyDAO.getPolicy(packageName.toString());

                if(model != null) { return model.odp; }

                return null;
            }
        });
    }

    /**
     * Asynchronously get the off-device policy JSON string that belongs to an app.
     *
     * @param packageName the package name of the app to add an off-device policy for
     * @return the JSON String
     * */
    public String syncGetODPForPackage(final CharSequence packageName) {
        Precondition.checkEmptyCharSequence(packageName);

        OffDevicePolicyDBModel model = offDevicePolicyDAO.getPolicy(packageName.toString());

        if(model != null) { return model.odp; }

        return null;
    }

    /**
     * Get the InstallInfo that was logged at some point during the app install process.
     *
     * @return the InstallInfo
     * */
    public static InstallInfo getInstallInfo() { return install; }

    /**
     * Install a policy profile. This is a prototype and so it only will install the sample
     * organizational profile CMU provided. This action is completed in a separate thread.
     *
     * @param profileToInstall the profile to install
     * */
    public void installProfile(final CharSequence profileToInstall) {
        Precondition.checkEmptyCharSequence(profileToInstall);

        (new Thread() {
            public void run() {
                if(profileIsNotInstalled(profileToInstall.toString())) {
                    profilesAdded.add(profileToInstall.toString());
                    PolicyProfile profile = new PolicyProfile();
                    profile.profileName = profileToInstall.toString();
                    profile.isActive = false;

                    policyProfileDAO.insert(profile);

                    List<PolicyProfileSetting> profileSettings =
                            backendServer.getSampleProfileSettings();

                    for(PolicyProfileSetting setting : profileSettings) {
                        policyDAO.insert(setting);
                    }
                }
            }
        }).start();
    }

    public void syncInstallProfile(CharSequence profileToInstall) {
        String profileString = profileToInstall.toString();

        if(profileIsNotInstalled(profileString)) {
            profilesAdded.add(profileString);
            PolicyProfile profile = new PolicyProfile();
            profile.profileName = profileString;
            profile.isActive = false;

            policyProfileDAO.insert(profile);

            List<PolicyProfileSetting> profileSettings =
                    backendServer.getSampleProfileSettings();

            for(PolicyProfileSetting setting : profileSettings) {
                policyDAO.insert(setting);
            }
        }
    }

    /**
     * Synchronously remove the provided UserPolicy from the DataRepository.
     *
     * @param policyToRemove the policy to remove
     * */
    public void syncRemoveAppPolicy(UserPolicy policyToRemove) {
        policyDAO.deleteUserPolicy(policyToRemove.app,
                                   policyToRemove.permission.androidPermission.toString(),
                                   policyToRemove.purpose.name.toString(),
                                   policyToRemove.thirdPartyLibrary.qualifiedName);
    }

    /**
     * Synchronously get the settings for a policy profile.
     *
     * @param profileName the profile to get settings for
     * @return the list of UserPolicys this profile controls
     * */
    public List<UserPolicy> getProfileSettings(CharSequence profileName) {
        List<PolicyProfileSetting> settings =
                policyDAO.getAllProfileSettings(profileName.toString());

        List<UserPolicy> profileSettings = new LinkedList<UserPolicy>();

        for(PolicyProfileSetting setting : settings) {
            profileSettings.add(convertPolicySettingToUserPolicy(setting));
        }

        return profileSettings;
    }

    /**
     * Get profile settings specifically for the sample organizational profile.
     *
     * @return the list of UserPolicys the sample profile uses
     * */
    public List<UserPolicy> previewProfileSettings() {
        List<PolicyProfileSetting> profileSettings =
                                backendServer.getSampleProfileSettings();

        List<UserPolicy> profilePolicies = new LinkedList<UserPolicy>();

        for(PolicyProfileSetting setting : profileSettings) {
            profilePolicies.add(convertPolicySettingToUserPolicy(setting));
        }

        return profilePolicies;
    }

    /**
     * Synchronously checks to see if an app is installed.
     *
     * @param packageName the package name of the app to check
     * @return true if installed, false otherwise
     * */
    public boolean isAppInstalled(CharSequence packageName) {
        Precondition.checkEmptyCharSequence(packageName);

        AppInfo app = appDAO.getAppWithPackageName(packageName.toString());
        return app != null;
    }

    public List<PolicyProfileSetting> requestProfileSettings(final String profileName) {
        if(profileIsInstalled(profileName)) {
            return policyDAO.getAllProfileSettings(profileName);
        }

        return new LinkedList<PolicyProfileSetting>();
    }

    private boolean profileIsInstalled(final String profileName) {
        List<PolicyProfile> installedProfiles = policyProfileDAO.getProfiles();

        for(PolicyProfile installedProfile : installedProfiles) {
            if(installedProfile.profileName.equalsIgnoreCase(profileName)) {
                return true;
            }
        }

        return false;
    }

    private boolean profileIsNotInstalled(final String profileName) {
        return !profileIsInstalled(profileName);
    }

    /**
     * Asynchronously request a list of apps that request the given permission for the
     * specified purpose.
     *
     * @param permission the permission to check for
     * @param purpose the purpose to check for
     * @return CompletableFuture which returns the list of W4PData (apps)
     * */
    public CompletableFuture<List<W4PData>> requestAppsUsingPermissionAndPurpose(
            final SensitiveData permission,
            final Purpose purpose
    ) {
        return CompletableFuture.supplyAsync(
                new Supplier<List<W4PData>>() {
                    @Override
                    public List<W4PData> get() {
                        String permissionName = permission.androidPermission.toString(),
                               purposeName = purpose.name.toString();
                        List<PolicyProfileSetting> apps = policyDAO.getAppsUsingPermissionPurpose(
                                permissionName,
                                purposeName
                        );

                        List<W4PData> filteredApps = new LinkedList<W4PData>();

                        for(PolicyProfileSetting setting : apps) {
                            AppInfo appInfo = new AppInfo();
                            appInfo.packageName = setting.app;
                            appInfo.appName = Util.getAppCommonName(context, setting.app);
                            filteredApps.add(W4PData.createFromApp(appInfo));
                        }

                        return filteredApps;
                    }
                }
        );
    }

    /**
     * Asynchronously request all apps (non-system apps) installed on the device.
     *
     * @return CompletableFuture list of W4PData (apps)
     * */
    public CompletableFuture<List<W4PData>> requestAllApps() {
        return CompletableFuture.supplyAsync(
                new Supplier<List<W4PData>>() {
                    @Override
                    public List<W4PData> get() {
                        List<AppInfo> apps = appDAO.getAll();

                        List<W4PData> requestedApps = new ArrayList<W4PData>(apps.size());

                        for(AppInfo appInfo : apps) {
                            requestedApps.add(W4PData.createFromApp(appInfo));
                        }

                        return requestedApps;
                    }
                }
        );
    }

    /**
     * Synchronously uninstall an app with the given package name.
     *
     * @param packageName the package name of the app to uninstall
     * */
    public void syncUninstallApp(final CharSequence packageName) {
        Precondition.checkEmptyCharSequence(packageName);

        policyDAO.deletePoliciesWithPackageName(
                activePolicyProfile.profileName,
                packageName.toString()
        );

        appDAO.deleteWithPackageName(packageName.toString());
        policyDAO.deletePoliciesWithPackageName(
                activePolicyProfile.profileName,
                packageName.toString()
        );
    }

    public Map<String, String> getMetadataByOwner(final String owner) {
        Map<String, String> metadata = null;

        GetMetadataTask task = new GetMetadataTask(owner);
        task.start();

        try {
            task.join();
        }
        catch(InterruptedException e) {
            Log.d("DataRepository", "Thread interrupted - cant get metadata");
            Log.d("DataRepository", e.getMessage());
        }

        return task.metadata;
    }

    /*
     * Future-based API call, really just meant for unit testing since
     * I really don't have time for something better.
     * */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Sets the given profile name as the active policy profile.
     *
     * @param profileName the profile to make active
     * */
    public void activateProfile(final CharSequence profileName) {
        Precondition.checkEmptyCharSequence(profileName);

        Future<Void> task = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PolicyProfile profile = policyProfileDAO.getProfile(profileName.toString());

                if(profile != null) {
                    policyProfileDAO.update(false, activePolicyProfile.profileName);
                    policyProfileDAO.update(true, profileName.toString());
                    activePolicyProfile = profile;
                }

                return null;
            }
        });

        try {
            task.get();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<W4PGraph> requestAppGraph(final String packageName) {
        return CompletableFuture.supplyAsync(new Supplier<W4PGraph>() {
            @Override
            public W4PGraph get() {
                AppInfo app = appDAO.getAppWithPackageName(packageName);
                W4PGraph appGraph = new W4PGraph(W4PData.createFromApp(app));
                return appGraph;
            }
        });
    }

    /**
     * Request apps that were installed to this device within the last 30 days.
     *
     * @return CompletableFuture containing the list of apps installed up to 30 days ago.
     * */
    public CompletableFuture<List<W4PData>> requestRecentlyInstalledApps() {
        return CompletableFuture.supplyAsync(
                new Supplier<List<W4PData>>() {
                    @Override
                    public List<W4PData> get() {
                        final int THIRTY_DAYS = (60 * 60 * 24 * 30);
                        final long limit = ((System.currentTimeMillis() / 1000) - THIRTY_DAYS);
                        List<AppInfo> filteredApps =
                                appDAO.getAppPermissionsInstalledBy(limit);

                        List<W4PData> recentApps = new ArrayList<W4PData>(filteredApps.size());

                        for(AppInfo app : filteredApps) {
                            if(!CriticalSystemApps.packageIsSystemApp(app.packageName)) {
                                recentApps.add(W4PData.createFromApp(app));
                            }
                        }

                        return recentApps;
                    }
                }
        );
    }

    public void addMetadata(final String owner,
                            final Map<String, String> metadata) {
        final String jsonString = metadataToJson(metadata);

        (new Thread() {
            public void run() {
                Metadata data = new Metadata();
                data.dataOwner = owner;
                data.metadataJson = jsonString;

                metadataDAO.insert(data);
            }
        }).start();
    }

    public void updateMetadata(final String owner,
                               final Map<String, String> metadata) {
        final String jsonString = metadataToJson(metadata);

        (new Thread() {
            public void run() {
                metadataDAO.updateMetada(owner, jsonString);
            }
        }).start();
    }

    //--------------------------------------------------
    //------------- Policy CRUD Operations -------------
    //--------------------------------------------------

    /**
     * Synchronously get a UserPolicy that has a policy action that is to be enforced based on the
     * provided UserPolicy you queried with.
     *
     * @param policy the policy to query an action for
     * @return the UserPolicy with the action to take/enforce
     * */
    public UserPolicy getUserPolicyAction(UserPolicy policy) {
        final PolicyProfileSetting setting = convertUserPolicyToPolicySetting(policy);

        PolicyProfileSetting result = policyDAO.getSetting(activePolicyProfile.profileName,
                                                           setting.app,
                                                           setting.permission,
                                                           setting.purpose,
                                                           setting.thirdPartyLibrary);

        if(result == null) {
            result = policyDAO.getSetting(PolicyProfile.DEFAULT,
                                          setting.app,
                                          setting.permission,
                                          setting.purpose,
                                          setting.thirdPartyLibrary);
        }

        boolean quickSettingsOverrideUserSetting =
                result.profileName.equalsIgnoreCase(PolicyProfile.DEFAULT) &&
                QuickSettingCheck.quickSettings.containsKey(result.permission) &&
                QuickSettingCheck.quickSettings.get(result.permission) == false;

        if(quickSettingsOverrideUserSetting) {
            result.policyAction = UserPolicy.Policy.DENY.name();
        }

        return convertPolicySettingToUserPolicy(result);
    }

    /**
     * Get all global settings for the specified policy profile. If the default profile is
     * queried, these are just the normal global settings.
     *
     * @param profileName the profile to get global settings for
     * @return the list of global settings
     * */
    public List<UserPolicy> getAllGlobalSettings(final CharSequence profileName) {
        Precondition.checkEmptyCharSequence(profileName);

        GlobalSettingRetrievalTask task = new GlobalSettingRetrievalTask(profileName.toString());

        task.start();

        try {
            task.join();
        } catch(InterruptedException ie) {
            Log.d("DataRepository", "Thread interrupted while getting user policy action");
            Log.d("DataRepository", ie.getMessage());
        }

        List<UserPolicy> globalSettings = new LinkedList<UserPolicy>();

        for(PolicyProfileSetting setting : task.result) {
            globalSettings.add(convertPolicySettingToUserPolicy(setting));
        }

        return globalSettings;
    }

    /**
     * Get all app settings for the provided package name.
     *
     * @param packageName the package name of the app to get settings for
     * @return list of UserPolicys for this app
     * */
    public List<UserPolicy> getAllPoliciesForApp(CharSequence packageName) {
        Precondition.checkEmptyCharSequence(packageName);

        List<PolicyProfileSetting> settings = policyDAO.getAppPermissions(
                PolicyProfile.DEFAULT,
                packageName.toString()
        );

        List<UserPolicy> policies = new ArrayList<UserPolicy>(settings.size());

        for(PolicyProfileSetting setting : settings) {
            policies.add(convertPolicySettingToUserPolicy(setting));
        }

        return policies;
    }

    /**
     * Synchronously adds the given UserPolicy to the DataRepository.
     *
     * @param policy the policy to add
     * */
    public void addUserPolicy(UserPolicy policy) {
        PolicyProfileSetting setting = convertUserPolicyToPolicySetting(policy);
        setting.profileName = PolicyProfile.DEFAULT;

        long[] ids = policyDAO.insert(setting);

        Log.d("dbg", "Inserted with id " + ids[0]);
    }

    /**
     * Synchronously update the provided UserPolicy.
     *
     * @param policy the policy to update
     * */
    public void updateUserPolicy(UserPolicy policy) {
        PolicyProfileSetting setting = convertUserPolicyToPolicySetting(policy);

        int updatedRows = policyDAO.update(
                PolicyProfile.DEFAULT,
                setting.app,
                setting.permission,
                setting.purpose,
                setting.thirdPartyLibrary,
                setting.policyAction,
                setting.lastUpdated
        );

        if(updatedRows == 0) {
            PolicyProfileSetting searchResult = policyDAO.getExactPolicy(
                    setting.app,
                    setting.permission,
                    setting.purpose,
                    setting.thirdPartyLibrary
            );

            if(searchResult == null) {
                throw new IllegalStateException("Cannot update " + policy + " - does not exist.");
            }
        }
    }

    /**
     * Get the policy that matches this exact policy. Mostly an existence check.
     *
     * @param policy the policy to query for
     * @return the UserPolicy the input policy matched
     * */
    public UserPolicy getExactPolicy(UserPolicy policy) {
        PolicyProfileSetting profileSetting = convertUserPolicyToPolicySetting(policy);

        PolicyProfileSetting result = policyDAO.getExactPolicy(
                profileSetting.app,
                profileSetting.permission,
                profileSetting.purpose,
                profileSetting.thirdPartyLibrary
        );

        return convertPolicySettingToUserPolicy(result);
    }

    /**
     * Get the users response to the ask UI within a specified timeframe. If a user
     * had wanted to temporarily apply a policy, then that policy is only valid for a timeframe
     * (specified by the PolicyManager). Synchronous.
     *
     * @param policy the policy to get a response for
     * @param timeframe the duration which a temporary policy is valid for
     * */
    public UserPolicy getUserResponseToAskPromptWithinTime(UserPolicy policy,
                                                           long timeframe) {
        AskPolicySetting setting = askPolicySettingDAO.getLastUserDecisionInTimeframe(
                policy.app,
                policy.permission.androidPermission.toString(),
                policy.purpose.name.toString(),
                policy.thirdPartyLibrary.qualifiedName,
                timeframe
        );

        if(setting == null) { return null; }

        PolicyProfileSetting result = new PolicyProfileSetting();
        result.profileName = policy.profile;
        result.app = setting.app;
        result.permission = setting.permission;
        result.purpose = setting.purpose;
        result.thirdPartyLibrary = setting.library;
        result.policyAction = setting.policy;

        return convertPolicySettingToUserPolicy(result);
    }

    /**
     * Logs the user's temporary policy decision that they selected in a ask/runtime UI prompt.
     * Synchronous.
     *
     * @param policy the policy to temporarily remember
     * */
    public void logUserResponseToAskPrompt(UserPolicy policy) {
        long now = System.currentTimeMillis();

        String library =
                policy.thirdPartyLibrary == null ? ThirdPartyLibraries.APP_INTERNAL_USE :
                                                   policy.thirdPartyLibrary.qualifiedName;

        AskPolicySetting setting = askPolicySettingDAO.settingExists(
                policy.app,
                policy.permission.androidPermission.toString(),
                policy.purpose.name.toString(),
                library
        );

        if(setting != null) {
            setting.timeThisPolicyWasSet = now;

            if(policy.isAllowed()) {
                setting.policy = UserPolicy.Policy.ALLOW.name();
            } else if(policy.isDenied()) {
                setting.policy = UserPolicy.Policy.DENY.name();
            }

            askPolicySettingDAO.update(setting);
        } else {
            AskPolicySetting userResponse = new AskPolicySetting();
            userResponse.app = policy.app;
            userResponse.permission = policy.permission.androidPermission.toString();
            userResponse.purpose = policy.purpose.name.toString();
            userResponse.library = library;
            userResponse.timeThisPolicyWasSet = now;

            if(policy.isAllowed()) {
                userResponse.policy = UserPolicy.Policy.ALLOW.name();
            } else if(policy.isDenied()) {
                userResponse.policy = UserPolicy.Policy.DENY.name();
            }

            askPolicySettingDAO.insert(userResponse);
        }
    }

    //----------------------------------------------
    //--------------- Util Requests ----------------
    //----------------------------------------------

    public static void removeAppPackageFromRepository(final String packageName) {
        (new Thread() {
            public void run() {
                AppInfo toRemoveAppInfo = appDAO.getAppWithPackageName(packageName);

                if(toRemoveAppInfo != null) {
                    appDAO.delete(toRemoveAppInfo);
                    policyDAO.deletePoliciesWithPackageName(PolicyProfile.DEFAULT, packageName);
                    metadataDAO.deleteDataOwnedBy(packageName);
                }
            }
        }).start();
    }

    private PolicyProfileSetting convertUserPolicyToPolicySetting(final UserPolicy policy) {
        if(policy == null) { return null; }

        PolicyProfileSetting setting = new PolicyProfileSetting();
        setting.profileName = activePolicyProfile.profileName;
        setting.app = policy.app;
        setting.permission = policy.permission.androidPermission.toString();
        setting.purpose = policy.purpose.name.toString();

        if(policy.thirdPartyLibrary != null) {
            setting.thirdPartyLibrary = policy.thirdPartyLibrary.qualifiedName;
        } else {
            setting.thirdPartyLibrary = ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE.qualifiedName;
        }

        setting.lastUpdated = policy.getLastUpdatedMillis();

        if (policy.isAllowed()) {
            setting.policyAction = UserPolicy.Policy.ALLOW.name();
        }
        if (policy.isAsk()) {
            setting.policyAction = UserPolicy.Policy.ASK.name();
        }
        if (policy.isDenied()) {
            setting.policyAction = UserPolicy.Policy.DENY.name();
        }

        return setting;
    }

    private UserPolicy convertPolicySettingToUserPolicy(final PolicyProfileSetting setting) {
        if(setting != null) { return UserPolicy.convert(setting); }
        return null;
    }

    private class GlobalSettingRetrievalTask extends Thread {
        List<PolicyProfileSetting> result = new LinkedList<PolicyProfileSetting>();
        private String profileName;

        public GlobalSettingRetrievalTask(final String profileName) {
            this.profileName = profileName;
        }

        public void run() {
            result = policyDAO.getAllGlobalPolicies(profileName);
        }
    }

    private Map<String, String> jsonToMetadata(final String json) {
        Map<String, String> metadata = new TreeMap<String, String>();

        JsonReader reader = new JsonReader(new StringReader(json));

        try {
            reader.beginObject();

            while(reader.hasNext()) {
                String key = reader.nextName();
                String value = "";

                if(key.equals("rank")) { value = String.valueOf(reader.nextDouble()); }
                else { value = reader.nextString(); }

                metadata.put(key, value);
            }

            reader.endObject();
        }
        catch(IOException e) {
            Log.d("DataRepository", "Unable to convert json metadata");
            Log.d("DataRepository", e.getMessage());
        }

        return metadata;
    }

    private String metadataToJson(final Map<String, String> metadata) {
        String jsonString = "{ ";
        Iterator<String> iter = metadata.keySet().iterator();

        while(iter.hasNext()) {
            String key = iter.next();
            jsonString += "\"" + key + "\" : \"" + metadata.get(key) + "\"";

            if(iter.hasNext()) { jsonString += ", "; }
        }

        jsonString += " }";

        return jsonString;
    }

    private class GetMetadataTask extends Thread {
        public Map<String, String> metadata;
        private String owner;

        public GetMetadataTask(final String o) {
            owner = o;
        }

        public void run() {
            Metadata m = metadataDAO.getMetadataByOwner(owner);

            if(m != null) {
                metadata = jsonToMetadata(m.metadataJson);
            }
        }
    }
}