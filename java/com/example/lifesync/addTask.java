package com.example.lifesync;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.example.lifesync.TaskModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class addTask extends Activity {
    Button btn,btn2;
    String TAG = "LIFESYNC";
    int id=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

        btn =(Button)findViewById(R.id.registerTask);
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
                if(taskNameInput!=null)
                {
                    TaskModel taskModel = new TaskModel(id++, taskNameInput, "Pending",taskPriorityInput,taskDeadlineInput,taskDurationInput);
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
                }

                Intent intent = new Intent(addTask.this,MainActivity.class);
                startActivity(intent);
            }
        });
        btn2 =(Button)findViewById(R.id.backAddTask);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(addTask.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}

