package com.app.waleent.geodo;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wale on 11/1/17.
 */

class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.RecyclerViewHolder> {

    private List<TaskModel> taskModelList;

    private TaskRecyclerAdapterCallback mTaskRecyclerAdapterCallback;

    public  TaskRecyclerViewAdapter(List<TaskModel> taskModelList,TaskRecyclerAdapterCallback taskRecyclerAdapterCallback){
        this.taskModelList = taskModelList;
        //this.clickListener = clickListener;
        this.mTaskRecyclerAdapterCallback = taskRecyclerAdapterCallback;
    }

    @Override
    public TaskRecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.recylcer_task_item,parent,false));
    }

    @Override
    public void onBindViewHolder(TaskRecyclerViewAdapter.RecyclerViewHolder holder, int position) {
        TaskModel taskModel = taskModelList.get(position);
        holder.titleTextView.setText(taskModel.getTitle());
        holder.locationTextView.setText(taskModel.getLocation());
        holder.itemView.setTag(taskModel);
        holder.deleteButton.setOnClickListener(view -> mTaskRecyclerAdapterCallback.onDeleteMethod(taskModel));
        holder.completeStatusView.setChecked(taskModel.getCompleted());
        holder.completeStatusView.setOnCheckedChangeListener((compoundButton, b) -> {
            mTaskRecyclerAdapterCallback.onUpdateMethod(taskModel,b);
        });
    }

    @Override
    public int getItemCount() {
        return taskModelList.size();
    }

    public void addItems(List<TaskModel> taskModelList){
        this.taskModelList = taskModelList;
        notifyDataSetChanged();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView locationTextView;
        private CheckBox completeStatusView;
        private ImageButton deleteButton;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            titleTextView =  itemView.findViewById(R.id.title);
            locationTextView = itemView.findViewById(R.id.location);
            deleteButton = itemView.findViewById(R.id.delete_task_btn);
            completeStatusView = itemView.findViewById(R.id.complete_btn);

        }
    }
}
