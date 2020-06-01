package edu.cmu.policymanager.DataRepository.db.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import edu.cmu.policymanager.DataRepository.db.model.AskPolicySetting;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;

@Dao
public abstract class AskPolicySettingDAO {
    public AskPolicySetting getLastUserDecisionInTimeframe(String app,
                                                           String permission,
                                                           String purpose,
                                                           String libraryOrCategory,
                                                           long limit
    ) {
        String category = ThirdPartyLibraries.getLibraryCategory(libraryOrCategory);
        long currentTime = System.currentTimeMillis();

        return queryLastUserDecisionInTimeframe(
                app, permission, purpose, libraryOrCategory, category, currentTime, limit
        );
    }

    @Query("SELECT id, app, permission, purpose, library, policy, time_this_policy_was_set " +
           "FROM askpolicysetting WHERE app=:app AND permission=:permission AND " +
           "purpose=:purpose AND (library=:library OR library=:category) AND " +
           ":currentTime <= (time_this_policy_was_set + :limit) " +
           " ORDER BY time_this_policy_was_set DESC LIMIT 1")
    abstract public AskPolicySetting queryLastUserDecisionInTimeframe(String app,
                                                                      String permission,
                                                                      String purpose,
                                                                      String library,
                                                                      String category,
                                                                      long currentTime,
                                                                      long limit
    );

    @Query("SELECT id, app, permission, purpose, library, policy, time_this_policy_was_set " +
           "FROM askpolicysetting WHERE app=:app AND permission=:permission AND " +
           "purpose=:purpose AND library=:library")
    abstract public AskPolicySetting settingExists(
            String app, String permission, String purpose, String library
    );

    @Insert
    abstract public long insert(AskPolicySetting setting);

    @Query("UPDATE askpolicysetting SET policy=:policy WHERE app=:app AND permission=:permission " +
           "AND purpose=:purpose AND library=:library")
    abstract public int update(
            String app, String permission, String purpose, String library, String policy
    );

    @Update
    abstract public int update(AskPolicySetting setting);

    @Query("SELECT id, app, permission, purpose, library, policy, time_this_policy_was_set " +
           " FROM askpolicysetting")
    abstract public List<AskPolicySetting> getAllAskResponses();
}