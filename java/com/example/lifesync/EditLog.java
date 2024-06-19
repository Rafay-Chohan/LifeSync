package com.example.lifesync;



import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditLog extends AppCompatActivity {

    private EditText titleEditText;
    private EditText dataEditText;
    private FloatingActionButton saveButton, btn2;

    private String LogId; // Store the Log document ID
    String TAG = "Log Manager Edit Query";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log);

        titleEditText = findViewById(R.id.logName);
        dataEditText = findViewById(R.id.logData);
        saveButton = findViewById(R.id.registerLog);

        // Get the Log ID from the intent
        LogId = getIntent().getStringExtra("logId");

        // Pre-populate fields with existing Log data (call a method to fetch data)
        populateLogData(LogId);

        saveButton.setOnClickListener(v -> {
            updateLogInFirestore();
        });
        btn2 =(FloatingActionButton)findViewById(R.id.backAddLog);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void populateLogData(String LogId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference LogRef = db.collection("Logs").document(LogId);

        LogRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        com.example.lifesync.LogModel Log = documentSnapshot.toObject(com.example.lifesync.LogModel.class);
                        if (Log != null) {
                            titleEditText.setText(Log.getName());
                            dataEditText.setText(Log.getData());
                        } else {
                            Toast.makeText(EditLog.this, "Failed to retrieve Log data!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.w("Firestore", "Error getting documents: ", exception);
                });
    }

    private void updateLogInFirestore() {
        String newTitle = titleEditText.getText().toString().trim();
        String newData = dataEditText.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference LogRef = db.collection("Logs").document(LogId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newTitle);
        updates.put("data", newData);
        LogRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    Toast.makeText(EditLog.this, "Log updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity and potentially refresh Log list
                })
                .addOnFailureListener(exception -> {
                    // Update failed
                    Toast.makeText(EditLog.this, "Failed to update Log!", Toast.LENGTH_SHORT).show();
                });

    }

}