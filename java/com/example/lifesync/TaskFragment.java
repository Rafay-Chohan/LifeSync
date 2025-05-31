package com.example.lifesync;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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

public class TaskFragment extends Fragment implements RefreshableFragment {

    MainActivity mainActivity;
    RecyclerView taskRV;
    TextView tvPendingTasks;
    TaskListAdapter taskListAdapter;
    Context context;
    int pendingTaskCount = 0;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Task Manager Query";
    ArrayList<com.example.lifesync.TaskModel> taskList = new ArrayList<>();
    public TaskFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(@NonNull Context context) {
        this.context=context;
        super.onAttach(context);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        mainActivity = (MainActivity)getActivity();

        taskRV = view.findViewById(R.id.taskListRV);
        tvPendingTasks = view.findViewById(R.id.tvPendingTasks);
        taskListAdapter = new TaskListAdapter(taskList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        taskRV.setLayoutManager(layoutManager);
        taskRV.setAdapter(taskListAdapter);

        FloatingActionButton btnAddTask = view.findViewById(R.id.btnAddTask);
        mainActivity = (MainActivity)getActivity();
        btnAddTask.setOnClickListener(v -> addTaskDialog());
        refreshContent();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            // Set the swipe threshold (percentage of item width)
            private static final float SWIPE_THRESHOLD = 0.25f; // 25% of item width
            private static final float MAX_SWIPE_DISTANCE = 0.3f;
            private static final float CORNER_RADIUS = 16f;
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return SWIPE_THRESHOLD;
            }
            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                // Make it harder to trigger swipe by escape velocity
                return defaultValue * 10;
            }
            @Override
            public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
                // Customize the animation duration when returning to position
                return animationType == ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL ? 200 : super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
            }
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    // Show delete confirmation dialog
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Task")
                            .setMessage("Are you sure you want to delete this task?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                String taskId = taskList.get(position).getTaskId();
                                db.collection("Tasks").document(taskId)
                                        .delete()
                                        .addOnSuccessListener(unused -> {
                                            taskList.remove(position);
                                            taskListAdapter.notifyItemRemoved(position);
                                            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                taskListAdapter.notifyItemChanged(position); // reset item
                            })
                            .setCancelable(false)
                            .show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Toggle task status
                    com.example.lifesync.TaskModel task = taskList.get(position);
                    String oldStatus = task.getTaskStatus();
                    String newStatus = oldStatus.equalsIgnoreCase("Completed") ? "Pending" : "Completed";
                    task.setTaskStatus(newStatus);

                    db.collection("Tasks").document(task.getTaskId())
                            .set(task)
                            .addOnSuccessListener(unused -> {
                                taskListAdapter.notifyItemChanged(position);
                                Toast.makeText(context, "Task marked as " + newStatus, Toast.LENGTH_SHORT).show();

                                if (oldStatus.equalsIgnoreCase("Completed") && newStatus.equalsIgnoreCase("Pending")) {
                                    pendingTaskCount++;
                                } else if ((oldStatus.equalsIgnoreCase("Pending") || oldStatus.equalsIgnoreCase("Missed")) && newStatus.equalsIgnoreCase("Completed")) {
                                    if(pendingTaskCount!=0)
                                        pendingTaskCount--;
                                }

                                tvPendingTasks.setText("Pending Tasks: " + pendingTaskCount);
                            });
                }

                // Reset the item position after swipe (important!)
                taskListAdapter.notifyItemChanged(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float width = itemView.getWidth();
                    float height = itemView.getHeight();

                    // Convert dp to pixels for consistent sizing
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

                    // Limit maximum swipe distance
                    float threshold = width * MAX_SWIPE_DISTANCE;
                    if (Math.abs(dX) > threshold) {
                        dX = dX > 0 ? threshold : -threshold;
                    }

                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    Path path = new Path();

                    if (dX > 0) { // Swiping Right (Green)
                        // Draw background with margin
                        paint.setColor(Color.parseColor("#4CAF50"));
                        RectF rect = new RectF(
                                itemView.getLeft() + margin,
                                itemView.getTop() ,
                                itemView.getLeft() + dX - margin,
                                itemView.getBottom()
                        );
                        path.addRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, Path.Direction.CW);
                        c.drawPath(path, paint);

                        Drawable icon = ContextCompat.getDrawable(context, R.drawable.status_icon);
                        if (icon != null) {
                            int iconLeft = (int) (itemView.getLeft() + margin + (dX - margin - iconSize)/2);
                            int iconTop = (int) (itemView.getTop() + (height - iconSize)/2);
                            icon.setBounds(
                                    iconLeft,
                                    iconTop,
                                    iconLeft + iconSize,
                                    iconTop + iconSize
                            );
                            icon.draw(c);
                        }

                    } else if (dX < 0) { // Swiping Left (Red)
                        // Draw background with margin
                        paint.setColor(Color.parseColor("#F44336"));
                        RectF rect = new RectF(
                                itemView.getRight() + dX + margin,
                                itemView.getTop() ,
                                itemView.getRight() - margin,
                                itemView.getBottom()
                        );
                        path.addRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, Path.Direction.CW);
                        c.drawPath(path, paint);

                        // Draw static-sized icon centered vertically
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

                    // Draw the item view on top
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        }).attachToRecyclerView(taskRV);

        return view;
    }
    @Override
    public void refreshContent() {
        // Clear existing data
        taskList.clear();

        AppWidgetManager manager = AppWidgetManager.getInstance(requireContext());
        ComponentName widget = new ComponentName(requireContext(), TaskWidget.class);
        manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(widget), R.id.lvWidgetTasks);

        // Fetch fresh data from Firestore
        db.collection("Tasks")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .orderBy("taskStatus", Query.Direction.DESCENDING)
                .orderBy("taskDeadline", Query.Direction.ASCENDING)
                .orderBy("taskPriority", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            pendingTaskCount = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                com.example.lifesync.TaskModel taskModel = document.toObject(com.example.lifesync.TaskModel.class);
                                taskModel.setTaskId(document.getId());
                                taskList.add(taskModel);

                                if (taskModel.getTaskStatus().equals("Pending")) {
                                    pendingTaskCount++;
                                }
                            }
                            taskListAdapter.notifyDataSetChanged();

                            tvPendingTasks.setText("Pending Tasks: " + pendingTaskCount);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        // Set the number of pending tasks

    }
    private void addTaskDialog(){
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_update_task, null);

        EditText etName=dialogView.findViewById(R.id.taskName);
        EditText etDeadline=dialogView.findViewById(R.id.taskDeadline);
        EditText etPriority=dialogView.findViewById(R.id.taskPriority);
        EditText etDuration=dialogView.findViewById(R.id.taskDuration);

        etDeadline.setOnClickListener(v -> showDateTimePicker(etDeadline));

        View titleView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task_title, null);
        TextView titleTextView = titleView.findViewById(R.id.dialog_title_text);
        titleTextView.setText("Add New Task");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setCustomTitle(titleView)
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    SaveTask(etName,etDeadline,etPriority,etDuration);
                })
                .create();

        dialog.show();
    }
    private void SaveTask(EditText nameET,EditText deadlineET,EditText priorityET,EditText durationET){
        String taskNameInput = nameET.getText().toString().trim();
        String taskDeadlineInput=deadlineET.getText().toString().trim();
        String taskPriorityInput = priorityET.getText().toString().trim();
        String taskDurationInput = durationET.getText().toString().trim();

        if(!taskNameInput.equals("")) {
            int Prioritytask=0;
            if(!taskPriorityInput.equals(""))
                Prioritytask=(int)Double.parseDouble(taskPriorityInput);
            if(taskPriorityInput.equals("") || (taskPriorityInput.length() > 0 && Prioritytask>=1 && Prioritytask<=5)) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                dateFormatter.setLenient(false);
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                timeFormatter.setLenient(false);
                com.example.lifesync.TaskModel taskModel = new com.example.lifesync.TaskModel("", taskNameInput, "Pending",Prioritytask,taskDeadlineInput,taskDurationInput, FirebaseAuth.getInstance().getUid());
                db.collection("Tasks").add(taskModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
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
                return;
            }
            else if(Integer.parseInt(taskPriorityInput)<1 || Integer.parseInt(taskPriorityInput)>5) {
                Toast.makeText(requireContext(), "Priority should be between 1 and 5", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(requireContext(), "Task name can't be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDateTimePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();

        // Date Picker
        DatePickerDialog datePicker = new DatePickerDialog(
                context,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);

                    // Time Picker (shows after date is selected)
                    TimePickerDialog timePicker = new TimePickerDialog(
                            context,
                            (view1, hour, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);

                                // Format: "Jun 15, 2:30 PM"
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                targetEditText.setText(sdf.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                    );
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePicker.show();
    }
}