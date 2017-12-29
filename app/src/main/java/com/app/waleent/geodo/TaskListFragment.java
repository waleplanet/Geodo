package com.app.waleent.geodo;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;

public class TaskListFragment extends Fragment implements TaskRecyclerAdapterCallback {

    // TODO: Customize parameters
    private int mColumnCount = 1;
    private String TAG = TaskListFragment.class.getSimpleName();
    //private OnListFragmentInteractionListener mListener;
    private TaskRecyclerViewAdapter recyclerViewAdapter;
    private TaskListViewModel viewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recyclerViewAdapter = new TaskRecyclerViewAdapter(new ArrayList<>(),this);

        viewModel = ViewModelProviders.of(this).get(TaskListViewModel.class);
        viewModel.getTaskList().observe(this, taskModelList ->  {
//            @Override
//            public void onChanged(@Nullable List<TaskModel> taskModelList) {
                recyclerViewAdapter.addItems(taskModelList);
        //    }
        });
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_fragment_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        View recycleView = view.findViewById(R.id.task_list);
        if (recycleView instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) recycleView;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(recyclerViewAdapter);
        }

        return view;
    }

    @Override
    public void onDeleteMethod(TaskModel taskModel) {
        viewModel.deleteTask(taskModel);
    }

    @Override
    public void onUpdateMethod(TaskModel taskModel,boolean bool) {
        Log.d(TAG,"befor model: "+taskModel.getCompleted());
        taskModel.setCompleted(bool);
        Log.d(TAG,"befor model: "+taskModel.getCompleted());
        viewModel.updateTask(taskModel);

    }
}
