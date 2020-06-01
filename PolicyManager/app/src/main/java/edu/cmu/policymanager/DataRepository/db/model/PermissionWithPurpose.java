package edu.cmu.policymanager.DataRepository.db.model;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 * Created by shawn on 3/29/2018.
 */

public class PermissionWithPurpose {
    @Embedded
    public PermissionInfo permissionInfo;

    @Relation(parentColumn = "id", entityColumn = "permission_id")
    public List<PurposeInfo> purposes;
}
