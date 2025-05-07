package com.example.lifesync;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TaskFragment extends Fragment implements RefreshableFragment {

    MainActivity mainActivity;
    RecyclerView taskRV;
    TaskListAdapter taskListAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Task Manager Query";
    ArrayList<com.example.lifesync.TaskModel> taskList = new ArrayList<>();
    public TaskFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        mainActivity = (MainActivity)getActivity();

        taskRV = view.findViewById(R.id.taskListRV);
        taskListAdapter = new TaskListAdapter(taskList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        taskRV.setLayoutManager(layoutManager);
        taskRV.setAdapter(taskListAdapter);

        FloatingActionButton b1 = view.findViewById(R.id.btnAddTask);
        mainActivity = (MainActivity)getActivity();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), addTask.class);
                startActivity(intent);
                taskListAdapter.notifyDataSetChanged();
            }
        });
//        db.collection("Tasks")
//                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
//                .orderBy("taskStatus", Query.Direction.DESCENDING)
//                .orderBy("taskDeadline", Query.Direction.ASCENDING)
//                .orderBy("taskPriority", Query.Direction.DESCENDING)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                                com.example.lifesync.TaskModel taskModel=document.toObject(com.example.lifesync.TaskModel.class);
//                                taskModel.setTaskId(document.getId());
//                                taskList.add(taskModel);
//                            }
//                            taskListAdapter.notifyDataSetChanged();
//                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
        refreshContent();
        return view;
    }
    @Override
    public void refreshContent() {
        // Clear existing data
        taskList.clear();
        taskListAdapter.notifyDataSetChanged();

        // Fetch fresh data from Firestore
        db.collection("Tasks")
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
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                com.example.lifesync.TaskModel taskModel = document.toObject(com.example.lifesync.TaskModel.class);
                                taskModel.setTaskId(document.getId());
                                taskList.add(taskModel);
                            }
                            taskListAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}