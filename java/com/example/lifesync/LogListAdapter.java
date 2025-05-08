package com.example.lifesync;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.ViewHolder> {

    private ArrayList<com.example.lifesync.LogModel> logDataSet;
    Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView logName,logData,logDate;

        LinearLayout containerLL;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            logName = (TextView) view.findViewById(R.id.logName);
            logData = (TextView) view.findViewById(R.id.logData);
            logDate = (TextView) view.findViewById(R.id.logDate);
            containerLL=(LinearLayout) view.findViewById(R.id.LogContainer);
        }
    }
    public LogListAdapter(ArrayList<com.example.lifesync.LogModel> logDataSet) {
        this.logDataSet = logDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_log, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        context = viewHolder.itemView.getContext();
        viewHolder.logName.setText(logDataSet.get(position).getName());
        viewHolder.logData.setText(logDataSet.get(position).getData());
        viewHolder.logDate.setText(logDataSet.get(position).getDate());
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
                            editLogDialog(position);
                        }else if(item.getItemId()==R.id.Deletebtn)
                        {
                            FirebaseFirestore.getInstance().collection("Logs").document(logDataSet.get(position).getLogID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(view.getContext(), "Log Removed", Toast.LENGTH_SHORT).show();
                                    logDataSet.remove(position);
                                    notifyItemRemoved(position);
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

    private void editLogDialog(int position){
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_update_log, null);

        EditText etName=dialogView.findViewById(R.id.logName);
        EditText etData=dialogView.findViewById(R.id.logData);
        populateLogData(position,etName,etData);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Add New Log")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    updateLogInFirestore(etName,etData,position);
                })
                .create();


        // 6. Show the dialog
        dialog.show();
    }
    private void populateLogData(int position,EditText etName,EditText etData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference LogRef = db.collection("Logs").document(logDataSet.get(position).getLogID());

        LogRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        com.example.lifesync.LogModel Log = documentSnapshot.toObject(com.example.lifesync.LogModel.class);
                        if (Log != null) {
                            etName.setText(Log.getName());
                            etData.setText(Log.getData());
                        } else {
                            Toast.makeText(context, "Failed to retrieve Log data!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.w("Firestore", "Error getting documents: ", exception);
                });
    }

    private void updateLogInFirestore(EditText etName,EditText etData,int position) {
        String newTitle = etName.getText().toString().trim();
        String newData = etData.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference LogRef = db.collection("Logs").document(logDataSet.get(position).getLogID());

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newTitle);
        updates.put("data", newData);
        LogRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    Toast.makeText(context, "Log updated successfully!", Toast.LENGTH_SHORT).show();
                    logDataSet.get(position).setName(newTitle);
                    logDataSet.get(position).setData(newData);
                    notifyItemChanged(position);
                })
                .addOnFailureListener(exception -> {
                    // Update failed
                    Toast.makeText(context, "Failed to update Log!", Toast.LENGTH_SHORT).show();
                });

    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return logDataSet.size();
    }
}
