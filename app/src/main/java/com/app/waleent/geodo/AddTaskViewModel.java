package com.app.waleent.geodo;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.os.AsyncTask;

/**
 * Created by wale on 10/30/17.
 */

public class AddTaskViewModel extends AndroidViewModel {
    private static AppDatabase appDatabase;

    public AddTaskViewModel(Application application) {
        super(application);
        appDatabase =AppDatabase.getInstance(this.getApplication());
    }
    public void addTask(final TaskModel taskModel){
        new addAsyncTask(appDatabase).execute(taskModel);

    }

    private class addAsyncTask extends AsyncTask<TaskModel,Void,Void>{

        private AppDatabase db;
        public addAsyncTask(AppDatabase appDatabase) {
            db=appDatabase;
        }

        @Override
        protected Void doInBackground(TaskModel... taskModels) {
            db.taskDao().addTask(taskModels[0]);
            return null;
        }
    }
}
