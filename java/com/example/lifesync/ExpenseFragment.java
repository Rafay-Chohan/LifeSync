package com.example.lifesync;

import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lifesync.model.ExpenseModel;
import com.example.lifesync.model.IncomeModel;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExpenseFragment extends Fragment implements RefreshableFragment{
    MainActivity mainActivity;
    RecyclerView ExpenseRV;
    ExpenseListAdapter expenseListAdapter;
    int spent=0;
    int income=0;
    int saving=0;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Expense Query";
    TextView spt;
    TextView svg;
    TextView inc;
    ArrayList<ExpenseModel> expenseList = new ArrayList<>();
    public ExpenseFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        mainActivity = (MainActivity)getActivity();
        spt = view.findViewById(R.id.Spent);
        svg = view.findViewById(R.id.Savings);
        inc = view.findViewById(R.id.Income);
        ExpenseRV = view.findViewById(R.id.expenseListRV);
        expenseListAdapter = new ExpenseListAdapter(expenseList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        ExpenseRV.setLayoutManager(layoutManager);
        ExpenseRV.setAdapter(expenseListAdapter);

        FloatingActionButton b1 = view.findViewById(R.id.btnAddExpense);
        mainActivity = (MainActivity)getActivity();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), addExpense.class);
                startActivity(intent);
                expenseListAdapter.notifyDataSetChanged();
            }
        });
        db.collection("Expenses")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ExpenseModel ExpenseModel=document.toObject(ExpenseModel.class);
                                ExpenseModel.setExpId(document.getId());
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    Date date = sdf.parse(ExpenseModel.getDate());

                                    // Get current month/year
                                    Calendar currentCal = Calendar.getInstance();
                                    Calendar inputCal = Calendar.getInstance();
                                    inputCal.setTime(date);
                                    if((inputCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) )&& (inputCal.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH)))
                                        spent+=ExpenseModel.getAmount();
                                }catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                expenseList.add(ExpenseModel);
                                db.collection("Incomes")
                                        .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.d("Retrieve Income", document.getId() + " => " + document.getData());
                                                        IncomeModel incomeModel = document.toObject(IncomeModel.class);
                                                        incomeModel.setDocId(document.getId());
                                                        income=incomeModel.getIncome();
                                                        spt.setText("SPENT:\nRs."+Integer.toString(spent));
                                                        saving=income-spent;
                                                        svg.setText("SAVING:\nRs."+Integer.toString(saving));
                                                        inc.setText("INCOME:\nRs."+Integer.toString(income));
                                                    }
                                                }else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });

                            }
                            expenseListAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });



        TextView incomeTV = view.findViewById(R.id.Income);
        incomeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(mainActivity);
                dialog.setContentView(R.layout.add_income_dialog);
                dialog.show();
                EditText incomeSet = dialog.findViewById(R.id.incomeInput);
                incomeSet.setText(String.valueOf(income));
                incomeSet.setSelection(incomeSet.getText().length()); //cursor at end of income
                Button btn = dialog.findViewById(R.id.confirmIncome);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String incomeInput = incomeSet.getText().toString().trim();
                        if(!incomeInput.equals("")){
                            income = (int)Double.parseDouble(incomeInput);
                            updateIncome(income);
                            saving = income - spent;
                            spt.setText("SPENT:\nRs."+Integer.toString(spent));
                            svg.setText("SAVING:\nRs."+Integer.toString(saving));
                            inc.setText("INCOME:\nRs."+Integer.toString(income));

                        }
                        dialog.dismiss();
                    }
                });

            }
        });

        return view;
    }



    public static void updateIncome(int income){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Incomes")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // no existing document
                                IncomeModel incomeModel = new IncomeModel(FirebaseAuth.getInstance().getUid(), "", income);
                                db.collection("Incomes").add(incomeModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d("Income Update", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                updateIncomeDB(income, documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Income Update", "Error adding document", e);
                                            }
                                        });
                            } else {
                                // update existing document
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Retrieve Income", document.getId() + " => " + document.getData());
                                    IncomeModel incomeModel = document.toObject(IncomeModel.class);
                                    incomeModel.setDocId(document.getId());
                                    updateIncomeDB(income, document.getId());
                                }
                            }
                        } else {
                            Log.d("Income Updation", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    public static void updateIncomeDB(int income,String docID ){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference ExpenseRef = db.collection("Incomes").document(docID);
        Map<String, Object> updates = new HashMap<>();
        updates.put("income", income);

        ExpenseRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                })
                .addOnFailureListener(exception -> {
                    // Update failed
                });
    }
    @Override
    public void refreshContent()
    {
        spent=0;
        saving=0;
        expenseList.clear();
        expenseListAdapter.notifyDataSetChanged();

        db.collection("Expenses")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ExpenseModel ExpenseModel=document.toObject(ExpenseModel.class);
                                ExpenseModel.setExpId(document.getId());
                                spent+=ExpenseModel.getAmount();
                                expenseList.add(ExpenseModel);
                                db.collection("Incomes")
                                        .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.d("Retrieve Income", document.getId() + " => " + document.getData());
                                                        IncomeModel incomeModel = document.toObject(IncomeModel.class);
                                                        incomeModel.setDocId(document.getId());
                                                        income=incomeModel.getIncome();
                                                        spt.setText("SPENT:\nRs."+Integer.toString(spent));
                                                        saving=income-spent;
                                                        svg.setText("SAVING:\nRs."+Integer.toString(saving));
                                                        inc.setText("INCOME:\nRs."+Integer.toString(income));
                                                    }
                                                }else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });

                            }
                            expenseListAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


}