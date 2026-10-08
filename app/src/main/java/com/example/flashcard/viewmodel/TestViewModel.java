package com.example.flashcard.viewmodel;

import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.flashcard.data.Flashcard;
import com.example.flashcard.data.FlashcardDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestViewModel extends ViewModel {
    private final SavedStateHandle savedStateHandle;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Flashcard>> flashcardsLiveData = new MutableLiveData<>();
    public LiveData<List<Flashcard>> getFlashcards() { return flashcardsLiveData; }

    public TestViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;

        // Initialize default values if not present
        if (!savedStateHandle.contains("originalTimeMillis")) {
            savedStateHandle.set("originalTimeMillis", -1L);
            savedStateHandle.set("timerTargetRealtime", -1L);
            savedStateHandle.set("currentIndex", 0);
            savedStateHandle.set("correctCount", 0);
            savedStateHandle.set("incorrectCount", 0);
            savedStateHandle.set("isAnswerVisible", false);
            savedStateHandle.set("testFinished", false);
            savedStateHandle.set("resultsMap", new HashMap<Integer, Integer>());
        }
    }

    public void initData(long originalTimeMillis, int[] cardIds, int subjectId, String subjectName, String difficulty, FlashcardDatabase db) {
        if (!savedStateHandle.contains("isInitialized")) {
            savedStateHandle.set("originalTimeMillis", originalTimeMillis);
            savedStateHandle.set("subjectId", subjectId);
            savedStateHandle.set("subjectName", subjectName != null ? subjectName : "All Subjects");
            savedStateHandle.set("difficulty", difficulty != null ? difficulty : "All");
            savedStateHandle.set("cardIds", cardIds);
            savedStateHandle.set("timerTargetRealtime", originalTimeMillis != -1 ? SystemClock.elapsedRealtime() + originalTimeMillis : -1L);
            savedStateHandle.set("isInitialized", true);
            loadFlashcards(db, cardIds);
        } else {
            // Already initialized (like after rotation), but need to reload flashcards from DB optionally?
            // Wait, we can re-fetch because flashcards aren't saved in savedStateHandle.
            if (flashcardsLiveData.getValue() == null) {
                int[] savedCardIds = savedStateHandle.get("cardIds");
                if (savedCardIds != null) {
                    loadFlashcards(db, savedCardIds);
                }
            }
        }
    }

    private void loadFlashcards(FlashcardDatabase db, int[] ids) {
        executorService.execute(() -> {
            List<Flashcard> all = db.flashcardDao().getAllFlashcardsSync();
            HashMap<Integer, Flashcard> map = new HashMap<>();
            for (Flashcard f : all) {
                map.put(f.getId(), f);
            }

            final List<Flashcard> loadedList = new ArrayList<>();
            for (int id : ids) {
                if (map.containsKey(id)) {
                    loadedList.add(map.get(id));
                }
            }

            HashMap<Integer, Integer> resultsMap = savedStateHandle.get("resultsMap");
            if (resultsMap == null) resultsMap = new HashMap<>();

            for (int i = 0; i < loadedList.size(); i++) {
                if (!resultsMap.containsKey(i)) {
                    resultsMap.put(i, -1);
                }
            }
            savedStateHandle.set("resultsMap", resultsMap);

            flashcardsLiveData.postValue(loadedList);
        });
    }

    // Getters and Setters through SavedStateHandle
    public long getOriginalTimeMillis() {
        Long val = savedStateHandle.get("originalTimeMillis");
        return val != null ? val : -1L;
    }

    public long getTimerTargetRealtime() {
        Long val = savedStateHandle.get("timerTargetRealtime");
        return val != null ? val : -1L;
    }

    public void setTimerTargetRealtime(long target) {
        savedStateHandle.set("timerTargetRealtime", target);
    }

    public int getCurrentIndex() {
        Integer val = savedStateHandle.get("currentIndex");
        return val != null ? val : 0;
    }

    public void setCurrentIndex(int index) {
        savedStateHandle.set("currentIndex", index);
    }

    public int getCorrectCount() {
        Integer val = savedStateHandle.get("correctCount");
        return val != null ? val : 0;
    }

    public int getIncorrectCount() {
        Integer val = savedStateHandle.get("incorrectCount");
        return val != null ? val : 0;
    }

    public void incrementCorrectCount() {
        savedStateHandle.set("correctCount", getCorrectCount() + 1);
    }

    public void incrementIncorrectCount() {
        savedStateHandle.set("incorrectCount", getIncorrectCount() + 1);
    }

    public boolean isAnswerVisible() {
        Boolean val = savedStateHandle.get("isAnswerVisible");
        return val != null ? val : false;
    }

    public void setAnswerVisible(boolean visible) {
        savedStateHandle.set("isAnswerVisible", visible);
    }

    public boolean isTestFinished() {
        Boolean val = savedStateHandle.get("testFinished");
        return val != null ? val : false;
    }

    public void setTestFinished(boolean finished) {
        savedStateHandle.set("testFinished", finished);
    }

    public HashMap<Integer, Integer> getResultsMap() {
        return savedStateHandle.get("resultsMap");
    }

    public void updateResult(int index, int resultScore) {
        HashMap<Integer, Integer> map = savedStateHandle.get("resultsMap");
        if (map != null) {
            map.put(index, resultScore);
            savedStateHandle.set("resultsMap", map);
        }
    }

    public int getSubjectId() {
        Integer val = savedStateHandle.get("subjectId");
        return val != null ? val : -1;
    }

    public String getSubjectName() {
        String val = savedStateHandle.get("subjectName");
        return val != null ? val : "All Subjects";
    }

    public String getDifficulty() {
        String val = savedStateHandle.get("difficulty");
        return val != null ? val : "All";
    }

    public void adjustTimerTargetRealtime(long remainingTimeMillis) {
        long target = SystemClock.elapsedRealtime() + remainingTimeMillis;
        savedStateHandle.set("timerTargetRealtime", target);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
