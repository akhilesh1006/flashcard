package com.example.flashcard.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.data.ExamPlan;
import com.example.flashcard.data.SubjectWithCount;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateEditPlanActivity extends AppCompatActivity {

    @Override
    protected void onStop() {
        super.onStop();
        com.example.flashcard.widget.FlashcardWidgetProvider.requestUpdate(this);
    }

    private FlashcardViewModel flashcardViewModel;
    
    private TextInputEditText editPlanName;
    private TextView textExamDate;
    private SubjectSelectionAdapter subjectAdapter;

    private String selectedExamDate = null; // yyyy-MM-dd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_plan);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_create_edit_plan);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editPlanName = findViewById(R.id.edit_plan_name);
        textExamDate = findViewById(R.id.text_exam_date);
        MaterialButton btnPickDate = findViewById(R.id.btn_pick_date);
        MaterialButton btnSavePlan = findViewById(R.id.btn_save_plan);

        RecyclerView recyclerSubjects = findViewById(R.id.recycler_subjects_selection);
        recyclerSubjects.setLayoutManager(new LinearLayoutManager(this));
        subjectAdapter = new SubjectSelectionAdapter();
        recyclerSubjects.setAdapter(subjectAdapter);

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);
        
        flashcardViewModel.getAllSubjectsWithCount().observe(this, new Observer<List<SubjectWithCount>>() {
            @Override
            public void onChanged(@Nullable List<SubjectWithCount> subjectWithCounts) {
                subjectAdapter.setSubjects(subjectWithCounts);
            }
        });

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
                builder.setTitleText("Select Exam Date");
                MaterialDatePicker<Long> picker = builder.build();
                picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(selection);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        selectedExamDate = format.format(calendar.getTime());

                        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        textExamDate.setText(displayFormat.format(calendar.getTime()));
                    }
                });
                picker.show(getSupportFragmentManager(), "EXAM_DATE_PICKER");
            }
        });

        btnSavePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlan();
            }
        });
    }

    private void savePlan() {
        String name = editPlanName.getText().toString().trim();
        List<Integer> selectedSubjects = subjectAdapter.getSelectedSubjectIds();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter a plan name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedExamDate == null) {
            Toast.makeText(this, "Please select an exam date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSubjects.isEmpty()) {
            Toast.makeText(this, "Please select at least one subject", Toast.LENGTH_SHORT).show();
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        ExamPlan plan = new ExamPlan(name, today, selectedExamDate, System.currentTimeMillis());

        flashcardViewModel.createExamPlan(plan, selectedSubjects, new com.example.flashcard.data.FlashcardRepository.InsertPlanCallback() {
            @Override
            public void onInserted(long planId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CreateEditPlanActivity.this, "Plan created!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
