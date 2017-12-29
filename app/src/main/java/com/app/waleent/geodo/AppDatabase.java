package com.app.waleent.geodo;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by wale on 10/30/17.
 */

@Database(entities = {TaskModel.class}, version = 1)
public abstract class AppDatabase  extends RoomDatabase{
    private static AppDatabase INSTANCE;
    public static AppDatabase getInstance(Context context){
        if(INSTANCE == null){
            // create database
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,"task_db").build();
//            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,"task_db").allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
    public abstract TaskDao taskDao();

}

