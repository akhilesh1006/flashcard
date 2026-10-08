package com.example.flashcard.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.R;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.example.flashcard.data.StudyDay;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StreakDetailActivity extends AppCompatActivity {

    private FlashcardViewModel flashcardViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail); // Using same simple layout

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "study_streak",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_streak_title, R.string.guide_streak_message));

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvStats = findViewById(R.id.tvStats);

        tvTitle.setText("Study Streak Details");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        flashcardViewModel.getAllStudyDays().observe(this, studyDays -> {
            int currentStreak = 0;
            int longestStreak = 0;

            // Reusing logic from MainActivity (for longest streak, we iterate all)
            if (studyDays != null && !studyDays.isEmpty()) {
                // To find longest streak and current streak
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                int tempStreak = 0;
                String expectedNextDate = "";

                // For longest streak, we need chronological or reverse chronological
                // studyDays is DESC (newest first).
                for (int i = 0; i < studyDays.size(); i++) {
                    StudyDay day = studyDays.get(i);
                    boolean isActive = day.cardsStudied > 0 || day.testsCompleted > 0;

                    if (!isActive) continue;

                    if (expectedNextDate.isEmpty() || expectedNextDate.equals(day.date)) {
                        tempStreak++;
                    } else {
                        if (tempStreak > longestStreak) longestStreak = tempStreak;
                        tempStreak = 1;
                    }

                    if (tempStreak > longestStreak) longestStreak = tempStreak;

                    try {
                        Calendar c = Calendar.getInstance();
                        c.setTime(sdf.parse(day.date));
                        c.add(Calendar.DAY_OF_YEAR, -1);
                        expectedNextDate = sdf.format(c.getTime());
                    } catch (Exception e) {}
                }

                // calculating current
                Calendar cal = Calendar.getInstance();
                String today = sdf.format(cal.getTime());
                cal.add(Calendar.DAY_OF_YEAR, -1);
                String yesterday = sdf.format(cal.getTime());

                int todayIndex = -1;
                int yesterdayIndex = -1;
                for (int i = 0; i < studyDays.size(); i++) {
                    if (studyDays.get(i).date.equals(today)) todayIndex = i;
                    if (studyDays.get(i).date.equals(yesterday)) yesterdayIndex = i;
                }

                boolean isActiveToday = todayIndex != -1 && (studyDays.get(todayIndex).cardsStudied > 0 || studyDays.get(todayIndex).testsCompleted > 0);
                boolean isActiveYesterday = yesterdayIndex != -1 && (studyDays.get(yesterdayIndex).cardsStudied > 0 || studyDays.get(yesterdayIndex).testsCompleted > 0);

                if (isActiveToday || isActiveYesterday) {
                    String checkDate = isActiveToday ? today : yesterday;
                    for (StudyDay day : studyDays) {
                        if (!day.date.equals(checkDate)) {
                            if (day.date.compareTo(checkDate) < 0) break;
                            continue;
                        }
                        if (day.cardsStudied > 0 || day.testsCompleted > 0) {
                            currentStreak++;
                            try {
                                Calendar c = Calendar.getInstance();
                                c.setTime(sdf.parse(checkDate));
                                c.add(Calendar.DAY_OF_YEAR, -1);
                                checkDate = sdf.format(c.getTime());
                            } catch (Exception e) {}
                        } else {
                            break;
                        }
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Current Streak: ").append(currentStreak).append(" days\n\n");
            sb.append("Longest Streak (All Time): ").append(longestStreak).append(" days\n\n");

            if (currentStreak > 0) {
                sb.append("Keep your streak alive by studying every day!");
            } else {
                sb.append("Start studying today to build a streak!");
            }
            tvStats.setText(sb.toString());
        });
    }
}
