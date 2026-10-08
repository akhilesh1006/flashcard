package com.example.flashcard.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.R;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestResultDetailsActivity extends AppCompatActivity {

    private FlashcardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result_details);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "test_result_details",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_result_details_title, R.string.guide_result_details_message));

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Result Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        long testResultId = getIntent().getLongExtra("TEST_RESULT_ID", -1);
        if (testResultId == -1) {
            finish();
            return;
        }

        TextView tvDetailsSummary = findViewById(R.id.tvDetailsSummary);
        LinearLayout detailsListContainer = findViewById(R.id.detailsListContainer);
        MaterialButton btnReviewDetailsMistakes = findViewById(R.id.btnReviewDetailsMistakes); // We will add this to the layout

        // Fetch result details + items
        // We will fetch permanently saved results and find ours
        viewModel.getPermanentlySavedResults().observe(this, results -> {
            com.example.flashcard.data.TestResult currentResult = null;
            for (com.example.flashcard.data.TestResult r : results) {
                if (r.id == testResultId) {
                    currentResult = r;
                    break;
                }
            }

            if (currentResult != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());
                String dateStr = sdf.format(new Date(currentResult.completedAt));

                StringBuilder summary = new StringBuilder();
                summary.append("Date: ").append(dateStr).append("\n");
                summary.append("Subject: ").append(currentResult.subjectName != null ? currentResult.subjectName : "All Subjects").append("\n");
                summary.append("Difficulty: ").append(currentResult.difficulty != null ? currentResult.difficulty : "All").append("\n\n");
                summary.append("Score: ").append(currentResult.score).append("%\n");
                summary.append("Total Cards: ").append(currentResult.totalCards).append("\n");
                summary.append("Correct: ").append(currentResult.correctAnswers).append("\n");
                summary.append("Incorrect: ").append(currentResult.incorrectAnswers).append("\n");
                if (currentResult.unanswered > 0) {
                    summary.append("Unanswered: ").append(currentResult.unanswered).append("\n");
                }

                if (currentResult.timeUsedMillis > 0) {
                    int m = (int) (currentResult.timeUsedMillis / 1000) / 60;
                    int s = (int) (currentResult.timeUsedMillis / 1000) % 60;
                    summary.append(String.format(Locale.getDefault(), "Time used: %02d:%02d\n", m, s));
                }

                tvDetailsSummary.setText(summary.toString());
                
                // Final fields for inner class
                final String sName = currentResult.subjectName;
                final String sDiff = currentResult.difficulty;
                final int sId = currentResult.subjectId;

                viewModel.getResultItemsForTest(testResultId).observe(this, items -> {
                    detailsListContainer.removeAllViews();
                    List<Integer> mistakesList = new ArrayList<>();
                    if (items != null) {
                        for (com.example.flashcard.data.TestResultItem item : items) {
                            TextView tv = new TextView(this);
                            tv.setTextSize(16f);
                            tv.setPadding(0, 16, 0, 16);

                            String resStr = "━";
                            int col = Color.GRAY;
                            if (item.result == 1) {
                                resStr = "✓";
                                col = getResources().getColor(R.color.nav_import_word, getTheme());
                            } else if (item.result == 0) {
                                resStr = "✗";
                                col = getResources().getColor(R.color.nav_clear_data, getTheme());
                                mistakesList.add(item.flashcardId);
                            } else {
                                // Unanswered
                                mistakesList.add(item.flashcardId);
                            }

                            tv.setText(resStr + " " + item.question);
                            tv.setTextColor(col);

                            detailsListContainer.addView(tv);
                        }
                        
                        if (!mistakesList.isEmpty() && btnReviewDetailsMistakes != null) {
                            int[] arr = new int[mistakesList.size()];
                            for(int i=0; i<mistakesList.size(); i++) arr[i] = mistakesList.get(i);
                            
                            btnReviewDetailsMistakes.setVisibility(View.VISIBLE);
                            btnReviewDetailsMistakes.setOnClickListener(v -> {
                                Intent intent = new Intent(TestResultDetailsActivity.this, TestActivity.class);
                                intent.putExtra("CARD_IDS", arr);
                                intent.putExtra("TIME_MILLIS", -1L); // Unlimited time
                                intent.putExtra("SUBJECT_ID", sId);
                                intent.putExtra("SUBJECT_NAME", sName);
                                intent.putExtra("DIFFICULTY", sDiff);
                                startActivity(intent);
                            });
                        }
                    }
                });
            }
        });
    }
}
