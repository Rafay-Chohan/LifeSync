package com.example.lifesync;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import android.widget.Button;

public class TaskManagerFragment extends Fragment {

MainActivity mainActivity;

    public TaskManagerFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_manager, container, false);
        ListView listViewTasks = view.findViewById(R.id.listViewTasks);

        // Dummy task data
        ArrayList<String> tasks = new ArrayList<>();
        tasks.add("Task 1");
        tasks.add("Task 2");
        tasks.add("Task 3");
        tasks.add("Task 4");
        tasks.add("Task 5");
        tasks.add("Task 6");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, tasks);

        listViewTasks.setAdapter(adapter);

        Button b1 = view.findViewById(R.id.btnAddTask);
        mainActivity = (MainActivity)getActivity();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), addTask.class);
                startActivity(i);
            }
        });

        return view;
    }
}