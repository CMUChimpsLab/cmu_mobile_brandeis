package edu.cmu.policymanager.DataRepository.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.cmu.policymanager.DataRepository.db.model.OffDevicePolicyDBModel;

@Dao
public abstract class OffDevicePolicyDAO {
    @Query("SELECT id, package_name, odp FROM offdevicepolicydbmodel WHERE package_name=:packageName")
    public abstract OffDevicePolicyDBModel getPolicy(String packageName);

    @Insert public abstract long insert(OffDevicePolicyDBModel policy);
}