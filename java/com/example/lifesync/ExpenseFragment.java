package com.example.lifesync;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

public class ExpenseFragment extends Fragment implements RefreshableFragment{
    MainActivity mainActivity;
    RecyclerView ExpenseRV;
    ExpenseListAdapter expenseListAdapter;
    String expenseCategory;
    int spent=0;
    int income=0;
    int saving=0;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Expense Query";
    TextView spt;
    TextView svg;
    TextView inc;
    Context context;
    PieChart pieChart;

    FloatingActionButton fabToggle,fabCalender;
    ArrayList<Integer> colors = new ArrayList<>();
    ArrayList<ExpenseModel> expenseList = new ArrayList<>();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    Calendar startDate = Calendar.getInstance();
    Calendar endDate = Calendar.getInstance();
    public ExpenseFragment() {
        // Required empty public constructor
    }
    public void onAttach(@NonNull Context context) {
        this.context=context;
        super.onAttach(context);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        mainActivity = (MainActivity)getActivity();

        startDate.set(Calendar.DAY_OF_MONTH, 1);//Set starting date to 1st of the month by default
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
        endDate.set(Calendar.MILLISECOND, 0);

        pieChart = view.findViewById(R.id.pieChart);
        colors.add(Color.parseColor("#FFA726")); // Orange
        colors.add(Color.parseColor("#66BB6A")); // Green
        colors.add(Color.parseColor("#29B6F6")); // Blue
        colors.add(Color.parseColor("#EF5350")); // Red
        colors.add(Color.parseColor("#AB47BC")); // Purple
        colors.add(Color.parseColor("#26A69A")); // Teal
        colors.add(Color.parseColor("#5C6BC0")); // Indigo
        colors.add(Color.parseColor("#EC407A")); // Pink
        colors.add(Color.parseColor("#8D6E63")); // Brown
        pieChart.invalidate(); // refresh

        fabToggle=view.findViewById(R.id.btnToggleChart);
        fabToggle.setOnClickListener(v->{
            if(pieChart.getVisibility()==View.GONE) {
                pieChart.setVisibility(View.VISIBLE);
                fabToggle.setImageResource(R.drawable.graphon);
            }
            else{
                pieChart.setVisibility(View.GONE);
                fabToggle.setImageResource(R.drawable.graphoff);
            }
        });

        fabCalender=view.findViewById(R.id.fabCalender);
        fabCalender.setOnClickListener(v->showStartDatePicker());

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
        b1.setOnClickListener(v -> addExpenseDialog());
        refreshContent();
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
                        .setTitle("Delete Expense")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            ExpenseModel expense = expenseList.get(position);
                            db.collection("Expenses").document(expense.getExpId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        expenseList.remove(position);
                                        expenseListAdapter.notifyItemRemoved(position);
                                        calculateSpent();
                                        Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            expenseListAdapter.notifyItemChanged(position); // reset swipe
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
                            6f,
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
                                itemView.getTop() + margin,
                                itemView.getRight() - margin,
                                itemView.getBottom() - margin
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
        }).attachToRecyclerView(ExpenseRV);

        return view;
    }


    private void addExpenseDialog(){
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_update_expense, null);

        EditText etName=dialogView.findViewById(R.id.ExpenseName);
        EditText etAmount=dialogView.findViewById(R.id.ExpenseAmount);
        EditText etPriority=dialogView.findViewById(R.id.ExpensePriority);
        Spinner spinner = dialogView.findViewById(R.id.ExpenseCategory);
        ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(context,
                R.array.category_items, android.R.layout.simple_spinner_item);

        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinneradapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item
                expenseCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do something when nothing is selected, if needed
            }
        });

        View titleView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task_title, null);
        TextView titleTextView = titleView.findViewById(R.id.dialog_title_text);
        titleTextView.setText("Add New Expense");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setCustomTitle(titleView)
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    SaveExpense(etName, etAmount, etPriority, spinner);
                })
                .create();

        dialog.show();
    }

    private void SaveExpense(EditText nameET,EditText amountET,EditText priorityET,Spinner spinner){
        String ExpenseNameInput = nameET.getText().toString().trim();
        String ExpensePriorityInput = priorityET.getText().toString().trim();
        String ExpenseAmountInput = amountET.getText().toString().trim();

        String CurrentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if(!ExpenseNameInput.equals("")) {
            if(!ExpenseAmountInput.equals("")){
                int Amount=(int)Double.parseDouble(ExpenseAmountInput);
                int PriorityExp=0;
                if(!ExpensePriorityInput.equals(""))
                    PriorityExp=(int)Double.parseDouble(ExpensePriorityInput);
                if(ExpensePriorityInput.equals("") || (ExpensePriorityInput.length() > 0 && PriorityExp>=1 && PriorityExp<=3)){
                    ExpenseModel expenseModel=new ExpenseModel("",ExpenseNameInput,CurrentDate,FirebaseAuth.getInstance().getUid(),expenseCategory,Amount, PriorityExp);
                    db.collection("Expenses").add(expenseModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
                                    refreshContent();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });
                }
                else{
                    Toast.makeText(requireContext(), "Priority should be between 1 and 5", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(requireContext(), "Expense Amount can't be empty", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(requireContext(), "Expense name can't be empty", Toast.LENGTH_SHORT).show();
        }
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
        spent = 0;
        saving = 0;
        expenseList.clear();
        expenseListAdapter.notifyDataSetChanged();
        Map<String, Integer> categoryTotalMap = new HashMap<>();
        db.collection("Expenses")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ExpenseModel expenseModel = document.toObject(ExpenseModel.class);
                            expenseModel.setExpId(document.getId());
                            try {
                                Date date = sdf.parse(expenseModel.getDate());

                                if(!date.before(startDate.getTime()) && !date.after(endDate.getTime())) {
                                    spent += expenseModel.getAmount();
                                    String category = expenseModel.getCategory();
                                    int amount = expenseModel.getAmount();
                                    if (categoryTotalMap.containsKey(category)) {
                                        categoryTotalMap.put(category, categoryTotalMap.get(category) + amount);
                                    } else {
                                        categoryTotalMap.put(category, amount);
                                    }
                                }
                            }catch (ParseException e) {
                                e.printStackTrace();
                            }
                            expenseList.add(expenseModel);
                        }
                        expenseListAdapter.notifyDataSetChanged();
                        updateStatsUI(); // update after fetching expenses
                        updatePieChart(categoryTotalMap);
                    } else {
                        Log.d(TAG, "Error getting expenses: ", task.getException());
                    }
                });

        // Fetch income (separate query)
        db.collection("Incomes")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            IncomeModel incomeModel = document.toObject(IncomeModel.class);
                            incomeModel.setDocId(document.getId());
                            income = incomeModel.getIncome();
                        }
                        updateStatsUI(); // update again after fetching income
                    } else {
                        Log.d(TAG, "Error getting income: ", task.getException());
                    }
                });
    }

    // Helper method to update the TextViews
    private void updateStatsUI() {
        saving = income - spent;
        if (spt != null) spt.setText("SPENT:\nRs." + spent);
        if (svg != null) svg.setText("SAVING:\nRs." + saving);
        if (inc != null) inc.setText("INCOME:\nRs." + income);
    }
    private void updatePieChart(Map<String, Integer> categoryMap) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);


        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(14f);

        pieChart.invalidate(); // refresh
    }

    private void showStartDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog startDatePicker = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            startDate.set(year, month, dayOfMonth);
            showEndDatePicker(); // immediately show end date picker
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        startDatePicker.setTitle("Select Start Date");
        startDatePicker.show();
    }

    private void showEndDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog endDatePicker = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            endDate.set(year, month, dayOfMonth);
            calculateSpent();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        endDatePicker.setTitle("Select End Date");
        endDatePicker.show();
    }

    private void calculateSpent(){
        int newSpent = 0;
        Map<String, Integer> categoryTotalMap = new HashMap<>();
        for (ExpenseModel expense : expenseList) {
            try {
                Date expenseDate = sdf.parse(expense.getDate());

                if (expenseDate != null && !expenseDate.before(startDate.getTime()) && !expenseDate.after(endDate.getTime())) {
                    newSpent += expense.getAmount();
                    String category = expense.getCategory();
                    int amount = expense.getAmount();
                    if (categoryTotalMap.containsKey(category)) {
                        categoryTotalMap.put(category, categoryTotalMap.get(category) + amount);
                    } else {
                        categoryTotalMap.put(category, amount);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        spent = newSpent;
        saving = income - spent;
        updateStatsUI();
        updatePieChart(categoryTotalMap);
    }

}