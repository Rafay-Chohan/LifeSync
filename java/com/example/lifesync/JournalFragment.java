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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class JournalFragment extends Fragment {
    MainActivity mainActivity;
    RecyclerView logRV;
    LogListAdapter logListAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Journal Query";
    ArrayList<com.example.lifesync.LogModel> logList = new ArrayList<>();
    public JournalFragment() {
        // Required empty public constructor
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
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), addLog.class);
                startActivity(intent);
            }
        });
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
        return view;
    }
}