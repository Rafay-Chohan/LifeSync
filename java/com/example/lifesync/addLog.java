package com.example.lifesync;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import androidx.annotation.NonNull;

import com.example.lifesync.TaskModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class addLog extends Activity {
    Button btn,btn2;
    String TAG = "LIFESYNC 2";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_log);

        btn =(Button)findViewById(R.id.registerLog);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText nameET = (EditText) findViewById(R.id.logName);
                String logNameInput = nameET.getText().toString().trim();

                EditText dataET = (EditText) findViewById(R.id.logData);
                String logDataInput = dataET.getText().toString().trim();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                if(!logNameInput.equals("")) {
                        com.example.lifesync.LogModel logModel = new com.example.lifesync.LogModel("", logNameInput, logDataInput,"", FirebaseAuth.getInstance().getUid());
                        db.collection("Logs").add(logModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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

                        Intent intent = new Intent(addLog.this,MainActivity.class);
                        startActivity(intent);
                    }
                else {
                    nameET.setError("Task name can't be empty");
                }
            }
        });
        btn2 =(Button)findViewById(R.id.backAddLog);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(addLog.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}

