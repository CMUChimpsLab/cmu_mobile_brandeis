package edu.cmu.policymanager.DataRepository.db.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.cmu.policymanager.DataRepository.db.model.ThirdPartyLibInfo;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 6/28/2018.
 */

@Dao
public abstract class ThirdPartyLibDAO {
    @Query("SELECT id, name, category, description, app_coverage, install_coverage, " +
           "purpose_json_array, permissions_json_array, apps_json_array, link " +
           "FROM thirdpartylibinfo ORDER BY app_coverage DESC LIMIT :totalLibraries")
    public abstract List<ThirdPartyLibInfo> getNLibraries(int totalLibraries);

    @Query("SELECT id, name, category, description, app_coverage, install_coverage, " +
           "purpose_json_array, permissions_json_array, apps_json_array, link " +
           "FROM thirdpartylibinfo WHERE name=:libraryName")
    public abstract ThirdPartyLibInfo getLibraryByName(String libraryName);

    @Query("SELECT id, name, category, description, app_coverage, install_coverage, " +
           "purpose_json_array, permissions_json_array, apps_json_array, link " +
           "FROM thirdpartylibinfo WHERE package_name=:packageName")
    public abstract ThirdPartyLibInfo getLibraryByPackage(String packageName);

    @Query("SELECT DISTINCT id, category FROM thirdpartylibinfo")
    public abstract List<ThirdPartyLibInfo> getLibraryCategories();

    @Query("SELECT id, name, category, description, app_coverage, install_coverage, " +
           "purpose_json_array, permissions_json_array, apps_json_array, link FROM " +
           "thirdpartylibinfo WHERE category=:category")
    public abstract List<ThirdPartyLibInfo> getLibrariesByCategory(String category);

    @Insert
    public abstract long insert(ThirdPartyLibInfo library);

    @Insert
    public abstract long[] insertAll(List<ThirdPartyLibInfo> libraries);
}