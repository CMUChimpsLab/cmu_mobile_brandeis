package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by shawn on 3/15/2018.
 */

@Entity()
public class AppInfo {
    public AppInfo() {}

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "package_name", index = true)
    public String packageName;

    @ColumnInfo(name = "app_name")
    public String appName;

    @ColumnInfo(name = "version_code")
    public int versionCode;

    @ColumnInfo(name = "version_name")
    public String versionName;

    @ColumnInfo(name = "category")
    public int category;

    @ColumnInfo(name = "last_update_time")
    public long lastUpdateTime;

    @ColumnInfo(name = "first_install_time")
    public long firstInstallTime;
}