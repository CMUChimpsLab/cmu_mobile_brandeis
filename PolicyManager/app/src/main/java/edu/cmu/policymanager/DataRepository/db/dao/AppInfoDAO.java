package edu.cmu.policymanager.DataRepository.db.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import edu.cmu.policymanager.DataRepository.db.model.AppInfo;
import edu.cmu.policymanager.DataRepository.db.model.AppWithPermission;

/**
 * Created by shawn on 3/16/2018.
 */

@Dao
public abstract class AppInfoDAO {
    @Query("SELECT * from appinfo WHERE package_name=:packageName LIMIT 1")
    abstract public AppInfo getAppWithPackageName(String packageName);

    @Query("DELETE FROM appinfo WHERE package_name=:packageName")
    abstract public void deleteWithPackageName(String packageName);

    @Query("SELECT * FROM appinfo ORDER BY app_name")
    abstract public List<AppInfo> getAll();

    @Query("SELECT id, package_name, app_name, version_code, version_name, last_update_time, " +
           "category, first_install_time FROM appinfo WHERE first_install_time >= :limit " +
           "OR first_install_time = 0 ORDER BY first_install_time DESC")
    abstract public List<AppInfo> getAppPermissionsInstalledBy(long limit);

    @Query("SELECT * from appinfo WHERE package_name=:packageName AND version_code=:versionCode LIMIT 1")
    abstract AppInfo getAppWithVersionCode(String packageName, int versionCode);

    @Insert
    abstract public long insert(AppInfo info);

    @Insert
    abstract public long[] insert(AppInfo... info);

    @Update
    abstract public int update(AppInfo... info);

    @Delete
    abstract public int delete(AppInfo... info);

    public boolean hasAppWithVersion(String packageName, int versionCode) {
        return getAppWithVersionCode(packageName, versionCode) != null;
    }
}