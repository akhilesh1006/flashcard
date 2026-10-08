package com.example.flashcard.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.R;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.example.flashcard.data.StudyDay;
import com.example.flashcard.utils.DailyGoalPreferences;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GoalDetailActivity extends AppCompatActivity {

    private FlashcardViewModel flashcardViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail); // Using a generalized layout for both or specialized

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "daily_goal",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_goal_title, R.string.guide_goal_message));

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvStats = findViewById(R.id.tvStats);

        tvTitle.setText("Daily Goal Details");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        flashcardViewModel.getAllStudyDays().observe(this, studyDays -> {
            int currentGoal = DailyGoalPreferences.getDailyGoal(this);
            int todayCards = 0;
            int totalCardsAllTime = 0;
            int totalGoalsHit = 0;

            String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

            if (studyDays != null) {
                for (StudyDay day : studyDays) {
                    if (day.date.equals(todayStr)) {
                        todayCards = day.cardsStudied;
                    }
                    totalCardsAllTime += day.cardsStudied;
                    if (day.goalCompleted) {
                        totalGoalsHit++;
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Current Daily Goal: ").append(currentGoal).append(" cards\n\n");
            sb.append("Today's Progress: ").append(todayCards).append(" / ").append(currentGoal).append("\n\n");
            sb.append("Total Cards Studied (All Time): ").append(totalCardsAllTime).append("\n\n");
            sb.append("Total Goals Achieved: ").append(totalGoalsHit).append(" days\n");

            tvStats.setText(sb.toString());
        });
    }
}
