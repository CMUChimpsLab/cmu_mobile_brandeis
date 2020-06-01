package edu.cmu.policymanager.DataRepository.db.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import edu.cmu.policymanager.DataRepository.db.model.AppInfo;
import edu.cmu.policymanager.DataRepository.db.model.PermissionInfo;
import edu.cmu.policymanager.DataRepository.db.model.PermissionWithPurpose;

/**
 * Created by shawn on 3/16/2018.
 */

@Dao
public abstract class PermissionInfoDAO {
    @Insert
    public abstract long insert(PermissionInfo permission);

    @Insert
    public abstract long[] insert(PermissionInfo... permission);

    @Insert
    abstract long[] _insertAllPermissions(List<PermissionInfo> permissions);

    public void insertPermissionsForApp(AppInfo app, List<PermissionInfo> permissions){
        for(PermissionInfo permission : permissions){
            permission.appID = app.id;
        }

        long[] ids = _insertAllPermissions(permissions);

        int i=0;
        for(PermissionInfo permission : permissions){
            permission.id = ids[i++];
        }
    }

    @Update
    public abstract void update(PermissionInfo... repos);

    @Delete
    public abstract void delete(PermissionInfo... repos);

    @Query("SELECT id, app_id, permission_name, permission_description, sensitivity, protection_level " +
           "FROM permissioninfo WHERE permission_name = :permissionName")
    public abstract PermissionInfo getPermissionWithName(String permissionName);

    @Query("SELECT id, app_id, permission_name, permission_description, sensitivity, protection_level " +
            "FROM permissioninfo WHERE sensitivity > 1 GROUP BY permission_name LIMIT :maxRecords")
    public abstract List<PermissionInfo> getNPermissionInfos(int maxRecords);

//    @Query("SELECT * FROM permissioninfo GROUP BY permission_name")
//    public abstract List<PermissionInfo> getAllPermissionInfos();
//
//    @Query("SELECT * FROM permissioninfo where sensitivity=:sensitivity GROUP BY permission_name")
//    public abstract List<PermissionInfo> getAllPermissionInfos(String sensitivity);
//
    @Query("SELECT * FROM permissioninfo WHERE sensitivity > 1 GROUP BY permission_name")
    public abstract List<PermissionInfo> getAllPermissionInfos();
//
//    @Query("SELECT * FROM permissioninfo WHERE app_id=:appId")
//    public abstract List<PermissionInfo> findPermissionInfosForApp(final int appId);
//
//    @Query("SELECT * FROM permissioninfo INNER JOIN appinfo on permissioninfo.app_id=appinfo.id WHERE package_name=:packageName")
//    public abstract List<PermissionInfo> findPermissionInfosForApp(String packageName);
//
    @Query("SELECT * from permissioninfo INNER JOIN purposeinfo on permissioninfo.id=purposeinfo.permission_id  WHERE permission_name=:permissionName GROUP BY purpose_name")
    public abstract PermissionWithPurpose getAllPurposesWithPermissionName(String permissionName);

    @Query("SELECT pi.id, pi.app_id, pi.permission_name, pi.permission_description, " +
           "pi.sensitivity, pi.protection_level FROM permissioninfo as `pi` INNER JOIN " +
           "purposeinfo ON purposeinfo.permission_id=pi.id INNER JOIN appinfo " +
           "ON pi.app_id=appinfo.id WHERE appinfo.package_name=:packageName AND " +
           "purposeinfo.purpose_name=:purposeName")
    public abstract List<PermissionInfo> getPermissionsWithPackageAndPurposeName(String packageName, String purposeName);

    @Query("DELETE FROM permissionInfo")
    public abstract void clearTable();
}