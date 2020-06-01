package edu.cmu.policymanager.DataRepository.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.cmu.policymanager.DataRepository.db.model.Metadata;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 6/26/2018.
 */

@Dao
public abstract class MetadataDAO {
    @Query("SELECT id, data_owner, metadata_json FROM metadata WHERE data_owner=:owner")
    public abstract Metadata getMetadataByOwner(String owner);

    @Insert
    public abstract long[] insert(Metadata... metadata);

    @Query("UPDATE metadata SET metadata_json=:updatedJson WHERE data_owner=:owner")
    public abstract int updateMetada(String owner, String updatedJson);

    @Query("DELETE FROM metadata WHERE data_owner=:owner")
    public abstract void deleteDataOwnedBy(final String owner);
}