package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flashcard.R;
import com.example.flashcard.data.Flashcard;
import com.example.flashcard.data.FlashcardDatabase;
import com.example.flashcard.data.Subject;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSetupActivity extends AppCompatActivity {

    private Spinner spinnerSubject;
    private Spinner spinnerTime;
    private Spinner spinnerNumCards;
    
    private CheckBox checkDiffAll, checkDiffEasy, checkDiffMedium, checkDiffHard;
    
    private MaterialButton btnStartTest;

    private List<Subject> subjectList = new ArrayList<>();
    private List<String> subjectNames = new ArrayList<>();
    
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_setup);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "test_setup",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_test_setup_title, R.string.guide_test_setup_message));

        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerTime = findViewById(R.id.spinnerTime);
        spinnerNumCards = findViewById(R.id.spinnerNumCards);
        
        checkDiffAll = findViewById(R.id.checkDiffAll);
        checkDiffEasy = findViewById(R.id.checkDiffEasy);
        checkDiffMedium = findViewById(R.id.checkDiffMedium);
        checkDiffHard = findViewById(R.id.checkDiffHard);
        
        btnStartTest = findViewById(R.id.btnStartTest);

        setupSpinners();
        setupDiffCheckboxes();
        loadSubjects();

        btnStartTest.setOnClickListener(v -> validateAndStartTest());
    }
    
    private void setupSpinners() {
        // Time options
        String[] times = {"1 minute", "2 minutes", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "Unlimited"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, times);
        spinnerTime.setAdapter(timeAdapter);
        spinnerTime.setSelection(2); // default 5 minutes
        
        // Num Cards options
        String[] cards = {"5", "10", "15", "20", "30", "All available"};
        ArrayAdapter<String> cardsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cards);
        spinnerNumCards.setAdapter(cardsAdapter);
        spinnerNumCards.setSelection(1); // default 10
    }
    
    private void setupDiffCheckboxes() {
        checkDiffAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkDiffEasy.setChecked(false);
                checkDiffMedium.setChecked(false);
                checkDiffHard.setChecked(false);
            }
        });
        
        View.OnClickListener childListener = v -> {
            if (((CheckBox)v).isChecked()) {
                checkDiffAll.setChecked(false);
            }
        };
        checkDiffEasy.setOnClickListener(childListener);
        checkDiffMedium.setOnClickListener(childListener);
        checkDiffHard.setOnClickListener(childListener);
    }

    private void loadSubjects() {
        executorService.execute(() -> {
            FlashcardDatabase db = FlashcardDatabase.getDatabase(getApplicationContext());
            List<Subject> subjects = db.subjectDao().getAllSubjectsSync();
            
            subjectList = subjects;
            subjectNames = new ArrayList<>();
            subjectNames.add("All Subjects");
            for (Subject s : subjects) {
                subjectNames.add(s.getName());
            }
            
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(TestSetupActivity.this, 
                        android.R.layout.simple_spinner_dropdown_item, subjectNames);
                spinnerSubject.setAdapter(adapter);
            });
        });
    }
    
    private void validateAndStartTest() {
        int selectedSubjectIndex = spinnerSubject.getSelectedItemPosition();
        int subjectId = -1; // -1 means All Subjects
        if (selectedSubjectIndex > 0 && selectedSubjectIndex - 1 < subjectList.size()) {
            subjectId = subjectList.get(selectedSubjectIndex - 1).getId();
        }
        
        String timeSelection = (String) spinnerTime.getSelectedItem();
        long timeMillis = parseTime(timeSelection);
        
        String numCardsSelection = (String) spinnerNumCards.getSelectedItem();
        int requestedNumCards = numCardsSelection.equals("All available") ? Integer.MAX_VALUE : Integer.parseInt(numCardsSelection);
        
        boolean diffAll = checkDiffAll.isChecked();
        boolean diffEasy = checkDiffEasy.isChecked();
        boolean diffMedium = checkDiffMedium.isChecked();
        boolean diffHard = checkDiffHard.isChecked();
        
        if (!diffAll && !diffEasy && !diffMedium && !diffHard) {
            Toast.makeText(this, "Please select at least one difficulty.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        final int finalSubjectId = subjectId;
        String subjectName = finalSubjectId == -1 ? "All Subjects" : subjectList.get(selectedSubjectIndex - 1).getName();

        String difficultyStr = diffAll ? "All" :
                (diffEasy ? "Easy " : "") +
                (diffMedium ? "Medium " : "") +
                (diffHard ? "Hard" : "");
        difficultyStr = difficultyStr.trim();
        final String finalDiffStr = difficultyStr;
        final String finalSubjectName = subjectName;

        btnStartTest.setEnabled(false);
        executorService.execute(() -> {
            FlashcardDatabase db = FlashcardDatabase.getDatabase(getApplicationContext());
            List<Flashcard> allCards;
            if (finalSubjectId == -1) {
                allCards = db.flashcardDao().getAllFlashcardsSync();
            } else {
                allCards = db.flashcardDao().getFlashcardsForSubjectSync(finalSubjectId);
            }
            
            List<Flashcard> filtered = new ArrayList<>();
            for (Flashcard c : allCards) {
                if (diffAll) {
                    filtered.add(c);
                } else {
                    String d = c.getDifficulty();
                    if (("EASY".equalsIgnoreCase(d) && diffEasy) || ("MEDIUM".equalsIgnoreCase(d) && diffMedium) || ("HARD".equalsIgnoreCase(d) && diffHard)) {
                        filtered.add(c);
                    }
                }
            }
            
            runOnUiThread(() -> {
                btnStartTest.setEnabled(true);
                if (filtered.isEmpty()) {
                    new AlertDialog.Builder(TestSetupActivity.this)
                        .setTitle("No cards match")
                        .setMessage("No cards match your selected options.")
                        .setPositiveButton("Change Settings", null)
                        .show();
                    return;
                }
                
                if (filtered.size() < requestedNumCards && requestedNumCards != Integer.MAX_VALUE) {
                    new AlertDialog.Builder(TestSetupActivity.this)
                        .setTitle("Not enough cards")
                        .setMessage("Only " + filtered.size() + " cards match your selected options.")
                        .setPositiveButton("Use " + filtered.size() + " Cards", (d, w) -> {
                            startTestActivity(filtered, filtered.size(), timeMillis, finalSubjectId, finalSubjectName, finalDiffStr);
                        })
                        .setNegativeButton("Change Settings", null)
                        .show();
                } else {
                    int actualNum = Math.min(filtered.size(), requestedNumCards);
                    startTestActivity(filtered, actualNum, timeMillis, finalSubjectId, finalSubjectName, finalDiffStr);
                }
            });
        });
    }

    private long parseTime(String ts) {
        if (ts.equals("Unlimited")) return -1;
        String num = ts.split(" ")[0];
        return Long.parseLong(num) * 60 * 1000;
    }

    private void startTestActivity(List<Flashcard> list, int num, long timeMillis, int subjectId, String subjectName, String difficulty) {
        // We need to pass the IDs to the next activity
        java.util.Collections.shuffle(list);

        int[] cardIds = new int[num];
        for(int i = 0; i < num; i++) {
            cardIds[i] = list.get(i).getId();
        }

        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra("CARD_IDS", cardIds);
        intent.putExtra("TIME_MILLIS", timeMillis);
        intent.putExtra("SUBJECT_ID", subjectId);
        intent.putExtra("SUBJECT_NAME", subjectName);
        intent.putExtra("DIFFICULTY", difficulty);

        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
