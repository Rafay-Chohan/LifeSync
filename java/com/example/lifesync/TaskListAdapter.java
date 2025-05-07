package com.example.lifesync;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private ArrayList<com.example.lifesync.TaskModel> taskDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName,taskStatus,taskDeadline;

        LinearLayout containerLL;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            taskName = (TextView) view.findViewById(R.id.taskName);
            taskStatus = (TextView) view.findViewById(R.id.taskStatus);
            taskDeadline = (TextView) view.findViewById(R.id.taskDeadline);
            containerLL=(LinearLayout) view.findViewById(R.id.itemContainer);

        }
    }
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
            viewHolder.taskStatus.setBackgroundResource(R.drawable.statuscomp);
        }
        else if(status.equalsIgnoreCase("pending")){
            viewHolder.taskStatus.setBackgroundResource(R.drawable.statuspending);
        }
        else {
            viewHolder.taskStatus.setBackgroundResource(R.drawable.statusmissed);
        }
        com.example.lifesync.TaskModel completedTask=taskDataSet.get(position);
        if(completedTask.getTaskStatus().equalsIgnoreCase("pending")) {
            String date=completedTask.getTaskDeadline();
            Date currentTime = new Date();
            if(!date.equals(" ")){
                try {
                    String[] parts = date.split(" ");
                    Date deadline;

                    // Parse with time if available (format: yyyy-MM-dd HH:mm:ss)
                    if (parts.length >= 2 && !parts[1].isEmpty()) {
                        deadline = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
                    }
                    // Fallback to date-only (set time to 23:59:59 for end-of-day comparison)
                    else {
                        String endOfDay = parts[0] + " 23:59:59";
                        deadline = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endOfDay);
                    }
                    if(currentTime.after(deadline))
                    {
                        completedTask.setTaskStatus("Missed");
                        FirebaseFirestore.getInstance().collection("Tasks").document(taskDataSet.get(position).getTaskId()).set(completedTask).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                viewHolder.taskStatus.setBackgroundResource(R.drawable.statusmissed);
                                viewHolder.taskStatus.setText("Missed");
                            }
                        });
                    }
                }
                catch (Exception e) {
                    int nothing;
                }

            }


        }
        viewHolder.containerLL.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                PopupMenu popupMenu=new PopupMenu(view.getContext(),viewHolder.containerLL);
                popupMenu.inflate(R.menu.task_menu);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId()==R.id.Editbtn){
                            Intent intent = new Intent(view.getContext(), EditTaskActivity.class);
                            intent.putExtra("taskId", taskDataSet.get(position).getTaskId()); // Pass task ID to EditTaskActivity
                            view.getContext().startActivity(intent);
                        } else if(item.getItemId()==R.id.Deletebtn)
                        {
                            FirebaseFirestore.getInstance().collection("Tasks").document(taskDataSet.get(position).getTaskId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(view.getContext(),"Task Removed",Toast.LENGTH_SHORT).show();
                                    taskDataSet.remove(position);
                                    notifyItemRemoved(position);
                                }
                            });
                        } else if(item.getItemId()==R.id.Completebtn) {
                            com.example.lifesync.TaskModel completedTask=taskDataSet.get(position);
                            if(!(completedTask.getTaskStatus().equalsIgnoreCase("completed"))) {
                                completedTask.setTaskStatus("Completed");
                                FirebaseFirestore.getInstance().collection("Tasks").document(taskDataSet.get(position).getTaskId()).set(completedTask).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(view.getContext(), "Task Completed", Toast.LENGTH_SHORT).show();
                                        viewHolder.taskStatus.setBackgroundResource(R.drawable.statuscomp);
                                        viewHolder.taskStatus.setText("Completed");
                                    }
                                });
                            }else if(!(completedTask.getTaskStatus().equalsIgnoreCase("pending"))) {
                                completedTask.setTaskStatus("Pending");
                                FirebaseFirestore.getInstance().collection("Tasks").document(taskDataSet.get(position).getTaskId()).set(completedTask).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(view.getContext(), "Task Pending", Toast.LENGTH_SHORT).show();
                                        viewHolder.taskStatus.setBackgroundResource(R.drawable.statuspending);
                                        viewHolder.taskStatus.setText("Pending");
                                    }
                                });
                            }
                        }
                        return false;
                    }
                });
                return false;
            }
        });

    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return taskDataSet.size();
    }
}
