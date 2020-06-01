package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 6/26/2018.
 */

@Entity
public class ThirdPartyLibInfo {
    public ThirdPartyLibInfo() { }

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "package_name")
    public String packageName;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "app_coverage")
    public Double appCoverage;

    @ColumnInfo(name = "install_coverage")
    public String installCoverage;

    @ColumnInfo(name = "purpose_json_array")
    public String purposeJsonArray;

    @ColumnInfo(name = "permissions_json_array")
    public String permissionsJsonArray;

    @ColumnInfo(name = "apps_json_array")
    public String appsJsonArray;

    @ColumnInfo(name = "link")
    public String link;
}