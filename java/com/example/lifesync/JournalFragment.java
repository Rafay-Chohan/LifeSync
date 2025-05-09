package com.example.lifesync;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class JournalFragment extends Fragment implements RefreshableFragment{
    MainActivity mainActivity;
    RecyclerView logRV;
    LogListAdapter logListAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Journal Query";
    ArrayList<com.example.lifesync.LogModel> logList = new ArrayList<>();
    Context context;
    public JournalFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(@NonNull Context context) {
        this.context=context;
        super.onAttach(context);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);
        mainActivity = (MainActivity)getActivity();

        logRV = view.findViewById(R.id.logListRV);
        logListAdapter = new LogListAdapter(logList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        logRV.setLayoutManager(layoutManager);
        logRV.setAdapter(logListAdapter);

        FloatingActionButton b1 = view.findViewById(R.id.btnAddJournal);
        mainActivity = (MainActivity)getActivity();
        b1.setOnClickListener(v -> addLogDialog());
        refreshContent();
        return view;
    }
    private void addLogDialog(){
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_update_log, null);

        EditText etName=dialogView.findViewById(R.id.logName);
        EditText etData=dialogView.findViewById(R.id.logData);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add New Log")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    SaveLog(etName,etData);
                })
                .create();

        dialog.show();
    }
    private void SaveLog(EditText etName,EditText etData){

        String logNameInput = etName.getText().toString().trim();
        String logDataInput = etData.getText().toString().trim();
        String CurrentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if(!logNameInput.equals("")) {
            com.example.lifesync.LogModel logModel = new com.example.lifesync.LogModel("", logNameInput, logDataInput,CurrentDate, FirebaseAuth.getInstance().getUid());
            db.collection("Logs").add(logModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            Toast.makeText(requireContext(), "Log added successfully", Toast.LENGTH_SHORT).show();
                            refreshContent();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(requireContext(), "Error Adding in DB", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            Toast.makeText(requireContext(), "Task name can't be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void refreshContent()
    {
        logList.clear();
        logListAdapter.notifyDataSetChanged();
        db.collection("Logs")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                com.example.lifesync.LogModel logModel=document.toObject(com.example.lifesync.LogModel.class);
                                logModel.setLogID(document.getId());
                                logList.add(logModel);
                            }
                            logListAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}