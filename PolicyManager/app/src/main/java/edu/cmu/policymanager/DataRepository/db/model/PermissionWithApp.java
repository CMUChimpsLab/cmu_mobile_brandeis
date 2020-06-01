package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 5/29/2018.
 */

public class PermissionWithApp {
    @Embedded
    public PermissionInfo permission;

    @Relation(parentColumn = "app_id", entityColumn = "id")
    public AppInfo app;
}