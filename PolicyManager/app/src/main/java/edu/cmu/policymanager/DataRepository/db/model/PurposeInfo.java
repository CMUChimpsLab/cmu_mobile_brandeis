package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Created by shawn on 3/15/2018.
 */

@Entity(foreignKeys = @ForeignKey(entity = PermissionInfo.class,
        parentColumns = "id",
        childColumns = "permission_id",
        onDelete = CASCADE))
public class PurposeInfo {
    public PurposeInfo() {}

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "permission_id")
    public long permissionID;

    @ColumnInfo(name = "purpose_name")
    public String purposeName;

    @ColumnInfo(name = "purpose_description")
    public String purposeDescription;
}