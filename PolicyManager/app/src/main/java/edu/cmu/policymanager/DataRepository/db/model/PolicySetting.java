package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by shawn on 3/15/2018.
 */

@Entity
public class PolicySetting {
    //int uid?

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "app")
    public String app;

    @ColumnInfo(name = "purpose")
    public String purpose;

    @ColumnInfo(name = "permission")
    public String permission;

    @ColumnInfo(name = "third_party_library")
    public String thirdPartyLibrary;

    @ColumnInfo(name = "last_updated")
    public Long lastUpdated = -1L;

    @ColumnInfo(name = "policy_action")
    public String policyAction;
}