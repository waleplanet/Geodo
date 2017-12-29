package com.app.waleent.geodo;

/**
 * Created by wale on 11/7/17.
 */

interface TaskRecyclerAdapterCallback {
    void onDeleteMethod(TaskModel taskModel);
    void onUpdateMethod(TaskModel taskModel,boolean bool);
}
