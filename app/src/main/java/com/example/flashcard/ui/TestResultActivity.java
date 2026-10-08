package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.R;
import com.example.flashcard.data.TestResult;
import com.example.flashcard.data.TestResultItem;
import com.example.flashcard.viewmodel.FlashcardViewModel;

import java.util.ArrayList;
import java.util.List;

import com.example.flashcard.utils.DailyGoalPreferences;

public class TestResultActivity extends AppCompatActivity {

    private FlashcardViewModel viewModel;

    private long currentTestId = -1;
    private boolean isInserting = false;
    private boolean isPermanentlySaved = false;
    private int[] incorrectFlashcardIds = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "test_results",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_result_title, R.string.guide_result_message));

        viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        TextView tvSummaryTotal = findViewById(R.id.tvSummaryTotal);
        TextView tvSummaryTime = findViewById(R.id.tvSummaryTime);
        TextView tvSummaryScore = findViewById(R.id.tvSummaryScore);
        LinearLayout resultsListContainer = findViewById(R.id.resultsListContainer);
        com.google.android.material.button.MaterialButton btnSaveHistory = findViewById(R.id.btnSaveHistory);
        com.google.android.material.button.MaterialButton btnReviewMistakes = findViewById(R.id.btnReviewMistakes);

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        int correctCount = getIntent().getIntExtra("CORRECT", 0);
        int incorrectCount = getIntent().getIntExtra("INCORRECT", 0);
        int total = getIntent().getIntExtra("TOTAL", 0);
        long timeUsed = getIntent().getLongExtra("TIME_USED", -1);
        long timeTotal = getIntent().getLongExtra("TIME_TOTAL", -1);
        int[] resultsArray = getIntent().getIntArrayExtra("RESULTS_ARRAY");
        String[] questionArray = getIntent().getStringArrayExtra("QUESTIONS_ARRAY");
        int[] flashcardIdsArray = getIntent().getIntArrayExtra("FLASHCARD_IDS_ARRAY");

        int subjectId = getIntent().getIntExtra("SUBJECT_ID", -1);
        String subjectName = getIntent().getStringExtra("SUBJECT_NAME");
        String difficulty = getIntent().getStringExtra("DIFFICULTY");
        boolean wasTimeUp = getIntent().getBooleanExtra("WAS_TIME_UP", false);
        boolean isNewTest = getIntent().getBooleanExtra("IS_NEW_TEST", false);

        int unanswered = total - correctCount - incorrectCount;
        int score = total > 0 ? (int) (((float) correctCount / total) * 100) : 0;

        tvSummaryTotal.setText("Cards: " + total + " | Correct: " + correctCount + " | Incorrect: " + incorrectCount + (unanswered > 0 ? " | Unanswered: " + unanswered : ""));

        if (timeUsed == -1) {
            tvSummaryTime.setText("Time: Unlimited");
        } else {
            tvSummaryTime.setText("Time used: " + formatTime(timeUsed) + (timeTotal != -1 ? " / " + formatTime(timeTotal) : ""));
        }

        tvSummaryScore.setText("Score: " + score + "%");

        if (resultsArray != null && questionArray != null && flashcardIdsArray != null) {
            List<Integer> mistakesList = new ArrayList<>();
            for (int i = 0; i < resultsArray.length; i++) {
                TextView tv = new TextView(this);
                tv.setTextSize(16f);
                tv.setPadding(0, 16, 0, 16);

                String resStr = "━";
                int col = Color.GRAY;
                if (resultsArray[i] == 1) {
                    resStr = "✓";
                    col = getResources().getColor(R.color.nav_import_word, getTheme());
                } else if (resultsArray[i] == 0) {
                    resStr = "✗";
                    col = getResources().getColor(R.color.nav_clear_data, getTheme());
                    mistakesList.add(flashcardIdsArray[i]);
                } else {
                    // Unanswered count as mistakes for review
                    mistakesList.add(flashcardIdsArray[i]);
                }

                tv.setText(resStr + " " + questionArray[i]);
                tv.setTextColor(col);

                resultsListContainer.addView(tv);
            }
            
            if (!mistakesList.isEmpty()) {
                incorrectFlashcardIds = new int[mistakesList.size()];
                for (int m = 0; m < mistakesList.size(); m++) {
                    incorrectFlashcardIds[m] = mistakesList.get(m);
                }
                btnReviewMistakes.setVisibility(View.VISIBLE);
                btnReviewMistakes.setOnClickListener(v -> {
                    Intent intent = new Intent(TestResultActivity.this, TestActivity.class);
                    intent.putExtra("CARD_IDS", incorrectFlashcardIds);
                    intent.putExtra("TIME_MILLIS", -1L); // Unlimited time for mistake review
                    intent.putExtra("SUBJECT_ID", subjectId);
                    intent.putExtra("SUBJECT_NAME", subjectName);
                    intent.putExtra("DIFFICULTY", difficulty);
                    startActivity(intent);
                    finish();
                });
            }
        }

        if (savedInstanceState == null && isNewTest) {
            long timestamp = System.currentTimeMillis();

            TestResult result = new TestResult();
            result.subjectId = subjectId;
            result.subjectName = subjectName != null ? subjectName : "All Subjects";
            result.difficulty = difficulty != null ? difficulty : "All";
            result.totalCards = total;
            result.correctAnswers = correctCount;
            result.incorrectAnswers = incorrectCount;
            result.unanswered = unanswered;
            result.score = score;
            result.timeLimitMillis = timeTotal;
            result.timeUsedMillis = timeUsed;
            result.completedAt = timestamp;
            result.wasTimeUp = wasTimeUp;
            result.isPermanentlySaved = false;

            List<TestResultItem> items = new ArrayList<>();
            if (resultsArray != null && questionArray != null && flashcardIdsArray != null) {
                for (int i = 0; i < resultsArray.length; i++) {
                    TestResultItem item = new TestResultItem();
                    item.flashcardId = flashcardIdsArray[i];
                    item.question = questionArray[i];
                    item.result = resultsArray[i];
                    items.add(item);
                }
            }

            isInserting = true;
            btnSaveHistory.setEnabled(false);
            btnSaveHistory.setText("Saving...");

            viewModel.insertTestResult(result, items, resultId -> {
                currentTestId = resultId;
                isInserting = false;
                if (!isPermanentlySaved) {
                    btnSaveHistory.setEnabled(true);
                    btnSaveHistory.setText("Save Permanently");
                }
            });
            // Increment tests completed today
            int dailyGoal = DailyGoalPreferences.getDailyGoal(this);
            viewModel.incrementTestsCompletedToday(dailyGoal);
        } else if (savedInstanceState != null) {
            currentTestId = savedInstanceState.getLong("currentTestId", -1);
            isInserting = savedInstanceState.getBoolean("isInserting", false);
            isPermanentlySaved = savedInstanceState.getBoolean("isPermanentlySaved", false);
            
            if (isPermanentlySaved) {
                btnSaveHistory.setEnabled(false);
                btnSaveHistory.setText("Saved!");
            } else if (isInserting) {
                btnSaveHistory.setEnabled(false);
                btnSaveHistory.setText("Saving...");
            } else {
                btnSaveHistory.setEnabled(true);
                btnSaveHistory.setText("Save Permanently");
            }
        }

        if (!isNewTest) {
            btnSaveHistory.setVisibility(android.view.View.GONE);
        } else {
            btnSaveHistory.setOnClickListener(v -> {
                if (currentTestId != -1 && !isPermanentlySaved && !isInserting) {
                    viewModel.markPermanentlySaved(currentTestId);
                    isPermanentlySaved = true;
                    btnSaveHistory.setEnabled(false);
                    btnSaveHistory.setText("Saved!");
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("currentTestId", currentTestId);
        outState.putBoolean("isInserting", isInserting);
        outState.putBoolean("isPermanentlySaved", isPermanentlySaved);
    }

    private String formatTime(long millis) {
        int m = (int) (millis / 1000) / 60;
        int s = (int) (millis / 1000) % 60;
        return String.format("%02d:%02d", m, s);
    }
}
