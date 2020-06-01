package edu.cmu.policymanager.DataRepository.db.model;

/**
 * Created by shawn on 3/15/2018.
 */

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = AppInfo.class,
        parentColumns = "id",
        childColumns = "app_id",
        onDelete = CASCADE))
public class PermissionInfo {
    public PermissionInfo() {}

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "app_id")
    public long appID;

    @ColumnInfo(name = "permission_name")
    public String permissionName;

    @ColumnInfo(name = "permission_description")
    public String permissionDescription;

    @ColumnInfo(name = "sensitivity")
    public String sensitivity;

    @ColumnInfo(name = "protection_level")
    public int protectionLevel;
}