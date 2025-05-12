package com.example.lifesync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lifesync.model.ExpenseModel;
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

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private static final float SWIPE_THRESHOLD = 0.25f;
            private static final float MAX_SWIPE_DISTANCE = 0.3f;
            private static final float CORNER_RADIUS = 16f;

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return SWIPE_THRESHOLD;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return defaultValue * 10;
            }

            @Override
            public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
                return animationType == ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL ? 200 : super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // Show delete confirmation dialog
                new AlertDialog.Builder(context)
                        .setTitle("Delete Log")
                        .setMessage("Are you sure you want to delete this Log?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            String logId = logList.get(position).getLogID();
                            db.collection("Logs").document(logId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        logList.remove(position);
                                        logListAdapter.notifyItemRemoved(position);
                                        Toast.makeText(context, "Log deleted", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            logListAdapter.notifyItemChanged(position); // reset swipe
                        })
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float width = itemView.getWidth();
                    float height = itemView.getHeight();

                    // Convert dp to pixels
                    float margin = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8f,
                            getResources().getDisplayMetrics()
                    );
                    int iconSize = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            24f,
                            getResources().getDisplayMetrics()
                    );

                    // Limit swipe distance
                    float threshold = width * MAX_SWIPE_DISTANCE;
                    if (Math.abs(dX) > threshold) {
                        dX = dX > 0 ? threshold : -threshold;
                    }

                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    Path path = new Path();

                    // Only left swipe for delete
                    if (dX < 0) {
                        // Draw background with margin and rounded corners
                        paint.setColor(Color.parseColor("#F44336"));
                        RectF rect = new RectF(
                                itemView.getRight() + dX + margin,
                                itemView.getTop() ,
                                itemView.getRight() - margin,
                                itemView.getBottom()
                        );
                        path.addRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, Path.Direction.CW);
                        c.drawPath(path, paint);

                        // Draw static-sized delete icon
                        Drawable icon = ContextCompat.getDrawable(context, R.drawable.delete_icon);
                        if (icon != null) {
                            int iconRight = (int) (itemView.getRight() - margin - (-dX - margin - iconSize)/2);
                            int iconTop = (int) (itemView.getTop() + (height - iconSize)/2);
                            icon.setBounds(
                                    iconRight - iconSize,
                                    iconTop,
                                    iconRight,
                                    iconTop + iconSize
                            );
                            icon.draw(c);
                        }
                    }

                    // Draw item on top
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        }).attachToRecyclerView(logRV);

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