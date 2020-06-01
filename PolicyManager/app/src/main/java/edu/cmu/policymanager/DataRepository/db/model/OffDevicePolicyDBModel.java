package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class OffDevicePolicyDBModel {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name="package_name")
    public String packageName;

    @ColumnInfo
    public String odp;
}