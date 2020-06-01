package edu.cmu.policymanager.DataRepository.db.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import edu.cmu.policymanager.DataRepository.db.model.PermissionInfo;
import edu.cmu.policymanager.DataRepository.db.model.PurposeInfo;

/**
 * Created by shawn on 3/27/2018.
 */

@Dao
public abstract class PurposeInfoDAO {
    @Insert
    public abstract long insert(PurposeInfo repo);

    @Update
    public abstract void update(PurposeInfo... repos);

    @Delete
    public abstract void delete(PurposeInfo... repos);

    /**
     * Get all unique (grouped by name) purpose infos
     * @return
     */
    @Query("SELECT * FROM purposeinfo GROUP BY purpose_name")
    public abstract List<PurposeInfo> getAllUniquePurposeInfos();

    @Query("SELECT id, permission_id, purpose_name, purpose_description FROM purposeinfo " +
           "GROUP BY purpose_name LIMIT :maxRows")
    public abstract List<PurposeInfo> getNPurposeInfos(int maxRows);

    @Query("SELECT * FROM purposeinfo WHERE permission_id=:permissionId")
    public abstract List<PurposeInfo> findPurposeInfosForPermission(final int permissionId);

    @Query("SELECT * FROM purposeinfo INNER JOIN permissioninfo on permissioninfo.id=purposeinfo.id WHERE permission_name=:permissionName")
    public abstract List<PurposeInfo> findPurposeInfosForPermission(String permissionName);

    @Query("SELECT id, purpose_name, permission_id, purpose_description FROM purposeinfo " +
           "WHERE purpose_name=:purposeName")
    public abstract PurposeInfo getPurposeWithName(String purposeName);

    @Insert
    public abstract long[] _insertAllPurposes(List<PurposeInfo> purposes);

    public void insertPurposesForPermission(PermissionInfo permission, List<PurposeInfo> purposeInfos){
        for(PurposeInfo purposeInfo : purposeInfos){
            purposeInfo.permissionID = permission.id;
        }

        long[] ids = _insertAllPurposes(purposeInfos);

        int idx = 0;
        for(PurposeInfo purposeInfo : purposeInfos){
            purposeInfo.id = ids[idx++];
        }
    }

    @Query("SELECT * from purposeinfo INNER JOIN permissioninfo on permissioninfo.id=purposeinfo.permission_id  WHERE permission_name=:permissionName GROUP BY purpose_name")
    public abstract List<PurposeInfo> getAllPurposesWithPermissionName(String permissionName);

    @Query("SELECT * from purposeinfo")
    public abstract List<PurposeInfo> getAllPurposeInfos();

//    @Query("SELECT * from purposeinfo INNER JOIN permissioninfo on permissioninfo.id=purposeinfo.permission_id  WHERE sensitivity=:sensitivity GROUP BY purpose_name")
//    public abstract List<PurposeInfo> getAllPurposeInfosSensitive(String sensitivity);

    @Query("SELECT * from purposeinfo INNER JOIN permissioninfo on permissioninfo.id=purposeinfo.permission_id INNER JOIN appinfo on appinfo.id=permissioninfo.app_id WHERE package_name=:packageName and permission_name=:permissionName")
    public abstract List<PurposeInfo> getPurposesWithAppAndPermissionName(String packageName, String permissionName);

    @Query("DELETE FROM purposeinfo")
    public abstract void clearTable();
}
