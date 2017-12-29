package com.app.waleent.geodo;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

/**
 * Created by wale on 10/30/17.
 */

public class TaskListViewModel extends AndroidViewModel {

    private final LiveData<List<TaskModel>> taskList;
    private static AppDatabase appDatabase;

    public TaskListViewModel(Application application) {
        super(application);
        appDatabase = AppDatabase.getInstance(this.getApplication());
        taskList = appDatabase.taskDao().getAllTaskItems();
    }
    public LiveData<List<TaskModel>> getTaskList() {return taskList;}

    public static TaskModel getTask(String id){
        return appDatabase.taskDao().getItembyId(id);
    }

    public void updateTask(TaskModel taskModel) {
        new updateAsyncTask(appDatabase).execute(taskModel);
    }
    public void deleteTask(TaskModel taskModel) {
        new deleteAsyncTask(appDatabase).execute(taskModel);
    }

    private class deleteAsyncTask extends AsyncTask<TaskModel,Void,Void> {
        private AppDatabase db;

        public deleteAsyncTask(AppDatabase appDatabase) {
            db =appDatabase;
        }

        @Override
        protected Void doInBackground(TaskModel... taskModels) {
            db.taskDao().deleteTask(taskModels[0]);
            return null;
        }
    }
    private class updateAsyncTask extends AsyncTask<TaskModel,Void,Void> {
        private AppDatabase db;

        public updateAsyncTask(AppDatabase appDatabase) {
            db =appDatabase;
        }

        @Override
        protected Void doInBackground(TaskModel... taskModels) {
            db.taskDao().updateTask(taskModels[0]);
            return null;
        }
    }
}
