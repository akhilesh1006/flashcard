package com.example.flashcard.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.R;
import com.example.flashcard.data.Flashcard;
import com.example.flashcard.data.FlashcardDatabase;
import com.example.flashcard.utils.FlashcardColorUtils;
import com.example.flashcard.viewmodel.TestViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    private TextView tvTimer;
    private TextView tvQuestionProgress;
    private FrameLayout cardContainer;
    private MaterialCardView cardFront, cardBack;
    private TextView tvQuestion, tvAnswer;

    private LinearLayout actionLayoutStart, actionLayoutResult;
    private MaterialButton btnShowAnswer, btnNextStart, btnGotItWrong, btnGotItRight, btnNextResult;

    private CountDownTimer timer;
    private boolean timerRunning = false;
    private long timeLeftMillis;

    private boolean isFlipping = false;
    private boolean isAnimatingSlide = false;

    private TestViewModel viewModel;
    private List<Flashcard> currentFlashcards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionProgress = findViewById(R.id.tvQuestionProgress);
        cardContainer = findViewById(R.id.cardContainer);
        cardFront = findViewById(R.id.cardFront);
        cardBack = findViewById(R.id.cardBack);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);

        actionLayoutStart = findViewById(R.id.actionLayoutStart);
        actionLayoutResult = findViewById(R.id.actionLayoutResult);

        btnShowAnswer = findViewById(R.id.btnShowAnswer);
        btnNextStart = findViewById(R.id.btnNextStart);
        btnGotItWrong = findViewById(R.id.btnGotItWrong);
        btnGotItRight = findViewById(R.id.btnGotItRight);
        btnNextResult = findViewById(R.id.btnNextResult);

        btnShowAnswer.setOnClickListener(v -> toggleAnswer());
        btnNextStart.setOnClickListener(v -> handleNext());
        btnNextResult.setOnClickListener(v -> handleNext());

        btnGotItWrong.setOnClickListener(v -> handleAnswerSelection(false));
        btnGotItRight.setOnClickListener(v -> handleAnswerSelection(true));

        viewModel = new ViewModelProvider(this).get(TestViewModel.class);

        long origTime = getIntent().getLongExtra("TIME_MILLIS", -1);
        int[] cardIds = getIntent().getIntArrayExtra("CARD_IDS");
        int subId = getIntent().getIntExtra("SUBJECT_ID", -1);
        String subName = getIntent().getStringExtra("SUBJECT_NAME");
        String diff = getIntent().getStringExtra("DIFFICULTY");

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "active_test",
                () -> startTestSession(origTime, cardIds, subId, subName, diff),
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_test_title, R.string.guide_test_message));
    }

    private void startTestSession(long origTime, int[] cardIds, int subId,
                                  String subName, String diff) {
        FlashcardDatabase db = FlashcardDatabase.getDatabase(getApplicationContext());
        viewModel.initData(origTime, cardIds, subId, subName, diff, db);

        if (viewModel.isTestFinished()) {
            // Restore timeLeftMillis correctly or show 0
            if (viewModel.getOriginalTimeMillis() != -1) {
                timeLeftMillis = Math.max(0, viewModel.getTimerTargetRealtime() - SystemClock.elapsedRealtime());
            }
            showEndDialog(timeLeftMillis <= 0 && viewModel.getOriginalTimeMillis() != -1 ? "TIME'S UP!" : "TEST COMPLETE!");
            return;
        }

        viewModel.getFlashcards().observe(this, flashcards -> {
            if (flashcards == null || flashcards.isEmpty()) {
                finish();
                return;
            }
            currentFlashcards = flashcards;
            updateUIForCard();
            if (viewModel.isAnswerVisible()) {
                cardFront.setVisibility(View.INVISIBLE);
                cardBack.setVisibility(View.VISIBLE);
                cardFront.setRotationY(180);
                cardBack.setRotationY(0);
                actionLayoutStart.setVisibility(View.GONE);
                actionLayoutResult.setVisibility(View.VISIBLE);
            }
            startTimer();
        });
    }

    private void updateUIForCard() {
        int currentIndex = viewModel.getCurrentIndex();
        if (currentIndex >= currentFlashcards.size() || viewModel.isTestFinished()) return;

        Flashcard current = currentFlashcards.get(currentIndex);
        tvQuestionProgress.setText("Question " + (currentIndex + 1) + " / " + currentFlashcards.size());
        tvQuestion.setText(current.getQuestion());
        tvAnswer.setText(current.getAnswer());

        int color = FlashcardColorUtils.getColorForId(current.getId());
        cardFront.setCardBackgroundColor(color);
        cardBack.setCardBackgroundColor(color);

        // Reset flipping state visually
        cardFront.setRotationY(0);
        cardFront.setVisibility(View.VISIBLE);
        cardBack.setRotationY(-180);
        cardBack.setVisibility(View.INVISIBLE);

        viewModel.setAnswerVisible(false);
        isFlipping = false;

        actionLayoutStart.setVisibility(View.VISIBLE);
        actionLayoutResult.setVisibility(View.GONE);

        btnShowAnswer.setVisibility(View.VISIBLE);
        btnShowAnswer.setEnabled(true);
        btnNextStart.setVisibility(View.VISIBLE);
        btnNextStart.setEnabled(true);

        btnGotItWrong.setEnabled(true);
        btnGotItRight.setEnabled(true);
        btnNextResult.setVisibility(View.GONE);
        btnNextResult.setEnabled(true);
    }

    private void startTimer() {
        if (viewModel.getOriginalTimeMillis() == -1) {
            tvTimer.setText("∞");
            return;
        }

        if (timerRunning) return;

        timeLeftMillis = Math.max(0, viewModel.getTimerTargetRealtime() - SystemClock.elapsedRealtime());

        timer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = Math.max(0, viewModel.getTimerTargetRealtime() - SystemClock.elapsedRealtime());
                updateTimerText();
            }

            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                updateTimerText();
                timerRunning = false;
                handleTimeOut();
            }
        }.start();

        timerRunning = true;
    }

    private void updateTimerText() {
        if (viewModel.getOriginalTimeMillis() == -1) return;
        int minutes = (int) (timeLeftMillis / 1000) / 60;
        int seconds = (int) (timeLeftMillis / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));

        if (timeLeftMillis <= 60000) {
            tvTimer.setTextColor(getResources().getColor(R.color.nav_clear_data, getTheme()));
        } else {
            tvTimer.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        }
    }

    private void handleTimeOut() {
        viewModel.setTestFinished(true);
        disableInteractions();
        showEndDialog("TIME'S UP!");
    }

    private void disableInteractions() {
        btnShowAnswer.setEnabled(false);
        btnNextStart.setEnabled(false);
        btnNextResult.setEnabled(false);
        btnGotItWrong.setEnabled(false);
        btnGotItRight.setEnabled(false);
    }

    private void toggleAnswer() {
        if (isFlipping || isAnimatingSlide || viewModel.isTestFinished()) return;
        isFlipping = true;

        btnShowAnswer.setEnabled(false);
        btnNextStart.setEnabled(false);

        float scale = getResources().getDisplayMetrics().density;
        float cameraDist = 8000 * scale;
        cardFront.setCameraDistance(cameraDist);
        cardBack.setCameraDistance(cameraDist);

        ObjectAnimator animOut = ObjectAnimator.ofFloat(cardFront, "rotationY", 0f, 180f);
        ObjectAnimator animIn = ObjectAnimator.ofFloat(cardBack, "rotationY", -180f, 0f);

        animOut.setDuration(300);
        animIn.setDuration(300);

        animOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardFront.setVisibility(View.INVISIBLE);
                cardBack.setVisibility(View.VISIBLE);
                animIn.start();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                isFlipping = false;
                if (!viewModel.isTestFinished()) {
                    btnShowAnswer.setEnabled(true);
                    btnNextStart.setEnabled(true);
                }
            }
        });

        animIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isFlipping = false;
                viewModel.setAnswerVisible(true);

                actionLayoutStart.setVisibility(View.GONE);
                actionLayoutResult.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                isFlipping = false;
                if (!viewModel.isTestFinished()) {
                    btnShowAnswer.setEnabled(true);
                    btnNextStart.setEnabled(true);
                }
            }
        });

        animOut.start();
    }

    private void handleAnswerSelection(boolean correct) {
        if (viewModel.isTestFinished()) return;

        int currentIndex = viewModel.getCurrentIndex();
        Integer currentResult = viewModel.getResultsMap().get(currentIndex);
        if (currentResult == null || currentResult == -1) {
            if (correct) {
                viewModel.incrementCorrectCount();
            } else {
                viewModel.incrementIncorrectCount();
            }
            viewModel.updateResult(currentIndex, correct ? 1 : 0);
        }

        btnGotItWrong.setEnabled(false);
        btnGotItRight.setEnabled(false);
        btnNextResult.setVisibility(View.VISIBLE);
        btnNextResult.setEnabled(true);
    }

    private void handleNext() {
        if (isFlipping || isAnimatingSlide || viewModel.isTestFinished()) return;

        if (viewModel.getCurrentIndex() < currentFlashcards.size() - 1) {
            animateSlideNext();
        } else {
            if (timer != null) timer.cancel();
            viewModel.setTestFinished(true);

            if (viewModel.getOriginalTimeMillis() != -1) {
                timeLeftMillis = Math.max(0, viewModel.getTimerTargetRealtime() - SystemClock.elapsedRealtime());
            }
            showEndDialog("TEST COMPLETE!");
        }
    }

    private void animateSlideNext() {
        isAnimatingSlide = true;
        btnNextStart.setEnabled(false);
        btnNextResult.setEnabled(false);
        btnShowAnswer.setEnabled(false);

        ObjectAnimator slideOut = ObjectAnimator.ofFloat(cardContainer, "translationX", 0f, -cardContainer.getWidth());
        slideOut.setDuration(250);
        slideOut.setInterpolator(new AccelerateDecelerateInterpolator());

        slideOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewModel.setCurrentIndex(viewModel.getCurrentIndex() + 1);
                updateUIForCard(); // Resets local flags like isFlipping, changes views

                if (!viewModel.isTestFinished()) {
                    btnShowAnswer.setEnabled(false);
                    btnNextStart.setEnabled(false);
                }

                cardContainer.setTranslationX(cardContainer.getWidth());
                ObjectAnimator slideIn = ObjectAnimator.ofFloat(cardContainer, "translationX", cardContainer.getWidth(), 0f);
                slideIn.setDuration(250);
                slideIn.setInterpolator(new AccelerateDecelerateInterpolator());
                slideIn.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isAnimatingSlide = false;
                        if (!viewModel.isTestFinished()) {
                            btnShowAnswer.setEnabled(true);
                            btnNextStart.setEnabled(true);
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        isAnimatingSlide = false;
                        if (!viewModel.isTestFinished()) {
                            btnShowAnswer.setEnabled(true);
                            btnNextStart.setEnabled(true);
                        }
                    }
                });
                slideIn.start();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimatingSlide = false;
                if (!viewModel.isTestFinished()) {
                    btnShowAnswer.setEnabled(true);
                    btnNextStart.setEnabled(true);
                }
            }
        });
        slideOut.start();
    }

    private void showEndDialog(String title) {
        if (isFinishing()) return;

        int total = currentFlashcards.size();
        int correct = viewModel.getCorrectCount();
        int incorrect = viewModel.getIncorrectCount();
        int unanswered = total - correct - incorrect;
        int score = total > 0 ? (int) (((float) correct / total) * 100) : 0;

        String msg = "Cards: " + total + "\n" +
                     "Attempted: " + (correct + incorrect) + "\n" +
                     "Correct: " + correct + "\n" +
                     "Incorrect: " + incorrect + "\n";

        if (unanswered > 0) {
            msg += "Unanswered: " + unanswered + "\n";
        }

        msg += "Score: " + score + "%\n";

        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("View Results", (dialog, which) -> {
                goToResults();
            })
            .setNegativeButton("Back to Home", (dialog, which) -> {
                finish();
            })
            .show();
    }

    private void goToResults() {
        Intent resIntent = new Intent(this, TestResultActivity.class);
        int correct = viewModel.getCorrectCount();
        int incorrect = viewModel.getIncorrectCount();
        int total = currentFlashcards.size();
        long origTimeMillis = viewModel.getOriginalTimeMillis();

        resIntent.putExtra("CORRECT", correct);
        resIntent.putExtra("INCORRECT", incorrect);
        resIntent.putExtra("TOTAL", total);
        resIntent.putExtra("TIME_USED", origTimeMillis == -1 ? -1 : origTimeMillis - timeLeftMillis);
        resIntent.putExtra("TIME_TOTAL", origTimeMillis);
        resIntent.putExtra("SUBJECT_ID", viewModel.getSubjectId());
        resIntent.putExtra("SUBJECT_NAME", viewModel.getSubjectName());
        resIntent.putExtra("DIFFICULTY", viewModel.getDifficulty());
        resIntent.putExtra("WAS_TIME_UP", origTimeMillis != -1 && (timeLeftMillis <= 0 || viewModel.isTestFinished() && timeLeftMillis == 0));
        resIntent.putExtra("IS_NEW_TEST", true);

        int[] resultsArray = new int[total];
        String[] questionArray = new String[total];
        int[] flashcardIdsArray = new int[total];
        for(int i = 0; i < total; i++) {
            Integer res = viewModel.getResultsMap().get(i);
            resultsArray[i] = res != null ? res : -1;
            questionArray[i] = currentFlashcards.get(i).getQuestion();
            flashcardIdsArray[i] = currentFlashcards.get(i).getId();
        }
        resIntent.putExtra("RESULTS_ARRAY", resultsArray);
        resIntent.putExtra("QUESTIONS_ARRAY", questionArray);
        resIntent.putExtra("FLASHCARD_IDS_ARRAY", flashcardIdsArray);

        startActivity(resIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (viewModel.isTestFinished()) {
            super.onBackPressed();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Leave Test?")
            .setMessage("Your current test progress will be lost.")
            .setPositiveButton("Leave Test", (dialog, which) -> {
                if (timer != null) timer.cancel();
                finish();
            })
            .setNegativeButton("Stay", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
