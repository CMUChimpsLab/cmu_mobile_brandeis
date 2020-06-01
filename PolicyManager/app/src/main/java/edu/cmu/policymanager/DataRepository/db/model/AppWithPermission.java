package edu.cmu.policymanager.DataRepository.db.model;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 * Created by shawn on 3/16/2018.
 */

public class AppWithPermission {
    @Embedded
    public AppInfo appInfo;

    @Relation(parentColumn = "id", entityColumn = "app_id")
    public List<PermissionInfo> permissions;
}
