package com.example.flashcard.ui;
import com.example.flashcard.FlashcardActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.R;
import com.example.flashcard.data.ExamPlan;
import com.example.flashcard.utils.ExamPlanUtils;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlanDetailsActivity extends AppCompatActivity {

    @Override
    protected void onStop() {
        super.onStop();
        com.example.flashcard.widget.FlashcardWidgetProvider.requestUpdate(this);
    }

    public static final String EXTRA_PLAN_ID = "plan_id";
    private int planId = -1;
    private FlashcardViewModel flashcardViewModel;

    private TextView textName, textDates, textCovered, textTotal, textDailyTarget, textStatusLabel;
    private ProgressBar progressBar;
    private MaterialButton btnStudyNow, btnMarkCompleted;

    private int currentTotal = 0;
    private int currentCovered = 0;
    private ExamPlan currentPlan = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_details);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_plan_details);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        planId = getIntent().getIntExtra(EXTRA_PLAN_ID, -1);
        if (planId == -1) {
            finish();
            return;
        }

        textName = findViewById(R.id.text_plan_details_name);
        textDates = findViewById(R.id.text_plan_details_dates);
        textCovered = findViewById(R.id.text_cards_covered);
        textTotal = findViewById(R.id.text_cards_total);
        textDailyTarget = findViewById(R.id.text_daily_target);
        textStatusLabel = findViewById(R.id.text_plan_status_label);
        progressBar = findViewById(R.id.progress_plan);
        btnStudyNow = findViewById(R.id.btn_study_plan_now);
        btnMarkCompleted = findViewById(R.id.btn_mark_completed);

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        flashcardViewModel.getExamPlan(planId).observe(this, plan -> {
            if (plan != null) {
                currentPlan = plan;
                textName.setText(plan.name);
                textDates.setText("Start: " + plan.startDate + " | Exam: " + plan.examDate);
                updateCalculateMetrics();
            }
        });

        flashcardViewModel.getCardCountForPlan(planId).observe(this, count -> {
            currentTotal = count != null ? count : 0;
            textTotal.setText(String.valueOf(currentTotal));
            updateCalculateMetrics();
        });

        flashcardViewModel.getCoveredCardCountForPlan(planId).observe(this, count -> {
            currentCovered = count != null ? count : 0;
            textCovered.setText(String.valueOf(currentCovered));
            updateCalculateMetrics();
        });

        btnStudyNow.setOnClickListener(v -> {
            if (currentTotal > 0 && currentCovered < currentTotal) {
                // We will navigate to FlashcardActivity giving it the EXTRA_PLAN_ID.
                // We'll also tell FlashcardViewModel to use the plan. (Task #23)
                // For now just pass the extra.
                Intent intent = new Intent(PlanDetailsActivity.this, FlashcardActivity.class);
                intent.putExtra(EXTRA_PLAN_ID, planId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Plan is completely covered!", Toast.LENGTH_SHORT).show();
            }
        });

        btnMarkCompleted.setOnClickListener(v -> {
            boolean isCompleted = (currentPlan != null && currentPlan.completedAt > 0);
            flashcardViewModel.markExamPlanCompleted(planId, !isCompleted, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(PlanDetailsActivity.this, "Plan completed status updated.", Toast.LENGTH_SHORT).show();
                });
            });
        });
    }

    private void updateCalculateMetrics() {
        if (currentPlan == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        int target = ExamPlanUtils.calculateTarget(currentTotal, currentCovered, today, currentPlan.examDate);
        textDailyTarget.setText(String.valueOf(target));

        ExamPlanUtils.PlanStatus status = ExamPlanUtils.calculateStatus(
                currentTotal, currentCovered, currentPlan.startDate, today, currentPlan.examDate, 2
        );

        textStatusLabel.setText("Status: " + status.name().replace("_", " "));

        if (currentTotal > 0) {
            int prog = (int) (((float) currentCovered / currentTotal) * 100);
            progressBar.setProgress(prog);
        } else {
            progressBar.setProgress(0);
        }

        if (currentPlan.completedAt > 0) {
            btnMarkCompleted.setText("Unmark Completed");
            textStatusLabel.setText("Status: COMPLETED");
            progressBar.setProgress(100);
        } else {
            btnMarkCompleted.setText("Mark as Completed");
        }
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
