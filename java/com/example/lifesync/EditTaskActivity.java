package com.example.lifesync;



import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditTaskActivity extends Activity {
    EditText date,time;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    private EditText titleEditText;
    private EditText taskPriorityEditText;
    private EditText taskDeadlineEditText;
    private EditText taskDeadlineTimeEditText;
    private EditText taskDurationEditText;
    private Button saveButton, btn2;

    private String taskId; // Store the task document ID
    String TAG = "Task Manager Edit Query";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        titleEditText = findViewById(R.id.taskName);
        taskPriorityEditText = findViewById(R.id.taskPriority);
        taskDeadlineEditText = findViewById(R.id.taskDeadlineDate);
        taskDeadlineTimeEditText = findViewById(R.id.taskDeadlineTime);
        taskDurationEditText = findViewById(R.id.taskDuration);
        saveButton = findViewById(R.id.registerTask);
        date=findViewById(R.id.taskDeadlineDate);
        time=findViewById(R.id.taskDeadlineTime);
        Calendar calendar=Calendar.getInstance();
        final int day=calendar.get(Calendar.DAY_OF_MONTH);
        final int year=calendar.get(Calendar.YEAR);
        final int month=calendar.get(Calendar.MONTH);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog=new DatePickerDialog(EditTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.setText(year+"-"+(month+1)+"-"+dayOfMonth);
                    }
                },year,month,day);
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePickerDialog.show();
            }

        });
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(EditTaskActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        if(minutes>10)
                            time.setText(hourOfDay + ":" + minutes+":00");
                        else
                            time.setText(hourOfDay + ":0" + minutes+":00");
                    }
                }, 0, 0, false);
                timePickerDialog.show();
            }
        });

        // Get the task ID from the intent
        taskId = getIntent().getStringExtra("taskId");

        // Pre-populate fields with existing task data (call a method to fetch data)
        populateTaskData(taskId);

        saveButton.setOnClickListener(v -> {
            titleEditText =  findViewById(R.id.taskName);
            String taskNameInput = titleEditText.getText().toString().trim();
            taskPriorityEditText = findViewById(R.id.taskPriority);
            String taskPriorityInput = taskPriorityEditText.getText().toString().trim();

            if(!taskNameInput.equals("")) {
                int Prioritytask=0;
                if(!taskPriorityInput.equals(""))
                    Prioritytask=(int)Double.parseDouble(taskPriorityInput);
                if(taskPriorityInput.equals("") || (taskPriorityInput.length() > 0 && Prioritytask>=1 && Prioritytask<=5)) {
                    updateTaskInFirestore();
                    finish();
                }
                else if(Integer.parseInt(taskPriorityInput)<1 || Integer.parseInt(taskPriorityInput)>5) {
                    taskPriorityEditText.setError("Priority should be between 1 and 5");
                }
            }
            else {
                titleEditText.setError("Task name can't be empty");
            }
        });
        btn2 =(Button)findViewById(R.id.backAddTask);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void populateTaskData(String taskId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("Tasks").document(taskId);

        taskRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        com.example.lifesync.TaskModel task = documentSnapshot.toObject(com.example.lifesync.TaskModel.class);
                        if (task != null) {
                            titleEditText.setText(task.getTaskName());
                            taskPriorityEditText.setText(Integer.toString(task.getTaskPriority()));
                            String date=task.getTaskDeadline();
                            if(!date.equals(" ")){
                                String[] parts = date.split(" ");
                                taskDeadlineEditText.setText(parts[0]);
                                if(parts.length>1)
                                    taskDeadlineTimeEditText.setText(parts[1]);
                            }
                            taskDurationEditText.setText(task.getTaskDuration());
                        } else {
                            Toast.makeText(EditTaskActivity.this, "Failed to retrieve task data!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.w("Firestore", "Error getting documents: ", exception);
                });
    }

    private void updateTaskInFirestore() {
        String newTitle = titleEditText.getText().toString().trim();
        String newPriority = taskPriorityEditText.getText().toString().trim();

        int Prioritytask=0;
        if(!newPriority.equals(""))
            Prioritytask=(int)Double.parseDouble(newPriority);
        String newDuration = taskDurationEditText.getText().toString().trim();
        String newDeadline = taskDeadlineEditText.getText().toString().trim();
        String newDeadlineTime = taskDeadlineTimeEditText.getText().toString().trim();
        String taskDeadlineInput=newDeadline+" "+newDeadlineTime;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("Tasks").document(taskId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("taskName", newTitle);
        updates.put("taskPriority", Prioritytask);
        updates.put("taskDuration", newDuration);
        updates.put("taskDeadline", taskDeadlineInput);

        taskRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    Toast.makeText(EditTaskActivity.this, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity and potentially refresh task list
                })
                .addOnFailureListener(exception -> {
                    // Update failed
                    Toast.makeText(EditTaskActivity.this, "Failed to update task!", Toast.LENGTH_SHORT).show();
                });

    }
}