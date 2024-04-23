package com.example.lifesync;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import android.widget.Button;

public class TaskManagerFragment extends Fragment {

MainActivity mainActivity;
RecyclerView taskRV;
TaskListAdapter taskListAdapter;
ArrayList<com.example.lifesync.TaskModel>   taskList=new ArrayList<>();
    public TaskManagerFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_manager, container, false);
        mainActivity = (MainActivity)getActivity();

        taskRV=view.findViewById(R.id.taskListRV);
        taskList.add(new com.example.lifesync.TaskModel("1","Demo Task1","Pending","3","2024-4-26 16:00:00","2 hours"));
        taskList.add(new com.example.lifesync.TaskModel("2","Demo Task2","Completed","3","2024-4-24 16:00:00","2 hours"));
        taskList.add(new com.example.lifesync.TaskModel("3","Demo Task3","Pending","3","2024-4-25 16:00:00","2 hours"));
        taskListAdapter=new TaskListAdapter(taskList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        taskRV.setLayoutManager(layoutManager);
        taskRV.setAdapter(taskListAdapter);

        Button b1 = view.findViewById(R.id.btnAddTask);
        mainActivity = (MainActivity)getActivity();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), addTask.class);
                startActivity(intent);
            }
        });

        return view;
    }
}