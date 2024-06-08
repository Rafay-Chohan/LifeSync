package com.example.lifesync;

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
import android.widget.TextView;

import com.example.lifesync.model.ExpenseModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BudgetTrackerFragment extends Fragment {
    MainActivity mainActivity;
    RecyclerView ExpenseRV;
    ExpenseListAdapter expenseListAdapter;
    int spent=0;
    int income=10000;
    int saving=0;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Expense Query";
    ArrayList<ExpenseModel> expenseList = new ArrayList<>();
    public BudgetTrackerFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_tracker, container, false);
        mainActivity = (MainActivity)getActivity();
        TextView spt=view.findViewById(R.id.Spent);
        TextView svg=view.findViewById(R.id.Savings);
        TextView inc=view.findViewById(R.id.Income);
        ExpenseRV = view.findViewById(R.id.expenseListRV);
        expenseListAdapter = new ExpenseListAdapter(expenseList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        ExpenseRV.setLayoutManager(layoutManager);
        ExpenseRV.setAdapter(expenseListAdapter);

        Button b1 = view.findViewById(R.id.btnAddExpense);
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
                                spent+=ExpenseModel.getAmount();
                                expenseList.add(ExpenseModel);
                            }
                            expenseListAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        spt.setText("SPENT:\nRs."+Integer.toString(spent));
                        saving=income-spent;
                        svg.setText("SAVING:\nRs."+Integer.toString(saving));
                        inc.setText("INCOME:\nRs."+Integer.toString(income));

                    }
                });
        return view;
    }
}