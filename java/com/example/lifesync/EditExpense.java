package com.example.lifesync;



import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.lifesync.model.ExpenseModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditExpense extends AppCompatActivity {
    
    private EditText titleEditText;
    private EditText AmountEditText;
    private Spinner spinner;
    private EditText priorityEditText;
    private FloatingActionButton saveButton, btn2;
    String ExpenseCategoryInput;

    private String expId; // Store the Expense document ID
    String TAG = "Expense Manager Edit Query";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        titleEditText = findViewById(R.id.ExpenseName);
        AmountEditText = findViewById(R.id.ExpenseAmount);
        priorityEditText = findViewById(R.id.ExpensePriority);
        spinner = findViewById(R.id.ExpenseCategory);
        saveButton = findViewById(R.id.registerExpense);
       
        // Get the Expense ID from the intent
        expId = getIntent().getStringExtra("expId");

        // Pre-populate fields with existing Expense data (call a method to fetch data)
        populateExpenseData(expId);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_items, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item
                ExpenseCategoryInput = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do something when nothing is selected, if needed
            }
        });

        saveButton.setOnClickListener(v -> {
            String ExpenseNameInput = titleEditText.getText().toString().trim();
            String ExpenseAmountInput = AmountEditText.getText().toString().trim();
            String ExpensePriorityInput = priorityEditText.getText().toString().trim();

            if(!ExpenseNameInput.equals("")) {
                if(!ExpenseAmountInput.equals("")){
                    int Amount=(int)Double.parseDouble(ExpenseAmountInput);
                    int PriorityExp=0;
                    if(!ExpensePriorityInput.equals(""))
                        PriorityExp=(int)Double.parseDouble(ExpensePriorityInput);
                    if(ExpensePriorityInput.equals("") || (ExpensePriorityInput.length() > 0 && PriorityExp>=1 && PriorityExp<=3)){
                        updateExpenseInFirestore();
                        finish();}
                    else{
                        priorityEditText.setError("Priority should be between 1 and 3");
                    }
                }
                else {
                    AmountEditText.setError("Expense Amount can't be empty");
                }
            }
            else {
                titleEditText.setError("Expense name can't be empty");
            }
        });
        btn2 =(FloatingActionButton)findViewById(R.id.backAddExpense);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void populateExpenseData(String expId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ExpenseRef = db.collection("Expenses").document(expId);

        ExpenseRef.get()
                .addOnSuccessListener(documentSnapshot -> { 
                    if (documentSnapshot.exists()) {
                        ExpenseModel Expense = documentSnapshot.toObject(ExpenseModel.class);
                        if (Expense != null) {
                            titleEditText.setText(Expense.getName());
                            AmountEditText.setText(Integer.toString(Expense.getAmount()));
                            spinner.setPrompt(ExpenseCategoryInput);
                            priorityEditText.setText(Integer.toString(Expense.getExpPriority()));
                            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                                    R.array.category_items, android.R.layout.simple_spinner_item);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                            String compareValue = Expense.getCategory();
                            if (compareValue != null) {
                                int spinnerPosition = adapter.getPosition(compareValue);
                                spinner.setSelection(spinnerPosition);
                            }
                        } else {
                            Toast.makeText(EditExpense.this, "Failed to retrieve Expense data!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.w("Firestore", "Error getting documents: ", exception);
                });
    }

    private void updateExpenseInFirestore() {
        String newTitle = titleEditText.getText().toString().trim();
        String newAmount = AmountEditText.getText().toString().trim();
        String newPriority = priorityEditText.getText().toString().trim();
        String newCategory = ExpenseCategoryInput;

        int PriorityExpense=0;
        if(!newPriority.equals(""))
            PriorityExpense=(int)Double.parseDouble(newPriority);
        int Amount=(int)Double.parseDouble(newAmount);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ExpenseRef = db.collection("Expenses").document(expId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newTitle);
        updates.put("expPriority", PriorityExpense);
        updates.put("category", newCategory);
        updates.put("amount", Amount);

        ExpenseRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    Toast.makeText(EditExpense.this, "Expense updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity and potentially refresh Expense list
                })
                .addOnFailureListener(exception -> {
                    // Update failed
                    Toast.makeText(EditExpense.this, "Failed to update Expense!", Toast.LENGTH_SHORT).show();
                });

    }
}