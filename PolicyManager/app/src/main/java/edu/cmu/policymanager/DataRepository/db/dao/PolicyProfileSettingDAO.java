package edu.cmu.policymanager.DataRepository.db.dao;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfileSetting;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;

@Dao
public abstract class PolicyProfileSettingDAO {
    public PolicyProfileSetting getSetting(String profile,
                                           String appName,
                                           String permission,
                                           String purpose,
                                           String libraryOrCategory) {
        String category = null;

        if(libraryOrCategory == null || libraryOrCategory.isEmpty()) {
            libraryOrCategory = ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE.qualifiedName;
        }

        if(libraryOrCategory.contains(ThirdPartyLibraries.APP_INTERNAL_USE)) {
            category = libraryOrCategory;
        }
        else if(libraryOrCategory.contains(ThirdPartyLibraries.THIRD_PARTY_USE)) {
            category = libraryOrCategory;
        }

        if(category == null) {
            if(ThirdPartyLibraries.packageToLibrary.containsKey(libraryOrCategory)) {
                ThirdPartyLibrary library = ThirdPartyLibraries.from(libraryOrCategory);
                category = library.category;
            }
        }

        return getSettingByCategoryOrLibrary(profile,
                                             appName,
                                             permission,
                                             purpose,
                                             libraryOrCategory,
                                             category);
    }

    @Query("SELECT id, profile_name, app, permission, purpose, third_party_library, " +
           "policy_action, last_updated FROM policyprofilesetting WHERE profile_name=:profile " +
           "AND (app=:packageName OR app='*') AND (permission=:permissionName OR permission='*') " +
           "AND (purpose=:purpose OR purpose='*') AND (third_party_library=:library OR " +
           "third_party_library=:libraryCategory OR third_party_library='*') ORDER BY " +
           "last_updated DESC LIMIT 1")
    abstract public PolicyProfileSetting getSettingByCategoryOrLibrary(String profile,
                                                                       String packageName,
                                                                       String permissionName,
                                                                       String purpose,
                                                                       String library,
                                                                       String libraryCategory);

    @Query("SELECT DISTINCT app, id FROM policyprofilesetting where permission=:permission AND " +
           "purpose=:purpose")
    abstract public List<PolicyProfileSetting> getAppsUsingPermissionPurpose(
            final String permission,
            final String purpose
    );

    @Query("SELECT id FROM policyprofilesetting WHERE profile_name=:profile AND " +
           "app=:appName AND permission=:permissionName AND " +
           "purpose=:purposeName AND third_party_library=:thirdPartyLibrary")
    abstract public PolicyProfileSetting checkIfPolicyExists(String profile,
                                                             String appName,
                                                             String permissionName,
                                                             String purposeName,
                                                             String thirdPartyLibrary);

    @Query("SELECT id, profile_name, app, permission, purpose, third_party_library, " +
           "policy_action, last_updated FROM policyprofilesetting WHERE " +
           "profile_name=:profile AND app=:packageName")
    abstract public List<PolicyProfileSetting> getAppPermissions(String profile,
                                                                 String packageName);

    @Query("SELECT id, app, permission, purpose, third_party_library, policy_action, " +
           "last_updated FROM policyprofilesetting WHERE profile_name=:profile")
    abstract public List<PolicyProfileSetting> getAllProfileSettings(String profile);

    @Query("SELECT id, app, permission, purpose, policy_action, third_party_library, " +
           "last_updated FROM policyprofilesetting WHERE profile_name=:profile AND app='*'")
    abstract public List<PolicyProfileSetting> getAllGlobalPolicies(String profile);

    @Query("SELECT policy_action FROM policyprofilesetting WHERE profile_name=:profile AND " +
           "(app=:app OR app='*') AND permission=:permission AND purpose=:purpose" +
           " GROUP BY policy_action ORDER BY COUNT(policy_action) DESC LIMIT 1")
    abstract public String getFrequentlySetPolicyFor(String app,
                                                     String profile,
                                                     String permission,
                                                     String purpose);

    @Query("UPDATE policyprofilesetting SET policy_action=:action, last_updated=:lastUpdated " +
           "WHERE profile_name=:profile AND app=:app AND permission=:permission " +
           "AND purpose=:purpose AND third_party_library=:thirdPartyLibrary")
    abstract public int update(String profile,
                               String app,
                               String permission,
                               String purpose,
                               String thirdPartyLibrary,
                               String action,
                               long lastUpdated);

    @Query("SELECT id, app, permission, purpose, third_party_library, policy_action, " +
           "last_updated FROM policyprofilesetting WHERE app=:app AND " +
           "permission=:permission AND purpose=:purpose AND " +
           "third_party_library=:libraryOrCategory")
    abstract public PolicyProfileSetting getExactPolicy(String app,
                                                        String permission,
                                                        String purpose,
                                                        String libraryOrCategory);

    @Insert
    abstract public long[] insert(PolicyProfileSetting... setting);

    @Query("DELETE FROM policyprofilesetting WHERE profile_name=:profile AND app=:packageName")
    abstract public int deletePoliciesWithPackageName(String profile, String packageName);

    @Query("DELETE FROM policyprofilesetting WHERE app=:appName AND " +
           "permission=:permission AND purpose=:purpose AND third_party_library=:library")
    abstract public void deleteUserPolicy(String appName,
                                          String permission,
                                          String purpose,
                                          String library);

    @Query("DELETE FROM policyprofilesetting")
    abstract public void clearTable();
}