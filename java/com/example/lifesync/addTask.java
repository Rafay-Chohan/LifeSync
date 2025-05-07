package com.example.lifesync;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifesync.TaskModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class addTask extends AppCompatActivity{
    FloatingActionButton btn,btn2;
    EditText date,time;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    String TAG = "LIFESYNC";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);
        date=findViewById(R.id.taskDeadlineDate);
        time=findViewById(R.id.taskDeadlineTime);
        Calendar calendar = Calendar.getInstance();
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Date Picker
                DatePickerDialog datePicker = new DatePickerDialog(
                        addTask.this,
                        (view, year, month, day) -> {
                            calendar.set(year, month, day);

                            // Format: "Jun 15, 2:30 PM"
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            date.setText(sdf.format(calendar.getTime()));
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePicker.show();
            }

        });
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Time Picker (shows after date is selected)
                TimePickerDialog timePicker = new TimePickerDialog(
                        addTask.this,
                        (view1, hour, minute) -> {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);

                            // Format: "Jun 15, 2:30 PM"
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                            time.setText(sdf.format(calendar.getTime()));
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                );
                timePicker.show();
            }
        });

        btn =(FloatingActionButton) findViewById(R.id.registerTask);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameET = (EditText) findViewById(R.id.taskName);
                String taskNameInput = nameET.getText().toString().trim();

                EditText deadlinedateET = (EditText) findViewById(R.id.taskDeadlineDate);
                String taskDeadlineDInput = deadlinedateET.getText().toString().trim();
                EditText deadlinetimeET = (EditText) findViewById(R.id.taskDeadlineTime);
                String taskDeadlineTInput = deadlinetimeET.getText().toString().trim();
                String taskDeadlineInput=taskDeadlineDInput+" "+taskDeadlineTInput;
                EditText priorityET = (EditText) findViewById(R.id.taskPriority);
                String taskPriorityInput = priorityET.getText().toString().trim();
                EditText durationET = (EditText) findViewById(R.id.taskDuration);
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
                        TaskModel taskModel = new TaskModel("", taskNameInput, "Pending",Prioritytask,taskDeadlineInput,taskDurationInput, FirebaseAuth.getInstance().getUid());
                        db.collection("Tasks").add(taskModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);
                                    }
                                });
                        finish();
                    }
                    else if(Integer.parseInt(taskPriorityInput)<1 || Integer.parseInt(taskPriorityInput)>5) {
                        priorityET.setError("Priority should be between 1 and 5");
                    }
                }
                else {
                    nameET.setError("Task name can't be empty");
                }
            }
        });
        btn2 =(FloatingActionButton) findViewById(R.id.backAddTask);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

