package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PolicyProfileSetting implements Comparable<PolicyProfileSetting> {
    //int uid?

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "profile_name")
    public String profileName;

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

    @Override
    public boolean equals(Object o) {
        PolicyProfileSetting s = (PolicyProfileSetting)o;

        String ours = app + permission + purpose + thirdPartyLibrary,
               theirs = s.app + s.permission + s.purpose + s.thirdPartyLibrary;

        return ours.equalsIgnoreCase(theirs);
    }

    @Override
    public int hashCode() {
        return (app + permission + purpose + thirdPartyLibrary).hashCode();
    }

    @Override
    public int compareTo(PolicyProfileSetting o) {
        String ours = app + permission + purpose + thirdPartyLibrary,
               theirs = o.app + o.permission + o.purpose + o.thirdPartyLibrary;

        if(ours.equalsIgnoreCase(theirs)) { return 0; }

        return 1;
    }
}