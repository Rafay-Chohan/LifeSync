package com.example.lifesync;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private ArrayList<com.example.lifesync.TaskModel> taskDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName,taskStatus,taskDeadline;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            taskName = (TextView) view.findViewById(R.id.taskName);
            taskStatus = (TextView) view.findViewById(R.id.taskStatus);
            taskDeadline = (TextView) view.findViewById(R.id.taskDeadline);
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public TaskListAdapter(ArrayList<com.example.lifesync.TaskModel> taskDataSet) {
        this.taskDataSet = taskDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_task, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.taskName.setText(taskDataSet.get(position).getTaskName());
        viewHolder.taskStatus.setText(taskDataSet.get(position).getTaskStatus());
        viewHolder.taskDeadline.setText(taskDataSet.get(position).getTaskDeadline());
        String status=taskDataSet.get(position).getTaskStatus();
        if(status.equalsIgnoreCase("completed")){
            viewHolder.taskStatus.setBackgroundColor(Color.parseColor("#04E824"));
        }
        else if(status.equalsIgnoreCase("pending")){
            viewHolder.taskStatus.setBackgroundColor(Color.parseColor("#F6F740"));
        }
        else {
            viewHolder.taskStatus.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }

    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return taskDataSet.size();
    }
}
