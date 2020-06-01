package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AskPolicySetting {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo
    public String app;

    @ColumnInfo
    public String permission;

    @ColumnInfo
    public String purpose;

    @ColumnInfo
    public String library;

    @ColumnInfo
    public String policy;

    @ColumnInfo(name = "time_this_policy_was_set")
    public long timeThisPolicyWasSet;
}