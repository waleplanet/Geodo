package com.app.waleent.geodo;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by wale on 10/30/17.
 */

@Dao
@TypeConverters({DateConverter.class})
public interface  TaskDao {
    @Query("select * from TaskModel")
    LiveData<List<TaskModel>> getAllTaskItems();

    @Query("select * from TaskModel")
    List<TaskModel> getTaskList();

    @Query("select * from TaskModel where id = :id")
    TaskModel getItembyId(String id);

    @Insert(onConflict = REPLACE)
    void addTask(TaskModel taskModel);

    @Delete
    void deleteTask(TaskModel taskModel);

    @Update
    void updateTask (TaskModel taskModel);
}
