package com.example.flashcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.data.Flashcard;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.example.flashcard.utils.FlashcardColorUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.flashcard.utils.DailyGoalPreferences;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class FlashcardActivity extends AppCompatActivity {

    private FlashcardViewModel flashcardViewModel;
    private List<Flashcard> flashcards = new ArrayList<>();
    private int currentIndex = 0;

    private ConstraintLayout clFlashcardContent;
    private LinearLayout llEmptyState;
    private TextView tvCounter;

    private MaterialCardView cvFlashcardFront;
    private MaterialCardView cvFlashcardBack;
    private TextView tvQuestionLabel;
    private TextView tvQuestion;
    private TextView tvAnswerLabel;
    private TextView tvAnswer;

    private MaterialButton btnRatingAgain;
    private MaterialButton btnRatingHard;
    private MaterialButton btnRatingGood;
    private MaterialButton btnRatingEasy;

    private MaterialCardView cvComment;
    private TextView tvDifficultyLabel;
    private LinearLayout llDifficulty;
    private TextView tvCommentContent;
    private MaterialButton btnAddEditComment;

    private ImageButton btnEdit;
    private ImageButton btnDelete;
    private MaterialButton btnShowAnswer;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;
    private MaterialButton btnEmptyAdd;
    private FloatingActionButton fabAdd;

    private boolean isAnswerVisible = false;
    private boolean isFlipping = false;
    private boolean isAnimatingSlide = false;
    private boolean jumpToLastOnNextUpdate = false;
    private Set<Integer> cardsStudiedThisSession = new HashSet<>();

    @Override
    protected void onStop() {
        super.onStop();
        com.example.flashcard.widget.FlashcardWidgetProvider.requestUpdate(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();

        Intent intent = getIntent();
        int subjectId = intent.getIntExtra("SUBJECT_ID", -1);
        int startIndex = intent.getIntExtra("START_INDEX", 0);
        boolean isDueTodayMode = intent.getBooleanExtra("DUE_TODAY_MODE", false);
        int planId = intent.getIntExtra("plan_id", -1);
        this.currentIndex = startIndex;

        if (isDueTodayMode) {
            com.example.flashcard.utils.FeatureTutorial.showOnce(this, "due_today",
                    com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_due_today_title, R.string.guide_due_today_message),
                    com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_flashcard_rating_title, R.string.guide_flashcard_rating_message));
        } else {
            com.example.flashcard.utils.FeatureTutorial.showOnce(this, "flashcard_study",
                    com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_flashcard_title, R.string.guide_flashcard_message),
                    com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_flashcard_rating_title, R.string.guide_flashcard_rating_message));
        }

        if (subjectId == -1 && planId == -1 && !isDueTodayMode) {
            // It's "All Cards" view, handled differently? Actually AllCardsActivity handles All Cards.
        }

        if (subjectId == -1 || isDueTodayMode || planId != -1) {
            fabAdd.setVisibility(View.GONE);
            btnEmptyAdd.setVisibility(View.GONE);
        }

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        LiveData<List<com.example.flashcard.data.Flashcard>> source;
        if (planId != -1) {
             // In a real app we'd fetch actual PlanCards, but the prompt says
             // "fetch flashcards uniquely from the plan's available cards".
             // We can use a new query in ViewModel or just we can add it to ViewModel:
             // getFlashcardsForPlan(planId) -> returns LiveData<List<Flashcard>>
             source = flashcardViewModel.getUncoveredFlashcardsForPlan(planId);
        } else if (isDueTodayMode) {
            long currentTime = java.util.Calendar.getInstance().getTimeInMillis();
            source = flashcardViewModel.getCardsDue(currentTime);
        } else if (subjectId == -1) {
            source = flashcardViewModel.getAllFlashcards();
        } else {
            source = flashcardViewModel.getFlashcardsForSubject(subjectId);
        }

        source.observe(this, newFlashcards -> {
            flashcards = newFlashcards;

            if (jumpToLastOnNextUpdate && flashcards.size() > 0) {
                currentIndex = flashcards.size() - 1;
                jumpToLastOnNextUpdate = false;
            }

            // Adjust index if we deleted the last item
            if (currentIndex >= flashcards.size() && flashcards.size() > 0) {
                currentIndex = flashcards.size() - 1;
            }

            updateUI();
        });
    }

    private void initViews() {
        clFlashcardContent = findViewById(R.id.clFlashcardContent);
        llEmptyState = findViewById(R.id.llEmptyState);

        tvCounter = findViewById(R.id.tvCounter);

        cvFlashcardFront = findViewById(R.id.cvFlashcardFront);
        cvFlashcardBack = findViewById(R.id.cvFlashcardBack);

        tvQuestionLabel = findViewById(R.id.tvQuestionLabel);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvAnswerLabel = findViewById(R.id.tvAnswerLabel);
        tvAnswer = findViewById(R.id.tvAnswer);

        btnRatingAgain = findViewById(R.id.btnRatingAgain);
        btnRatingHard = findViewById(R.id.btnRatingHard);
        btnRatingGood = findViewById(R.id.btnRatingGood);
        btnRatingEasy = findViewById(R.id.btnRatingEasy);

        cvComment = findViewById(R.id.cvComment);
        tvDifficultyLabel = findViewById(R.id.tvDifficultyLabel);
        llDifficulty = findViewById(R.id.llDifficulty);
        tvCommentContent = findViewById(R.id.tvCommentContent);
        btnAddEditComment = findViewById(R.id.btnAddEditComment);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnShowAnswer = findViewById(R.id.btnShowAnswer);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnEmptyAdd = findViewById(R.id.btnEmptyAdd);
        fabAdd = findViewById(R.id.fabAdd);

        TextView emptyTitle = llEmptyState.findViewById(R.id.tvEmptyTitle);
        TextView emptyDesc = llEmptyState.findViewById(R.id.tvEmptyDesc);
        int subjectId = getIntent().getIntExtra("SUBJECT_ID", -1);
        boolean isDueTodayMode = getIntent().getBooleanExtra("DUE_TODAY_MODE", false);

        if (isDueTodayMode) {
            emptyTitle.setText("All caught up!");
            emptyDesc.setText("You have no cards due right now.");
        } else if (subjectId == -1) {
            emptyTitle.setText("No flashcards available.");
            emptyDesc.setText("Add flashcards from a subject to start studying.");
        } else {
            emptyTitle.setText("No flashcards in this subject yet.");
            emptyDesc.setText("Create a flashcard to get started.");
        }
    }

    private void setupListeners() {
        btnShowAnswer.setOnClickListener(v -> toggleAnswer());

        btnNext.setOnClickListener(v -> {
            if (currentIndex < flashcards.size() - 1) {
                animateCardChange(true);
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) {
                animateCardChange(false);
            }
        });

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
        btnEmptyAdd.setOnClickListener(v -> showAddEditDialog(null));

        btnEdit.setOnClickListener(v -> {
            if (flashcards != null && flashcards.size() > 0) {
                showAddEditDialog(flashcards.get(currentIndex));
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (flashcards != null && flashcards.size() > 0) {
                showDeleteConfirmation(flashcards.get(currentIndex));
            }
        });

        btnRatingAgain.setOnClickListener(v -> applyRating(1)); // Again = 1
        btnRatingHard.setOnClickListener(v -> applyRating(3)); // Hard = 3
        btnRatingGood.setOnClickListener(v -> applyRating(4)); // Good = 4
        btnRatingEasy.setOnClickListener(v -> applyRating(5)); // Easy = 5

        cvComment.setOnClickListener(v -> {
            if (flashcards != null && flashcards.size() > 0) {
                showEditCommentDialog(flashcards.get(currentIndex));
            }
        });

        btnAddEditComment.setOnClickListener(v -> {
            if (flashcards != null && flashcards.size() > 0) {
                showEditCommentDialog(flashcards.get(currentIndex));
            }
        });
    }

    private void applyRating(int quality) {
        if (flashcards == null || flashcards.isEmpty()) return;
        Flashcard currentCard = flashcards.get(currentIndex);

        currentCard.applySm2Rating(quality);
        flashcardViewModel.update(currentCard);

        int planId = getIntent().getIntExtra("plan_id", -1);
        if (planId != -1) {
            flashcardViewModel.recordExamPlanStudyCompletion(planId, currentCard.getId(), 0);
        }

        Toast.makeText(this, "Next review in " + currentCard.getInterval() + " days", Toast.LENGTH_SHORT).show();

        if (currentIndex < flashcards.size() - 1) {
            animateCardChange(true);
        } else {
            Toast.makeText(this, "You have reached the end of the deck", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        if (flashcards == null || flashcards.isEmpty()) {
            clFlashcardContent.setVisibility(View.GONE);
            fabAdd.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            clFlashcardContent.setVisibility(View.VISIBLE);

            // Only show FAB if we are inside a subject (subjectId != -1)
            int subjectId = getIntent().getIntExtra("SUBJECT_ID", -1);
            if (subjectId != -1) {
                fabAdd.setVisibility(View.VISIBLE);
            } else {
                fabAdd.setVisibility(View.GONE);
            }

            llEmptyState.setVisibility(View.GONE);

            Flashcard currentFlashcard = flashcards.get(currentIndex);
            tvQuestion.setText(currentFlashcard.getQuestion());
            tvAnswer.setText(currentFlashcard.getAnswer());

            // Personal comment update
            String comment = currentFlashcard.getComment();
            if (TextUtils.isEmpty(comment)) {
                tvCommentContent.setText("No personal comment");
                tvCommentContent.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
                btnAddEditComment.setText("Add Comment");
            } else {
                tvCommentContent.setText(comment);
                tvCommentContent.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                btnAddEditComment.setText("Edit Comment");
            }

            int bgColor = FlashcardColorUtils.getColorForId(currentFlashcard.getId());
            int pText = FlashcardColorUtils.getPrimaryTextColor(bgColor);
            int sText = FlashcardColorUtils.getSecondaryTextColor(bgColor);

            cvFlashcardFront.setCardBackgroundColor(bgColor);
            cvFlashcardBack.setCardBackgroundColor(bgColor);

            tvQuestionLabel.setTextColor(sText);
            tvQuestion.setTextColor(pText);

            tvAnswerLabel.setTextColor(sText);
            tvAnswer.setTextColor(pText);

            tvCounter.setText("Card " + (currentIndex + 1) + " of " + flashcards.size());

            if (!isAnswerVisible) {
                tvDifficultyLabel.setVisibility(View.GONE);
                llDifficulty.setVisibility(View.GONE);
                btnShowAnswer.setVisibility(View.VISIBLE);
            } else {
                tvDifficultyLabel.setVisibility(View.VISIBLE);
                llDifficulty.setVisibility(View.VISIBLE);
                btnShowAnswer.setVisibility(View.GONE);
            }

            btnPrevious.setEnabled(currentIndex > 0);
            btnNext.setEnabled(currentIndex < flashcards.size() - 1);
        }
    }

    private void showEditCommentDialog(Flashcard flashcard) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_comment, null);
        builder.setView(dialogView);

        TextInputEditText etPersonalComment = dialogView.findViewById(R.id.etPersonalComment);
        if (flashcard.getComment() != null) {
            etPersonalComment.setText(flashcard.getComment());
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newComment = etPersonalComment.getText() != null ? etPersonalComment.getText().toString().trim() : "";
            flashcard.setComment(newComment);
            flashcardViewModel.update(flashcard);
        });

        if (!TextUtils.isEmpty(flashcard.getComment())) {
            builder.setNeutralButton("Clear", (dialog, which) -> {
                flashcard.setComment("");
                flashcardViewModel.update(flashcard);
            });
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void toggleAnswer() {
        if (isFlipping) return;

        btnShowAnswer.setEnabled(false);
        btnNext.setEnabled(false);
        btnPrevious.setEnabled(false);
        isFlipping = true;

        float scale = getResources().getDisplayMetrics().density;
        float cameraDist = 8000 * scale;
        cvFlashcardFront.setCameraDistance(cameraDist);
        cvFlashcardBack.setCameraDistance(cameraDist);

        if (isAnswerVisible) {
            // BACK to FRONT
            ObjectAnimator animOut = ObjectAnimator.ofFloat(cvFlashcardBack, "rotationY", 0f, -90f);
            animOut.setDuration(250);
            animOut.setInterpolator(new AccelerateInterpolator());

            ObjectAnimator animIn = ObjectAnimator.ofFloat(cvFlashcardFront, "rotationY", 90f, 0f);
            animIn.setDuration(250);
            animIn.setInterpolator(new DecelerateInterpolator());

            animOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cvFlashcardBack.setVisibility(View.GONE);
                    cvFlashcardFront.setVisibility(View.VISIBLE);
                    btnShowAnswer.setText("Show Answer");
                    animIn.start();
                }
            });
            animIn.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isFlipping = false;
                    isAnswerVisible = false;
                    btnShowAnswer.setVisibility(View.VISIBLE);
                    tvDifficultyLabel.setVisibility(View.GONE);
                    llDifficulty.setVisibility(View.GONE);
                    btnShowAnswer.setEnabled(true);
                    btnNext.setEnabled(currentIndex < flashcards.size() - 1);
                    btnPrevious.setEnabled(currentIndex > 0);
                }
            });
            animOut.start();
        } else {
            // FRONT to BACK
            ObjectAnimator animOut = ObjectAnimator.ofFloat(cvFlashcardFront, "rotationY", 0f, 90f);
            animOut.setDuration(250);
            animOut.setInterpolator(new AccelerateInterpolator());

            ObjectAnimator animIn = ObjectAnimator.ofFloat(cvFlashcardBack, "rotationY", -90f, 0f);
            animIn.setDuration(250);
            animIn.setInterpolator(new DecelerateInterpolator());

            animOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cvFlashcardFront.setVisibility(View.GONE);
                    cvFlashcardBack.setVisibility(View.VISIBLE);
                    btnShowAnswer.setText("Hide Answer");
                    animIn.start();
                }
            });
            animIn.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isFlipping = false;
                    isAnswerVisible = true;
                    btnShowAnswer.setVisibility(View.GONE);
                    tvDifficultyLabel.setVisibility(View.VISIBLE);
                    llDifficulty.setVisibility(View.VISIBLE);
                    btnShowAnswer.setEnabled(true);
                    btnNext.setEnabled(currentIndex < flashcards.size() - 1);
                    btnPrevious.setEnabled(currentIndex > 0);

                    // Track study activity
                    if (flashcards != null && flashcards.size() > 0) {
                        int currentCardId = flashcards.get(currentIndex).getId();
                        if (!cardsStudiedThisSession.contains(currentCardId)) {
                            cardsStudiedThisSession.add(currentCardId);
                            int dailyGoal = DailyGoalPreferences.getDailyGoal(FlashcardActivity.this);
                            flashcardViewModel.incrementCardsStudiedToday(dailyGoal);
                        }
                    }
                }
            });
            animOut.start();
        }
    }

    private void resetAnswerVisibility() {
        cvFlashcardFront.animate().cancel();
        cvFlashcardBack.animate().cancel();

        cvFlashcardFront.setRotationY(0f);
        cvFlashcardFront.setVisibility(View.VISIBLE);

        cvFlashcardBack.setRotationY(-90f);
        cvFlashcardBack.setVisibility(View.GONE);

        btnShowAnswer.setText("Show Answer");
        btnShowAnswer.setVisibility(View.VISIBLE);
        tvDifficultyLabel.setVisibility(View.GONE);
        llDifficulty.setVisibility(View.GONE);
        isAnswerVisible = false;
        isFlipping = false;

        btnShowAnswer.setEnabled(true);
        btnNext.setEnabled(currentIndex < flashcards.size() - 1);
        btnPrevious.setEnabled(currentIndex > 0);
    }

    private void animateCardChange(boolean isNext) {
        if (isAnimatingSlide) return;
        isAnimatingSlide = true;

        disableButtons();

        // 1. Snapshot the current visual state
        View container = findViewById(R.id.flFlashcardContainer);
        android.graphics.Bitmap bitmap = getCardBitmap(container);
        android.widget.ImageView dummyView = null;
        if (bitmap != null) {
            dummyView = new android.widget.ImageView(this);
            dummyView.setImageBitmap(bitmap);
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(container.getWidth(), container.getHeight());
            params.topToTop = container.getId();
            params.bottomToBottom = container.getId();
            params.startToStart = container.getId();
            params.endToEnd = container.getId();

            ((androidx.constraintlayout.widget.ConstraintLayout) findViewById(R.id.clFlashcardContent)).addView(dummyView, params);
        }

        // Determine offset amount
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float outTargetX = isNext ? -screenWidth : screenWidth;
        float inStartX = isNext ? screenWidth : -screenWidth;

        // 2. Safely navigate
        if (isNext) currentIndex++; else currentIndex--;

        // 3. Reset 3D flip cleanly before new data is populated
        resetAnswerVisibility();

        // 4. Update data on real container
        updateUI();

        // Disable buttons again because resetAnswerVisibility() and updateUI() may have re-enabled them
        disableButtons();

        // 5. Hide real container quickly until it slides in
        container.setTranslationX(inStartX);

        // 6. Animate dummy (old card) OUT
        android.animation.ObjectAnimator animOut = null;
        if (dummyView != null) {
            animOut = android.animation.ObjectAnimator.ofFloat(dummyView, "translationX", 0f, outTargetX);
            animOut.setDuration(350);
            animOut.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            final android.widget.ImageView finalDummyView = dummyView;
            animOut.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    ((androidx.constraintlayout.widget.ConstraintLayout) findViewById(R.id.clFlashcardContent)).removeView(finalDummyView);
                }
            });
        }

        // 7. Animate real container (new card) IN
        android.animation.ObjectAnimator animIn = android.animation.ObjectAnimator.ofFloat(container, "translationX", inStartX, 0f);
        animIn.setDuration(350);
        animIn.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        animIn.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimatingSlide = false;
                enableButtons();
            }
        });

        if (animOut != null) animOut.start();
        animIn.start();
    }

    private android.graphics.Bitmap getCardBitmap(View view) {
        if (view.getWidth() <= 0 || view.getHeight() <= 0) return null;
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(view.getWidth(), view.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void disableButtons() {
        if (btnShowAnswer != null) btnShowAnswer.setEnabled(false);
        if (btnNext != null) btnNext.setEnabled(false);
        if (btnPrevious != null) btnPrevious.setEnabled(false);
        if (btnEdit != null) btnEdit.setEnabled(false);
        if (btnDelete != null) btnDelete.setEnabled(false);
        if (fabAdd != null) fabAdd.setEnabled(false);
        if (btnEmptyAdd != null) btnEmptyAdd.setEnabled(false);
        if (btnRatingAgain != null) btnRatingAgain.setEnabled(false);
        if (btnRatingHard != null) btnRatingHard.setEnabled(false);
        if (btnRatingGood != null) btnRatingGood.setEnabled(false);
        if (btnRatingEasy != null) btnRatingEasy.setEnabled(false);
        if (btnAddEditComment != null) btnAddEditComment.setEnabled(false);
    }

    private void enableButtons() {
        if (btnShowAnswer != null) btnShowAnswer.setEnabled(true);
        if (btnNext != null) btnNext.setEnabled(currentIndex < flashcards.size() - 1);
        if (btnPrevious != null) btnPrevious.setEnabled(currentIndex > 0);
        if (btnEdit != null) btnEdit.setEnabled(true);
        if (btnDelete != null) btnDelete.setEnabled(true);
        if (fabAdd != null) fabAdd.setEnabled(true);
        if (btnEmptyAdd != null) btnEmptyAdd.setEnabled(true);
        if (btnRatingAgain != null) btnRatingAgain.setEnabled(true);
        if (btnRatingHard != null) btnRatingHard.setEnabled(true);
        if (btnRatingGood != null) btnRatingGood.setEnabled(true);
        if (btnRatingEasy != null) btnRatingEasy.setEnabled(true);
        if (btnAddEditComment != null) btnAddEditComment.setEnabled(true);
    }

    private void showAddEditDialog(Flashcard flashcardToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_flashcard, null);
        builder.setView(dialogView);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etQuestion = dialogView.findViewById(R.id.etQuestion);
        TextInputEditText etAnswer = dialogView.findViewById(R.id.etAnswer);
        TextInputEditText etComment = dialogView.findViewById(R.id.etComment);
        MaterialButton btnDialogDiffEasy = dialogView.findViewById(R.id.btnDialogDiffEasy);
        MaterialButton btnDialogDiffMedium = dialogView.findViewById(R.id.btnDialogDiffMedium);
        MaterialButton btnDialogDiffHard = dialogView.findViewById(R.id.btnDialogDiffHard);

        final String[] selectedDiff = new String[]{"MEDIUM"};

        View.OnClickListener diffClickListener = v -> {
            btnDialogDiffEasy.setChecked(false);
            btnDialogDiffMedium.setChecked(false);
            btnDialogDiffHard.setChecked(false);

            if (v.getId() == R.id.btnDialogDiffEasy) {
                btnDialogDiffEasy.setChecked(true);
                selectedDiff[0] = "EASY";
            } else if (v.getId() == R.id.btnDialogDiffMedium) {
                btnDialogDiffMedium.setChecked(true);
                selectedDiff[0] = "MEDIUM";
            } else if (v.getId() == R.id.btnDialogDiffHard) {
                btnDialogDiffHard.setChecked(true);
                selectedDiff[0] = "HARD";
            }
        };

        btnDialogDiffEasy.setOnClickListener(diffClickListener);
        btnDialogDiffMedium.setOnClickListener(diffClickListener);
        btnDialogDiffHard.setOnClickListener(diffClickListener);

        if (flashcardToEdit != null) {
            tvDialogTitle.setText("Edit Flashcard");
            etQuestion.setText(flashcardToEdit.getQuestion());
            etAnswer.setText(flashcardToEdit.getAnswer());
            etComment.setText(flashcardToEdit.getComment());
            selectedDiff[0] = flashcardToEdit.getDifficulty();
        } else {
            tvDialogTitle.setText("Add Flashcard");
            selectedDiff[0] = "MEDIUM";
        }

        btnDialogDiffEasy.setChecked("EASY".equals(selectedDiff[0]));
        btnDialogDiffMedium.setChecked("MEDIUM".equals(selectedDiff[0]));
        btnDialogDiffHard.setChecked("HARD".equals(selectedDiff[0]));

        builder.setPositiveButton("Save", (dialog, which) -> {
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String question = etQuestion.getText() != null ? etQuestion.getText().toString().trim() : "";
            String answer = etAnswer.getText() != null ? etAnswer.getText().toString().trim() : "";
            String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";

            if (TextUtils.isEmpty(question) || TextUtils.isEmpty(answer)) {
                Toast.makeText(FlashcardActivity.this, "Question and Answer cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (flashcardToEdit == null) {
                int targetSubjectId = getIntent().getIntExtra("SUBJECT_ID", -1);
                if (targetSubjectId == -1) {
                    Toast.makeText(FlashcardActivity.this, "Cannot add directly from All Cards.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                Flashcard newFlashcard = new Flashcard(question, answer, targetSubjectId, comment, selectedDiff[0]);
                flashcardViewModel.insert(newFlashcard);

                jumpToLastOnNextUpdate = true;
                resetAnswerVisibility();
            } else {
                flashcardToEdit.setQuestion(question);
                flashcardToEdit.setAnswer(answer);
                flashcardToEdit.setComment(comment);
                flashcardToEdit.setDifficulty(selectedDiff[0]);
                flashcardViewModel.update(flashcardToEdit);
            }
            dialog.dismiss();
        });
    }

    private void showDeleteConfirmation(Flashcard flashcard) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Flashcard?")
                .setMessage("Are you sure you want to delete this flashcard?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    flashcardViewModel.delete(flashcard);
                    resetAnswerVisibility();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
