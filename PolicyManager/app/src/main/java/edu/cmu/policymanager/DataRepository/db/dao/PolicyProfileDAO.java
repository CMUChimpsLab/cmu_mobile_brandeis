package edu.cmu.policymanager.DataRepository.db.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;

@Dao
public abstract class PolicyProfileDAO {
    @Insert
    abstract public long insert(PolicyProfile profile);

    @Query("SELECT id, profile_name, is_active FROM policyprofile")
    abstract public List<PolicyProfile> getProfiles();

    @Query("SELECT id, profile_name, is_active FROM policyprofile " +
           "WHERE is_active=1")
    abstract public PolicyProfile getActiveProfile();

    @Query("SELECT id, profile_name, is_active from policyprofile " +
           "WHERE profile_name=:profile")
    abstract public PolicyProfile getProfile(String profile);

    @Query("UPDATE policyprofile SET is_active=:isActive WHERE profile_name=:profile")
    abstract public int update(boolean isActive, String profile);
}