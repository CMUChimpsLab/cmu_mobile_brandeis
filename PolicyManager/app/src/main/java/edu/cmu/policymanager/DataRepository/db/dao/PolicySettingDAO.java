package edu.cmu.policymanager.DataRepository.db.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.cmu.policymanager.DataRepository.db.model.PolicySetting;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 3/30/2018.
 */

@Dao
public abstract class PolicySettingDAO {
    @Query("SELECT id FROM policysetting WHERE app=:appName AND permission=:permissionName AND " +
           "purpose=:purposeName AND third_party_library=:thirdPartyLibrary")
    abstract public PolicySetting checkIfPolicyExists(String appName, String permissionName, String purposeName, String thirdPartyLibrary);

    @Query("SELECT id, app, permission, purpose, third_party_library, policy_action, last_updated " +
           "FROM policysetting WHERE app=:packageName")
    abstract public List<PolicySetting> getAppPermissions(String packageName);

    @Query("SELECT id, app, permission, purpose, third_party_library, policy_action, last_updated " +
           "FROM policysetting WHERE app=:app AND permission=:permission AND NOT purpose='*' " +
           "AND NOT third_party_library='*'")
    abstract public List<PolicySetting> getAppPermissionPurposeLibs(String app, String permission);

    @Query("SELECT id, app, permission, purpose, third_party_library, policy_action, last_updated " +
           "FROM policysetting WHERE permission IN (:permissions)")
    abstract public List<PolicySetting> getAllPermissionPolicies(List<String> permissions);

    @Query("SELECT id, permission, purpose, policy_action, third_party_library, last_updated FROM " +
           "policysetting WHERE app='*'")
    abstract public List<PolicySetting> getAllGlobalPolicies();

    @Query("SELECT id, permission, purpose, policy_action, third_party_library, last_updated FROM " +
           "policysetting WHERE app='*' LIMIT :N")
    abstract public List<PolicySetting> getAtMostNGlobalPolicies(int N);

    @Query("SELECT id, app, purpose, third_party_library, last_updated FROM policysetting WHERE permission IN (:permissions)")
    abstract public List<PolicySetting> getGroupPurposeLibs(List<String> permissions);

    @Query("SELECT id, app, permission, purpose, policy_action, third_party_library, last_updated " +
            "FROM policysetting WHERE app=:app AND permission=:permission AND " +
            "purpose=:purpose AND third_party_library=:thirdPartyLibrary ORDER BY last_updated DESC LIMIT 1")
    abstract public PolicySetting getPolicySettingFor(String app, String permission, String purpose, String thirdPartyLibrary);

    @Query("SELECT id, app, permission, purpose, policy_action, third_party_library, last_updated " +
           "FROM policysetting WHERE app='*' AND permission='android.permission.READ_CONTACTS' OR " +
           "permission='android.permission.WRITE_CONTACTS'")
    abstract public List<PolicySetting> getGlobalContactSettings();

    @Query("SELECT policy_action FROM policysetting WHERE permission=:permission AND purpose=:purpose" +
           " GROUP BY policy_action ORDER BY COUNT(policy_action) DESC LIMIT 1")
    abstract public String getFrequentlySetPolicyFor(String permission, String purpose);

    @Query("UPDATE policysetting SET policy_action=:action, last_updated=:lastUpdated WHERE app=:app AND " +
           "permission=:permission AND purpose=:purpose AND third_party_library=:thirdPartyLibrary")
    abstract public int update(String app, String permission, String purpose, String thirdPartyLibrary, String action, long lastUpdated);

    @Query("SELECT id, app, permission, purpose, policy_action, third_party_library, last_updated " +
           "FROM policysetting WHERE (app=:app OR app='*') AND (permission=:permission OR permission='*') " +
           "AND (purpose=:purpose OR purpose='*') AND (third_party_library=:thirdPartyLibrary OR third_party_library='*') " +
           "ORDER BY last_updated DESC LIMIT 1")
    abstract public PolicySetting getPolicyToEnforce(String app, String permission, String purpose, String thirdPartyLibrary);

    @Query("SELECT id, app, permission, purpose, policy_action, last_updated FROM policysetting WHERE " +
           "app LIKE :app AND permission LIKE :permission AND purpose LIKE :purpose " +
           "AND third_party_library LIKE :thirdPartyLibrary")
    abstract public List<PolicySetting> getAllMatchingSettings(String app, String permission, String purpose, String thirdPartyLibrary);


    @Insert
    abstract public long[] insert(PolicySetting... setting);

    @Query("DELETE FROM policysetting WHERE app=:packageName")
    abstract public int deletePoliciesWithPackageName(String packageName);

    @Query("DELETE FROM policysetting")
    abstract public void clearTable();

    @Query("DELETE FROM policysetting WHERE app=:packageName AND permission=:permissionName AND " +
           "purpose=:purposeName AND third_party_library=:library AND policy_action=:action")
    abstract public void deletePolicy(String packageName, String permissionName, String purposeName, String library, String action);
}