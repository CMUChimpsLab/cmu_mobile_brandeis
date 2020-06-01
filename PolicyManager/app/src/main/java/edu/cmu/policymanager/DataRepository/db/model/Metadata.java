package edu.cmu.policymanager.DataRepository.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 6/26/2018.
 */

@Entity
public class Metadata {
    public Metadata() { }

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "data_owner")
    public String dataOwner;

    @ColumnInfo(name = "metadata_json")
    public String metadataJson;
}