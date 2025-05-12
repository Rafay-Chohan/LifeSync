package com.example.lifesync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TaskRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private ArrayList<com.example.lifesync.TaskModel> taskList = new ArrayList<>();

    public TaskRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        loadTasks();
    }

    private void loadTasks() {
        Log.d("Widgeting","Added Task");
        FirebaseFirestore.getInstance().collection("Tasks")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .orderBy("taskStatus", Query.Direction.DESCENDING)
                .orderBy("taskDeadline", Query.Direction.ASCENDING)
                .orderBy("taskPriority", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                com.example.lifesync.TaskModel taskModel = document.toObject(com.example.lifesync.TaskModel.class);
                                taskModel.setTaskId(document.getId());

                                boolean exists = false;
                                for (com.example.lifesync.TaskModel existingTask : taskList) {
                                    if (existingTask.getTaskId().equals(taskModel.getTaskId())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists && "Pending".equals(taskModel.getTaskStatus())) {
                                    taskList.add(taskModel);
                                    Log.d("Widgeting","Added Task");
                                }

                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });;
    }

    @Override
    public void onDataSetChanged() {
        loadTasks();
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        com.example.lifesync.TaskModel task = taskList.get(position);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_item);
        views.setTextViewText(R.id.taskName, "Name: "+task.getTaskName());
        views.setTextViewText(R.id.taskDeadline, "Due: "+task.getTaskDeadline());
        views.setTextViewText(R.id.taskStatus, "Status: "+task.getTaskStatus());

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("task_id", task.getTaskId());
        views.setOnClickFillInIntent(R.id.widget_task_item, fillInIntent);

        return views;
    }

    @Override public RemoteViews getLoadingView() { return null; }
    @Override public int getViewTypeCount() { return 1; }
    @Override public long getItemId(int position) { return position; }
    @Override public boolean hasStableIds() { return true; }
    @Override public void onDestroy() { taskList.clear(); }
}