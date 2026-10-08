package com.example.flashcard.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.MainActivity;
import com.example.flashcard.R;
import com.example.flashcard.data.Flashcard;
import com.example.flashcard.data.Subject;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_FIRST_RUN = "is_first_run";

    private final int[] iconResIds = {
            R.string.tutorial_icon_welcome, R.string.tutorial_icon_organize,
            R.string.tutorial_icon_study, R.string.tutorial_icon_progress
    };
    private final int[] titleResIds = {
            R.string.tutorial_welcome_title, R.string.tutorial_organize_title,
            R.string.tutorial_study_title, R.string.tutorial_progress_title
    };
    private final int[] descriptionResIds = {
            R.string.tutorial_welcome_description, R.string.tutorial_organize_description,
            R.string.tutorial_study_description, R.string.tutorial_progress_description
    };
    private final int[][] featureResIds = {
            {R.string.tutorial_welcome_feature_one, R.string.tutorial_welcome_feature_two, R.string.tutorial_welcome_feature_three},
            {R.string.tutorial_organize_feature_one, R.string.tutorial_organize_feature_two, R.string.tutorial_organize_feature_three},
            {R.string.tutorial_study_feature_one, R.string.tutorial_study_feature_two, R.string.tutorial_study_feature_three},
            {R.string.tutorial_progress_feature_one, R.string.tutorial_progress_feature_two, R.string.tutorial_progress_feature_three}
    };

    private FlashcardViewModel flashcardViewModel;
    private TextView tvTutorialIcon, tvTutorialStep, tvTutorialTitle, tvTutorialDescription;
    private TextView tvTutorialFeatureOne, tvTutorialFeatureTwo, tvTutorialFeatureThree;
    private MaterialButton btnBack, btnNext, btnCreateSample, btnSkip;
    private com.google.android.material.progressindicator.LinearProgressIndicator tutorialProgress;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        tvTutorialIcon = findViewById(R.id.tvTutorialIcon);
        tvTutorialStep = findViewById(R.id.tvTutorialStep);
        tvTutorialTitle = findViewById(R.id.tvTutorialTitle);
        tvTutorialDescription = findViewById(R.id.tvTutorialDescription);
        tvTutorialFeatureOne = findViewById(R.id.tvTutorialFeatureOne);
        tvTutorialFeatureTwo = findViewById(R.id.tvTutorialFeatureTwo);
        tvTutorialFeatureThree = findViewById(R.id.tvTutorialFeatureThree);
        tutorialProgress = findViewById(R.id.tutorialProgress);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnCreateSample = findViewById(R.id.btnCreateSample);
        btnSkip = findViewById(R.id.btnSkip);

        currentPage = savedInstanceState == null ? 0 : savedInstanceState.getInt("tutorial_page", 0);

        btnBack.setOnClickListener(v -> showPreviousPage());
        btnNext.setOnClickListener(v -> {
            if (currentPage < titleResIds.length - 1) {
                currentPage++;
                renderPage();
            } else {
                markFirstRunCompleteAndProceed();
            }
        });

        btnCreateSample.setOnClickListener(v -> createSampleDeckAndProceed());
        btnSkip.setOnClickListener(v -> markFirstRunCompleteAndProceed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentPage > 0) showPreviousPage(); else finish();
            }
        });

        renderPage();
    }

    private void showPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            renderPage();
        }
    }

    private void renderPage() {
        int lastPage = titleResIds.length - 1;
        tvTutorialIcon.setText(iconResIds[currentPage]);
        tvTutorialStep.setText(getString(R.string.tutorial_step, currentPage + 1, titleResIds.length));
        tvTutorialTitle.setText(titleResIds[currentPage]);
        tvTutorialDescription.setText(descriptionResIds[currentPage]);
        tvTutorialFeatureOne.setText(featureResIds[currentPage][0]);
        tvTutorialFeatureTwo.setText(featureResIds[currentPage][1]);
        tvTutorialFeatureThree.setText(featureResIds[currentPage][2]);
        tutorialProgress.setProgressCompat((currentPage + 1) * 100 / titleResIds.length, true);
        btnBack.setVisibility(currentPage == 0 ? View.INVISIBLE : View.VISIBLE);
        btnSkip.setVisibility(currentPage == lastPage ? View.INVISIBLE : View.VISIBLE);
        btnCreateSample.setVisibility(currentPage == lastPage ? View.VISIBLE : View.GONE);
        btnNext.setText(currentPage == lastPage ? R.string.tutorial_start_empty : R.string.tutorial_next);
    }

    private void createSampleDeckAndProceed() {
        setButtonsEnabled(false);
        btnCreateSample.setText(R.string.tutorial_creating_sample);
        Subject sampleSubject = new Subject("Sample: Capitals", "EASY");
        flashcardViewModel.insertSubjectWithCallback(sampleSubject, this, subjectId -> {
            List<Flashcard> cards = new ArrayList<>();
            cards.add(new Flashcard("France", "Paris", (int) subjectId));
            cards.add(new Flashcard("Japan", "Tokyo", (int) subjectId));
            cards.add(new Flashcard("Brazil", "Brasília", (int) subjectId));
            cards.add(new Flashcard("Australia", "Canberra", (int) subjectId));
            cards.add(new Flashcard("Canada", "Ottawa", (int) subjectId));

            flashcardViewModel.insertMultiple(cards);
            runOnUiThread(this::markFirstRunCompleteAndProceed);
        });
    }

    private void markFirstRunCompleteAndProceed() {
        setButtonsEnabled(false);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void setButtonsEnabled(boolean enabled) {
        if (btnBack != null) btnBack.setEnabled(enabled);
        if (btnNext != null) btnNext.setEnabled(enabled);
        if (btnCreateSample != null) btnCreateSample.setEnabled(enabled);
        if (btnSkip != null) btnSkip.setEnabled(enabled);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tutorial_page", currentPage);
    }
}
