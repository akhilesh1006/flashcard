package com.example.flashcard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.example.flashcard.utils.DailyGoalPreferences;
import com.example.flashcard.data.StudyDay;
import com.example.flashcard.ui.GoalDetailActivity;
import com.example.flashcard.ui.StreakDetailActivity;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class MainActivity extends AppCompatActivity {

    private FlashcardViewModel flashcardViewModel;

    @Override
    protected void onResume() {
        super.onResume();
        com.example.flashcard.widget.FlashcardWidgetProvider.requestUpdate(this);
    }

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    Intent intent = new Intent(this, com.example.flashcard.ui.ImportWordActivity.class);
                    intent.putExtra("DOCX_URI", uri.toString());
                    startActivity(intent);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("is_first_run", true);
        if (isFirstRun) {
            startActivity(new Intent(this, com.example.flashcard.ui.WelcomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "home_dashboard",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_home_title, R.string.guide_home_message),
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_home_progress_title, R.string.guide_home_progress_message));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cardSubjects), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            findViewById(android.R.id.content).setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        MaterialCardView cardSubjects = findViewById(R.id.cardSubjects);
        MaterialCardView cardAllCards = findViewById(R.id.cardAllCards);
        MaterialCardView cardDueToday = findViewById(R.id.cardDueToday);
        MaterialCardView cardExamPlans = findViewById(R.id.cardExamPlans);
        TextView tvDueTodayCount = findViewById(R.id.tvDueTodayCount);
        MaterialCardView cardStats = findViewById(R.id.cardStats);
        MaterialCardView cardImportWord = findViewById(R.id.cardImportWord);
        MaterialCardView cardTestMode = findViewById(R.id.cardTestMode);
        MaterialCardView cardTestHistory = findViewById(R.id.cardTestHistory);
        MaterialCardView cardSettings = findViewById(R.id.cardSettings); // changed

        MaterialCardView cardStreak = findViewById(R.id.cardStreak);
        MaterialCardView cardGoal = findViewById(R.id.cardGoal);
        TextView tvStreakCount = findViewById(R.id.tvStreakCount);
        TextView tvStreakTitle = findViewById(R.id.tvStreakTitle);
        LinearLayout layoutWeeklyDots = findViewById(R.id.layoutWeeklyDots);

        TextView tvGoalProgress = findViewById(R.id.tvGoalProgress);
        TextView tvGoalSubtext = findViewById(R.id.tvGoalSubtext);
        com.google.android.material.progressindicator.CircularProgressIndicator progressGoal = findViewById(R.id.progressGoal);

        cardStreak.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StreakDetailActivity.class));
        });

        cardGoal.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GoalDetailActivity.class));
        });

        findViewById(R.id.btnStudyNow).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AllCardsActivity.class));
        });

        findViewById(R.id.btnGoalSettings).setOnClickListener(v -> {
            showDailyGoalSettingsDialog();
        });

        flashcardViewModel.getAllStudyDays().observe(this, studyDays -> {
            currentStudyDays = studyDays;
            updateStreakUI(studyDays, tvStreakCount, tvStreakTitle, layoutWeeklyDots);
            updateTodayProgressUI(tvGoalProgress, tvGoalSubtext, progressGoal);
        });

        cardSubjects.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SubjectsActivity.class);
            startActivity(intent);
        });

        cardAllCards.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllCardsActivity.class);
            startActivity(intent);
        });

        cardDueToday.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FlashcardActivity.class);
            intent.putExtra("DUE_TODAY_MODE", true);
            startActivity(intent);
        });

        flashcardViewModel.getActiveExamPlans().observe(this, plans -> {
            TextView tvSubtitle = findViewById(R.id.tvExamPlanSubtitle);
            if (plans != null && !plans.isEmpty()) {
                tvSubtitle.setText(plans.size() + " active plan(s)");
            } else {
                tvSubtitle.setText("Create or view active plans");
            }
        });

        cardExamPlans.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.flashcard.ui.ExamPlansActivity.class);
            startActivity(intent);
        });

        cardStats.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.flashcard.ui.StatsActivity.class);
            startActivity(intent);
        });

        long currentTime = java.util.Calendar.getInstance().getTimeInMillis();
        flashcardViewModel.getCardsDueCount(currentTime).observe(this, count -> {
            if (count != null) {
                if (count == 0) {
                    tvDueTodayCount.setText("All caught up! ðŸŽ‰");
                    tvDueTodayCount.setTextColor(getResources().getColor(R.color.goal_color, getTheme()));
                } else {
                    tvDueTodayCount.setText(count + " reviews pending");
                    tvDueTodayCount.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
                }
            }
        });

        cardImportWord.setOnClickListener(v -> {
            filePickerLauncher.launch(new String[]{
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            });
        });

        findViewById(R.id.btnHelpFormat).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.flashcard.ui.WordFormatHelpActivity.class);
            startActivity(intent);
        });

        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.flashcard.ui.SettingsActivity.class);
            startActivity(intent);
        });

        cardTestMode.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.flashcard.ui.TestSetupActivity.class);
            startActivity(intent);
        });

        cardTestHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.flashcard.ui.TestHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void updateStreakUI(List<StudyDay> studyDays, TextView tvStreakCount, TextView tvStreakTitle, LinearLayout layoutWeeklyDots) {
        int currentStreak = com.example.flashcard.utils.StreakUtils.calculateCurrentStreak(studyDays);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        tvStreakCount.setText(currentStreak + " Day Streak");
        tvStreakTitle.setText(currentStreak > 0 ? "You're on fire! Keep it up!" : "Start studying today!");

        // Update weekly dots (Mon-Sun)
        layoutWeeklyDots.removeAllViews();

        // Find Monday of the current week (assumes week starts on Monday)
        Calendar weekCal = Calendar.getInstance();
        int currentDayOfWeek = weekCal.get(Calendar.DAY_OF_WEEK);
        int distanceToMonday = currentDayOfWeek == Calendar.SUNDAY ? -6 : Calendar.MONDAY - currentDayOfWeek;
        weekCal.add(Calendar.DAY_OF_MONTH, distanceToMonday);

        String[] daysOfWeek = {"M", "T", "W", "T", "F", "S", "S"};
        for (int i = 0; i < 7; i++) {
            String dayStr = sdf.format(weekCal.getTime());
            boolean isDayActive = false;

            if (studyDays != null) {
                for (StudyDay sd : studyDays) {
                    if (sd.date.equals(dayStr) && com.example.flashcard.utils.StreakUtils.isActive(sd)) {
                        isDayActive = true;
                        break;
                    }
                }
            }

            // Create dot wrapper
            LinearLayout dotContainer = new LinearLayout(this);
            dotContainer.setOrientation(LinearLayout.VERTICAL);
            dotContainer.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            dotContainer.setLayoutParams(params);

            // Create Dot
            ImageView dot = new ImageView(this);
            int size = (int)(24 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(size, size);
            dotParams.bottomMargin = (int)(4 * getResources().getDisplayMetrics().density);
            dot.setLayoutParams(dotParams);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            if (isDayActive) {
                shape.setColor(getResources().getColor(R.color.streak_color, getTheme()));
            } else {
                shape.setColor(getResources().getColor(R.color.bg_surface, getTheme()));
                shape.setStroke(2, getResources().getColor(R.color.border_color, getTheme()));
            }
            dot.setBackground(shape);

            // Create Label
            TextView label = new TextView(this);
            label.setText(daysOfWeek[i]);
            label.setTextSize(12f);
            label.setGravity(Gravity.CENTER);
            label.setTextColor(getResources().getColor(isDayActive ? R.color.text_primary : R.color.text_secondary, getTheme()));

            dotContainer.addView(dot);
            dotContainer.addView(label);
            layoutWeeklyDots.addView(dotContainer);

            weekCal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private List<StudyDay> currentStudyDays = null;

    private void updateTodayProgressUI(TextView tvGoalProgress, TextView tvGoalSubtext, com.google.android.material.progressindicator.CircularProgressIndicator progressGoal) {
        int currentGoal = DailyGoalPreferences.getDailyGoal(this);
        com.example.flashcard.utils.GoalUtils.GoalProgress gp = com.example.flashcard.utils.GoalUtils.calculateGoalProgress(currentStudyDays, currentGoal);

        if (gp.isComplete) {
            if (gp.extra == 0) {
                tvGoalProgress.setText("GOAL COMPLETE\n" + gp.cardsStudied + " / " + gp.currentGoal);
                tvGoalSubtext.setText("Goal completed! ðŸŽ‰");
            } else {
                tvGoalProgress.setText("GOAL COMPLETE\n" + gp.cardsStudied + " / " + gp.currentGoal);
                tvGoalSubtext.setText("+" + gp.extra + " extra cards");
            }
            tvGoalSubtext.setTextColor(getResources().getColor(R.color.goal_color, getTheme()));
        } else {
            tvGoalProgress.setText(gp.cardsStudied + " / " + gp.currentGoal);
            tvGoalSubtext.setText(gp.remaining + " cards remaining");
            tvGoalSubtext.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        }

        progressGoal.setProgressCompat(gp.percentage, true);
    }

    private int tempGoal = 20;

    private void showDailyGoalSettingsDialog() {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_daily_goal, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        TextView tvGoalCount = view.findViewById(R.id.tvGoalCount);
        tempGoal = DailyGoalPreferences.getDailyGoal(this);
        tvGoalCount.setText(String.valueOf(tempGoal));

        view.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (tempGoal > 5) {
                tempGoal -= 5;
                if (tempGoal < 5) tempGoal = 5;
                tvGoalCount.setText(String.valueOf(tempGoal));
                tvGoalCount.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(() -> {
                    tvGoalCount.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                }).start();
            }
        });

        view.findViewById(R.id.btnPlus).setOnClickListener(v -> {
            if (tempGoal < 100) {
                tempGoal += 5;
                if (tempGoal > 100) tempGoal = 100;
                tvGoalCount.setText(String.valueOf(tempGoal));
                tvGoalCount.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(() -> {
                    tvGoalCount.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                }).start();
            }
        });

        view.findViewById(R.id.btnCustom).setOnClickListener(v -> {
            showCustomGoalDialog(tvGoalCount);
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            DailyGoalPreferences.setDailyGoal(this, tempGoal);
            flashcardViewModel.updateDailyGoalForToday(tempGoal);
            updateTodayProgressUI(
                findViewById(R.id.tvGoalProgress),
                findViewById(R.id.tvGoalSubtext),
                findViewById(R.id.progressGoal)
            );
            Toast.makeText(this, "Daily goal updated to " + tempGoal + " cards.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialog.show();
    }

    private void showCustomGoalDialog(TextView tvSharedGoalCount) {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_custom_goal, null);
        EditText etCustomGoal = view.findViewById(R.id.etCustomGoal);
        etCustomGoal.setText(String.valueOf(tempGoal));
        etCustomGoal.setSelection(etCustomGoal.getText().length());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String val = etCustomGoal.getText().toString().trim();
                if (!val.isEmpty()) {
                    try {
                        int newG = Integer.parseInt(val);
                        if (newG >= 5 && newG <= 100) {
                            tempGoal = newG;
                            tvSharedGoalCount.setText(String.valueOf(tempGoal));
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Please enter a goal between 5 and 100 cards.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Goal cannot be empty.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
