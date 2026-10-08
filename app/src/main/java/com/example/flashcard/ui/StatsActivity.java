package com.example.flashcard.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.R;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class StatsActivity extends AppCompatActivity {

    private FlashcardViewModel viewModel;

    private int totalCards = 0;
    private int learningCount = 0;
    private int youngCount = 0;
    private int matureCount = 0;

    private TextView tvTotalCards, tvLearningCount, tvYoungCount, tvMatureCount;
    private LinearProgressIndicator progressLearning, progressYoung, progressMature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "statistics",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_stats_title, R.string.guide_stats_message));

        Toolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarStats), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        tvTotalCards = findViewById(R.id.tvTotalCards);
        tvLearningCount = findViewById(R.id.tvLearningCount);
        tvYoungCount = findViewById(R.id.tvYoungCount);
        tvMatureCount = findViewById(R.id.tvMatureCount);

        progressLearning = findViewById(R.id.progressLearning);
        progressYoung = findViewById(R.id.progressYoung);
        progressMature = findViewById(R.id.progressMature);

        viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        // Fetch counts
        viewModel.getLearningCardsCount().observe(this, count -> {
            learningCount = count != null ? count : 0;
            updateChart();
        });

        viewModel.getYoungCardsCount().observe(this, count -> {
            youngCount = count != null ? count : 0;
            updateChart();
        });

        viewModel.getMatureCardsCount().observe(this, count -> {
            matureCount = count != null ? count : 0;
            updateChart();
        });

        viewModel.getAllFlashcards().observe(this, cards -> {
            totalCards = cards != null ? cards.size() : 0;
            tvTotalCards.setText("Total Flashcards: " + totalCards);
            updateChart();
        });

        // Test history average
        TextView tvAverageScore = findViewById(R.id.tvAverageScore);
        TextView tvTestsTaken = findViewById(R.id.tvTestsTaken);
        
        viewModel.getPermanentlySavedResults().observe(this, results -> {
            if (results != null && !results.isEmpty()) {
                long totalScore = 0;
                long maxScore = 0;
                for (com.example.flashcard.data.TestResult tr : results) {
                    totalScore += tr.correctAnswers;
                    maxScore += tr.totalCards;
                }
                if (maxScore > 0) {
                    int avg = (int) ((totalScore * 100) / maxScore);
                    tvAverageScore.setText(avg + "%");
                } else {
                    tvAverageScore.setText("0%");
                }
                tvTestsTaken.setText("From " + results.size() + " tests");
            } else {
                tvAverageScore.setText("N/A");
                tvTestsTaken.setText("From 0 tests");
            }
        });
    }

    private void updateChart() {
        if (totalCards > 0) {
            tvLearningCount.setText(String.valueOf(learningCount));
            tvYoungCount.setText(String.valueOf(youngCount));
            tvMatureCount.setText(String.valueOf(matureCount));

            progressLearning.setProgressCompat((int) ((learningCount * 100f) / totalCards), true);
            progressYoung.setProgressCompat((int) ((youngCount * 100f) / totalCards), true);
            progressMature.setProgressCompat((int) ((matureCount * 100f) / totalCards), true);
        } else {
            tvLearningCount.setText("0");
            tvYoungCount.setText("0");
            tvMatureCount.setText("0");

            progressLearning.setProgressCompat(0, true);
            progressYoung.setProgressCompat(0, true);
            progressMature.setProgressCompat(0, true);
        }
    }
}
