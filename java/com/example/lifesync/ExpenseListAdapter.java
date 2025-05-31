package com.example.lifesync;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.model.ExpenseModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ViewHolder> {

    private ArrayList<ExpenseModel> ExpenseDataSet;
    Context context;
    String ExpenseCategoryInput;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView ExpenseName,ExpenseAmount,ExpenseDate,ExpenseCategory;

        LinearLayout containerLL;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            ExpenseName = (TextView) view.findViewById(R.id.ExpenseName);
            ExpenseAmount=(TextView) view.findViewById(R.id.ExpenseAmount);
            ExpenseDate = (TextView) view.findViewById(R.id.ExpenseDate);
            ExpenseCategory = (TextView) view.findViewById(R.id.ExpenseCategory);
            containerLL=(LinearLayout) view.findViewById(R.id.ExpenseContainer);
        }
    }

    public ExpenseListAdapter(ArrayList<ExpenseModel> ExpenseDataSet) {
        this.ExpenseDataSet = ExpenseDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_expense, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        context = viewHolder.itemView.getContext();
        viewHolder.ExpenseName.setText(ExpenseDataSet.get(position).getName());
        viewHolder.ExpenseAmount.setText("Rs."+Integer.toString(ExpenseDataSet.get(position).getAmount()));
        viewHolder.ExpenseDate.setText(ExpenseDataSet.get(position).getDate());
        viewHolder.ExpenseCategory.setText(ExpenseDataSet.get(position).getCategory());

        viewHolder.containerLL.setOnClickListener(v -> {
            editExpenseDialog(position);
        });
    }

    private void editExpenseDialog(int position){
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_update_expense, null);

        EditText etName=dialogView.findViewById(R.id.ExpenseName);
        EditText etAmount=dialogView.findViewById(R.id.ExpenseAmount);
        EditText etPriority=dialogView.findViewById(R.id.ExpensePriority);
        Spinner spinner=dialogView.findViewById(R.id.ExpenseCategory);

        populateExpenseData(position,etName,etAmount,spinner,etPriority);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
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

        View titleView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task_title, null);
        TextView titleTextView = titleView.findViewById(R.id.dialog_title_text);
        titleTextView.setText("Update Expense");

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCustomTitle(titleView)
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    updateExpenseInFirestore(position,etName,etAmount,etPriority);
                })
                .create();

        dialog.show();
    }
    private void populateExpenseData(int position,EditText etName,EditText etAmount,Spinner spinner,EditText etPriority) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ExpenseRef = db.collection("Expenses").document(ExpenseDataSet.get(position).getExpId());

        ExpenseRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ExpenseModel Expense = documentSnapshot.toObject(ExpenseModel.class);
                        if (Expense != null) {
                            etName.setText(Expense.getName());
                            etAmount.setText(Integer.toString(Expense.getAmount()));
                            spinner.setPrompt(ExpenseCategoryInput);
                            if(Expense.getExpPriority()!=0)
                                etPriority.setText(Integer.toString(Expense.getExpPriority()));
                            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                                    R.array.category_items, android.R.layout.simple_spinner_item);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                            String compareValue = Expense.getCategory();
                            if (compareValue != null) {
                                int spinnerPosition = adapter.getPosition(compareValue);
                                spinner.setSelection(spinnerPosition);
                            }
                        } else {
                            Toast.makeText(context, "Failed to retrieve Expense data!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.w("Firestore", "Error getting documents: ", exception);
                });
    }

    private void updateExpenseInFirestore(int position,EditText etName,EditText etAmount,EditText etPriority) {
        String newTitle = etName.getText().toString().trim();
        String newAmount = etAmount.getText().toString().trim();
        String newPriority = etPriority.getText().toString().trim();
        String newCategory = ExpenseCategoryInput;

        if(!newTitle.equals("")) {
            int PriorityExpense=0;
            if(!newPriority.equals(""))
                PriorityExpense=(int)Double.parseDouble(newPriority);
            int Amount=(int)Double.parseDouble(newAmount);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference ExpenseRef = db.collection("Expenses").document(ExpenseDataSet.get(position).getExpId());

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newTitle);
            updates.put("expPriority", PriorityExpense);
            updates.put("category", newCategory);
            updates.put("amount", Amount);
            ExpenseDataSet.get(position).setExpPriority(PriorityExpense);
            ExpenseRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Update successful
                        Toast.makeText(context, "Expense updated successfully!", Toast.LENGTH_SHORT).show();
                        ExpenseDataSet.get(position).setName(newTitle);
                        ExpenseDataSet.get(position).setCategory(newCategory);
                        ExpenseDataSet.get(position).setAmount(Amount);
                        notifyItemChanged(position);
                    })
                    .addOnFailureListener(exception -> {
                        // Update failed
                        Toast.makeText(context, "Failed to update Expense!", Toast.LENGTH_SHORT).show();
                    });
        }
        else {
            Toast.makeText(context, "Task name can't be empty", Toast.LENGTH_SHORT).show();
        }

    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return ExpenseDataSet.size();
    }
}
