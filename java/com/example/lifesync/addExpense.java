package com.example.lifesync;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.example.lifesync.model.ExpenseModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class addExpense extends Activity {
    Button btn,btn2;
    String TAG = "LIFESYNC";
    String ExpenseCategoryInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expense);

        Spinner spinner = findViewById(R.id.ExpenseCategory);
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

        btn =(Button)findViewById(R.id.registerExpense);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText nameET =  findViewById(R.id.ExpenseName);
                String ExpenseNameInput = nameET.getText().toString().trim();

                EditText AmountET =  findViewById(R.id.ExpenseAmount);
                String ExpenseAmountInput = AmountET.getText().toString().trim();

                EditText PriorityET =  findViewById(R.id.ExpensePriority);
                String ExpensePriorityInput = PriorityET.getText().toString().trim();

                String CurrentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                if(!ExpenseNameInput.equals("")) {
                    if(!ExpenseAmountInput.equals("")){
                        int Amount=(int)Double.parseDouble(ExpenseAmountInput);
                        int PriorityExp=0;
                        if(!ExpensePriorityInput.equals(""))
                            PriorityExp=(int)Double.parseDouble(ExpensePriorityInput);
                        if(ExpensePriorityInput.equals("") || (ExpensePriorityInput.length() > 0 && PriorityExp>=1 && PriorityExp<=3)){
                        ExpenseModel expenseModel=new ExpenseModel("",ExpenseNameInput,CurrentDate,FirebaseAuth.getInstance().getUid(),ExpenseCategoryInput,Amount, PriorityExp);
                        db.collection("Expenses").add(expenseModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
                        finish();}
                        else{
                            PriorityET.setError("Priority should be between 1 and 3");
                        }
                    }
                    else {
                        AmountET.setError("Expense Amount can't be empty");
                    }
                }
                else {
                    nameET.setError("Expense name can't be empty");
                }
            }
        });
        btn2 =(Button)findViewById(R.id.backAddExpense);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

