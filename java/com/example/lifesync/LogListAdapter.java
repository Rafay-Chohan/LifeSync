package com.example.lifesync;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.ViewHolder> {

    private ArrayList<com.example.lifesync.LogModel> logDataSet;

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
    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
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
                        if(item.getItemId()==R.id.Deletebtn)
                        {
                            FirebaseFirestore.getInstance().collection("Logs").document(logDataSet.get(position).getLogID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(view.getContext(), "Log Removed", Toast.LENGTH_SHORT).show();
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
        return logDataSet.size();
    }
}
