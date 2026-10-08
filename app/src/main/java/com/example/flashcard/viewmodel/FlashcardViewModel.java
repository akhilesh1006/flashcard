package com.example.flashcard.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.flashcard.data.Flashcard;
import com.example.flashcard.data.FlashcardRepository;
import com.example.flashcard.data.FlashcardWithSubjectName;
import com.example.flashcard.data.Subject;
import com.example.flashcard.data.SubjectWithCount;
import com.example.flashcard.data.StudyDay;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import java.util.List;

public class FlashcardViewModel extends AndroidViewModel {
    private FlashcardRepository repository;
    private LiveData<List<Flashcard>> allFlashcards;
    private LiveData<List<Subject>> allSubjects;
        private LiveData<List<SubjectWithCount>> allSubjectsWithCount;
    private LiveData<List<FlashcardWithSubjectName>> allFlashcardsWithSubjectName;

    public FlashcardViewModel(Application application) {
        super(application);
        repository = new FlashcardRepository(application);
        allFlashcards = repository.getAllFlashcards();
        allSubjects = repository.getAllSubjects();
        allSubjectsWithCount = repository.getAllSubjectsWithCount();
        allFlashcardsWithSubjectName = repository.getAllFlashcardsWithSubjectName();
    }

    public LiveData<List<Flashcard>> getAllFlashcards() {
        return allFlashcards;
    }

        public LiveData<List<SubjectWithCount>> getAllSubjectsWithCount() {
        return allSubjectsWithCount;
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public LiveData<List<FlashcardWithSubjectName>> getAllFlashcardsWithSubjectName() {
        return allFlashcardsWithSubjectName;
    }

    public LiveData<List<Flashcard>> getFlashcardsForSubject(int subjectId) {
        return repository.getFlashcardsForSubject(subjectId);
    }

    public LiveData<List<Flashcard>> getCardsDue(long currentTimeInMillis) {
        return repository.getCardsDue(currentTimeInMillis);
    }

    public LiveData<Integer> getCardsDueCount(long currentTimeInMillis) {
        return repository.getCardsDueCount(currentTimeInMillis);
    }

    public LiveData<Integer> getLearningCardsCount() {
        return repository.getLearningCardsCount();
    }

    public LiveData<Integer> getYoungCardsCount() {
        return repository.getYoungCardsCount();
    }

    public LiveData<Integer> getMatureCardsCount() {
        return repository.getMatureCardsCount();
    }

    public LiveData<List<FlashcardWithSubjectName>> getCardsDueWithSubjectName(long currentTimeInMillis) {
        return repository.getCardsDueWithSubjectName(currentTimeInMillis);
    }

    public LiveData<Integer> getFlashcardCountForSubject(int subjectId) {
        return repository.getFlashcardCountForSubject(subjectId);
    }

    public void insert(Flashcard flashcard) {
        repository.insert(flashcard);
    }

    public void insertMultiple(List<Flashcard> flashcards) {
        repository.insertMultiple(flashcards);
    }

    public void update(Flashcard flashcard) {
        repository.update(flashcard);
    }

    public void delete(Flashcard flashcard) {
        repository.delete(flashcard);
    }

        public interface InsertSubjectCallback {
        void onInserted(long subjectId);
    }

    public void insertSubjectWithCallback(Subject subject, androidx.lifecycle.LifecycleOwner owner, InsertSubjectCallback callback) {
        repository.insertSubjectWithCallback(subject, callback);
    }

    public void insertSubject(Subject subject) {
        repository.insertSubject(subject);
    }

    public void deleteSubject(Subject subject) {
        repository.deleteSubject(subject);
    }

    public void updateSubject(Subject subject) {
        repository.updateSubject(subject);
    }

    public void deleteAllFlashcards() {
        repository.deleteAllFlashcards();
    }

    public LiveData<List<com.example.flashcard.data.TestResult>> getLatest5Results() {
        return repository.getLatest5Results();
    }

    public LiveData<List<com.example.flashcard.data.TestResult>> getPermanentlySavedResults() {
        return repository.getPermanentlySavedResults();
    }

    public LiveData<List<com.example.flashcard.data.TestResultItem>> getResultItemsForTest(long testResultId) {
        return repository.getResultItemsForTest(testResultId);
    }

    public interface InsertCallback {
        void onInserted(long resultId);
    }

    public void insertTestResult(com.example.flashcard.data.TestResult testResult, List<com.example.flashcard.data.TestResultItem> items, InsertCallback callback) {
        repository.insertTestResult(testResult, items, callback);
    }

    public void markPermanentlySavedByTimestamp(long timestamp) {
        repository.markPermanentlySavedByTimestamp(timestamp);
    }

    public void markPermanentlySaved(long testResultId) {
        repository.markPermanentlySaved(testResultId);
    }

    public void deleteTestResult(long testResultId) {
        repository.deleteTestResult(testResultId);
    }

    public LiveData<List<StudyDay>> getAllStudyDays() {
        return repository.getAllStudyDays();
    }

    public LiveData<StudyDay> getStudyDayByDate(String date) {
        return repository.getStudyDayByDate(date);
    }

    public void incrementCardsStudiedToday(int dailyGoal) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        repository.incrementCardsStudiedToday(today, dailyGoal);
    }

    public void incrementTestsCompletedToday(int dailyGoal) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        repository.incrementTestsCompletedToday(today, dailyGoal);
    }

    public void updateDailyGoalForToday(int dailyGoal) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        repository.updateDailyGoalForToday(today, dailyGoal);
    }

    public void clearAllData(Runnable onComplete) {
        repository.clearAllData(onComplete);
    }

    public void restoreDatabase(String jsonString, Runnable onComplete) {
        repository.restoreDatabase(jsonString, onComplete);
    }

    public void generateJsonBackup(java.util.function.Consumer<String> callback) {
        new Thread(() -> {
            String json = repository.generateJsonBackupSync();
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                callback.accept(json);
            });
        }).start();
    }

    public void generateCsvBackup(java.util.function.Consumer<String> callback) {
        new Thread(() -> {
            String csv = repository.generateCsvBackupSync();
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                callback.accept(csv);
            });
        }).start();
    }

    // --- Exam Plan methods ---

    public LiveData<List<com.example.flashcard.data.ExamPlan>> getActiveExamPlans() {
        return repository.getActiveExamPlans();
    }

    public LiveData<List<com.example.flashcard.data.ExamPlan>> getArchivedExamPlans() {
        return repository.getArchivedExamPlans();
    }

    public LiveData<List<com.example.flashcard.data.ExamPlan>> getAllExamPlans() {
        return repository.getAllExamPlans();
    }

    public LiveData<com.example.flashcard.data.ExamPlan> getExamPlan(int planId) {
        return repository.getExamPlan(planId);
    }

    public LiveData<List<com.example.flashcard.data.ExamPlanSubject>> getSubjectsForPlan(int planId) {
        return repository.getSubjectsForPlan(planId);
    }

    public LiveData<List<com.example.flashcard.data.ExamPlanCard>> getCardsForPlan(int planId) {
        return repository.getCardsForPlan(planId);
    }

    public LiveData<Integer> getCardCountForPlan(int planId) {
        return repository.getCardCountForPlan(planId);
    }

    public LiveData<Integer> getCoveredCardCountForPlan(int planId) {
        return repository.getCoveredCardCountForPlan(planId);
    }

    public LiveData<List<com.example.flashcard.data.ExamPlanDay>> getDaysForPlan(int planId) {
        return repository.getDaysForPlan(planId);
    }

    public LiveData<List<Flashcard>> getUncoveredFlashcardsForPlan(int planId) {
        return repository.getUncoveredFlashcardsForPlan(planId);
    }

    public void createExamPlan(com.example.flashcard.data.ExamPlan plan, List<Integer> subjectIds, FlashcardRepository.InsertPlanCallback callback) {
        repository.createExamPlan(plan, subjectIds, callback);
    }

    public void updateExamPlan(com.example.flashcard.data.ExamPlan plan, List<Integer> selectedSubjectIds, Runnable onComplete) {
        repository.updateExamPlan(plan, selectedSubjectIds, onComplete);
    }

    public void setExamPlanArchived(int planId, boolean archived, Runnable onComplete) {
        repository.setExamPlanArchived(planId, archived, onComplete);
    }

    public void markExamPlanCompleted(int planId, boolean completed, Runnable onComplete) {
        repository.markExamPlanCompleted(planId, completed, onComplete);
    }

    public void deleteExamPlan(int planId, Runnable onComplete) {
        repository.deleteExamPlan(planId, onComplete);
    }

    public void recordExamPlanStudyCompletion(int planId, int flashcardId, int recommendedTarget) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        repository.recordExamPlanStudyCompletion(planId, flashcardId, today, recommendedTarget);
    }
}
