package com.example.lifesync;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private ArrayList<com.example.lifesync.TaskModel> taskDataSet;
    Context context;

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
        context = viewHolder.itemView.getContext();
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

        viewHolder.containerLL.setOnClickListener(v -> {
            editTaskDialog(position);
        });
    }
    private void editTaskDialog(int position){
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_update_task, null);

        EditText etName=dialogView.findViewById(R.id.taskName);
        EditText etDeadline=dialogView.findViewById(R.id.taskDeadline);
        EditText etPriority=dialogView.findViewById(R.id.taskPriority);
        EditText etDuration=dialogView.findViewById(R.id.taskDuration);

        etDeadline.setOnClickListener(v -> showDateTimePicker(etDeadline));
        populateTaskData(position,etName,etDeadline,etPriority,etDuration);

        View titleView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task_title, null);
        TextView titleTextView = titleView.findViewById(R.id.dialog_title_text);
        titleTextView.setText("Update Task");

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCustomTitle(titleView)
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    updateTaskInFirestore(position,etName,etDeadline,etPriority,etDuration);
                })
                .create();

        dialog.show();
    }
    private void showDateTimePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();

        // Date Picker
        DatePickerDialog datePicker = new DatePickerDialog(
                context,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);

                    // Time Picker (shows after date is selected)
                    TimePickerDialog timePicker = new TimePickerDialog(
                            context,
                            (view1, hour, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);

                                // Format: "Jun 15, 2:30 PM"
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                targetEditText.setText(sdf.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                    );
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePicker.show();
    }
    private void populateTaskData(int position,EditText nameET,EditText deadlineET,EditText priorityET,EditText durationET) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("Tasks").document(taskDataSet.get(position).getTaskId());
        taskRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        com.example.lifesync.TaskModel task = documentSnapshot.toObject(com.example.lifesync.TaskModel.class);
                        if (task != null) {
                            nameET.setText(task.getTaskName());
                            if(task.getTaskPriority()!=0)
                                priorityET.setText(Integer.toString(task.getTaskPriority()));
                            deadlineET.setText(task.getTaskDeadline());
                            durationET.setText(task.getTaskDuration());
                        } else {
                            Toast.makeText(context, "Failed to retrieve task data!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.w("Firestore", "Error getting documents: ", exception);
                });
    }

    private void updateTaskInFirestore(int position,EditText nameET,EditText deadlineET,EditText priorityET,EditText durationET) {
        String newTitle = nameET.getText().toString().trim();
        String newPriority = priorityET.getText().toString().trim();
        int Prioritytask=0;
        if(!newPriority.equals(""))
            Prioritytask=(int)Double.parseDouble(newPriority);
        String newDuration = durationET.getText().toString().trim();
        String taskDeadlineInput=deadlineET.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("Tasks").document(taskDataSet.get(position).getTaskId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("taskName", newTitle);
        updates.put("taskPriority", Prioritytask);
        taskDataSet.get(position).setTaskPriority(Prioritytask);
        updates.put("taskDuration", newDuration);
        updates.put("taskDeadline", taskDeadlineInput);
        updates.put("taskStatus","Pending");

        taskRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    taskDataSet.get(position).setTaskName(newTitle);
                    taskDataSet.get(position).setTaskDuration(newDuration);
                    taskDataSet.get(position).setTaskDeadline(taskDeadlineInput);
                    taskDataSet.get(position).setTaskStatus("Pending");
                    Toast.makeText(context, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                })
                .addOnFailureListener(exception -> {
                    // Update failed
                    Toast.makeText(context, "Failed to update task!", Toast.LENGTH_SHORT).show();
                });
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return taskDataSet.size();
    }
}
