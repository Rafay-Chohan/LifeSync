package com.example.lifesync;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.model.ExpenseModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ViewHolder> {

    private ArrayList<ExpenseModel> ExpenseDataSet;

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
        viewHolder.ExpenseName.setText(ExpenseDataSet.get(position).getName());
        viewHolder.ExpenseAmount.setText(Integer.toString(ExpenseDataSet.get(position).getAmount()));
        viewHolder.ExpenseDate.setText(ExpenseDataSet.get(position).getDate());
        viewHolder.ExpenseCategory.setText(ExpenseDataSet.get(position).getCategory());
        viewHolder.containerLL.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                PopupMenu popupMenu=new PopupMenu(view.getContext(),viewHolder.containerLL);
                popupMenu.inflate(R.menu.log_menu);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.Editbtn){
                            Intent intent = new Intent(view.getContext(), EditExpense.class);
                            intent.putExtra("expId", ExpenseDataSet.get(position).getExpId()); // Pass task ID to EditTaskActivity
                            view.getContext().startActivity(intent);
                        }else if(item.getItemId()==R.id.Deletebtn)
                        {
                            FirebaseFirestore.getInstance().collection("Expenses").document(ExpenseDataSet.get(position).getExpId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(view.getContext(), "Expense Removed", Toast.LENGTH_SHORT).show();
                                    viewHolder.containerLL.setVisibility(view.GONE);
                                }
                            });
                        }
                        return false;
                    }
                });
                return false;
            }
        });
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return ExpenseDataSet.size();
    }
}
