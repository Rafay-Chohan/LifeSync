package com.example.lifesync;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TaskFragment extends Fragment implements RefreshableFragment {

    MainActivity mainActivity;
    RecyclerView taskRV;
    TaskListAdapter taskListAdapter;
    Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Task Manager Query";
    ArrayList<com.example.lifesync.TaskModel> taskList = new ArrayList<>();
    public TaskFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(@NonNull Context context) {
        this.context=context;
        super.onAttach(context);

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

        FloatingActionButton btnAddTask = view.findViewById(R.id.btnAddTask);
        mainActivity = (MainActivity)getActivity();
        btnAddTask.setOnClickListener(v -> addTaskDialog());
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
    private void addTaskDialog(){
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_update_task, null);

        EditText etName=dialogView.findViewById(R.id.taskName);
        EditText etDeadline=dialogView.findViewById(R.id.taskDeadline);
        EditText etPriority=dialogView.findViewById(R.id.taskPriority);
        EditText etDuration=dialogView.findViewById(R.id.taskDuration);

        etDeadline.setOnClickListener(v -> showDateTimePicker(etDeadline));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add New Task")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    SaveTask(etName,etDeadline,etPriority,etDuration);
                })
                .create();


        // 6. Show the dialog
        dialog.show();
    }
    private void SaveTask(EditText nameET,EditText deadlineET,EditText priorityET,EditText durationET){
        String taskNameInput = nameET.getText().toString().trim();
        String taskDeadlineInput=deadlineET.getText().toString().trim();
        String taskPriorityInput = priorityET.getText().toString().trim();
        String taskDurationInput = durationET.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if(!taskNameInput.equals("")) {
            int Prioritytask=0;
            if(!taskPriorityInput.equals(""))
                Prioritytask=(int)Double.parseDouble(taskPriorityInput);
            if(taskPriorityInput.equals("") || (taskPriorityInput.length() > 0 && Prioritytask>=1 && Prioritytask<=5)) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                dateFormatter.setLenient(false);
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                timeFormatter.setLenient(false);
                com.example.lifesync.TaskModel taskModel = new com.example.lifesync.TaskModel("", taskNameInput, "Pending",Prioritytask,taskDeadlineInput,taskDurationInput, FirebaseAuth.getInstance().getUid());
                db.collection("Tasks").add(taskModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                                refreshContent();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                                Toast.makeText(requireContext(), "Error Adding in DB", Toast.LENGTH_SHORT).show();
                            }
                        });
                return;
            }
            else if(Integer.parseInt(taskPriorityInput)<1 || Integer.parseInt(taskPriorityInput)>5) {
                Toast.makeText(requireContext(), "Priority should be between 1 and 5", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(requireContext(), "Task name can't be empty", Toast.LENGTH_SHORT).show();
        }
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
}