package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PolicyProfile {
    public static final String DEFAULT = "Default User Profile";
    public static final String ORGANIZATIONAL = "Organizational Profile";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "profile_name")
    public String profileName;

    @ColumnInfo(name = "is_active")
    public boolean isActive;
}