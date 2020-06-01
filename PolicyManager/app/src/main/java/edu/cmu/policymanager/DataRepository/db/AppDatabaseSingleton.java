package edu.cmu.policymanager.DataRepository.db;

import android.content.Context;

import androidx.room.Room;
import edu.cmu.policymanager.DataRepository.DataRepository;

/**
 * Created by shawn on 3/16/2018.
 */

public class AppDatabaseSingleton {
    private static final AppDatabaseSingleton diskDatabase = new AppDatabaseSingleton(),
                                              inMemoryDatabase = new AppDatabaseSingleton();

    private AppDatabase db = null;

    private AppDatabaseSingleton() {}

    public static void createDatabases(final Context context) {
        if(diskDatabase.db == null) {
            createDiskDB(context);
        }

        if(inMemoryDatabase.db == null) {
            createMemoryDB(context);
        }
    }

    public static AppDatabase getDB(final DataRepository.StorageType type) {
        if(type == DataRepository.StorageType.DISK) {
            return diskDatabase.db;
        }

        if(type == DataRepository.StorageType.IN_MEMORY) {
            return inMemoryDatabase.db;
        }

        throw new IllegalArgumentException(
                "DataRepository can only get one of two types of databases: " +
                "IN_MEMORY and DISK, but received " + type
        );
    }

    private synchronized static void createDiskDB(Context ctx){
        diskDatabase.db = Room.databaseBuilder(ctx, AppDatabase.class, "policymanager-db")
                              .fallbackToDestructiveMigration()
                              .build();
    }

    private synchronized static void createMemoryDB(Context ctx) {
        inMemoryDatabase.db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class).build();
    }
}