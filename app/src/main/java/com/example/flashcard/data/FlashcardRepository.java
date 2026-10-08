package com.example.flashcard.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.ArrayList;

public class FlashcardRepository {
    private FlashcardDao flashcardDao;
    private SubjectDao subjectDao;
    private TestResultDao testResultDao;
    private StudyDayDao studyDayDao;
    private ExamPlanDao examPlanDao;
    private FlashcardDatabase db;
    private LiveData<List<Flashcard>> allFlashcards;
    private LiveData<List<Subject>> allSubjects;
    private LiveData<List<SubjectWithCount>> allSubjectsWithCount;
    private LiveData<List<FlashcardWithSubjectName>> allFlashcardsWithSubjectName;
    private LiveData<List<StudyDay>> allStudyDays;

    public FlashcardRepository(Application application) {
        db = FlashcardDatabase.getDatabase(application);
        flashcardDao = db.flashcardDao();
        subjectDao = db.subjectDao();
        testResultDao = db.testResultDao();
        studyDayDao = db.studyDayDao();
        examPlanDao = db.examPlanDao();

        allFlashcards = flashcardDao.getAllFlashcards();
        allSubjects = subjectDao.getAllSubjects();
        allSubjectsWithCount = subjectDao.getAllSubjectsWithCount();
        allFlashcardsWithSubjectName = flashcardDao.getAllFlashcardsWithSubjectName();
        allStudyDays = studyDayDao.getAllStudyDays();
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
        return flashcardDao.getFlashcardsForSubject(subjectId);
    }

    public LiveData<List<Flashcard>> getCardsDue(long currentTimeInMillis) {
        return flashcardDao.getCardsDue(currentTimeInMillis);
    }

    public LiveData<Integer> getCardsDueCount(long currentTimeInMillis) {
        return flashcardDao.getCardsDueCount(currentTimeInMillis);
    }

    public LiveData<Integer> getLearningCardsCount() {
        return flashcardDao.getLearningCardsCount();
    }

    public LiveData<Integer> getYoungCardsCount() {
        return flashcardDao.getYoungCardsCount();
    }

    public LiveData<Integer> getMatureCardsCount() {
        return flashcardDao.getMatureCardsCount();
    }

    public LiveData<List<FlashcardWithSubjectName>> getCardsDueWithSubjectName(long currentTimeInMillis) {
        return flashcardDao.getCardsDueWithSubjectName(currentTimeInMillis);
    }

    public LiveData<Integer> getFlashcardCountForSubject(int subjectId) {
        return subjectDao.getFlashcardCountForSubject(subjectId);
    }

    public void insert(Flashcard flashcard) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            flashcardDao.insert(flashcard);
        });
    }

    public void insertMultiple(List<Flashcard> flashcards) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            flashcardDao.insertMultiple(flashcards);
        });
    }

    public void update(Flashcard flashcard) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            flashcardDao.update(flashcard);
        });
    }

    public void delete(Flashcard flashcard) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            flashcardDao.delete(flashcard);
        });
    }

    public void insertSubjectWithCallback(Subject subject, com.example.flashcard.viewmodel.FlashcardViewModel.InsertSubjectCallback callback) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long id = subjectDao.insert(subject);
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onInserted(id);
                    });
                }
            } catch (Exception e) {
                // Ignore duplicates
            }
        });
    }

    public void insertSubject(Subject subject) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            try {
                subjectDao.insert(subject);
            } catch (Exception e) {
                // Ignore duplicates
            }
        });
    }

    public void deleteSubject(Subject subject) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            subjectDao.delete(subject);
        });
    }

    public void updateSubject(Subject subject) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            subjectDao.update(subject);
        });
    }

    public void deleteAllFlashcards() {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            flashcardDao.deleteAllFlashcards();
        });
    }

    public LiveData<List<TestResult>> getLatest5Results() {
        return testResultDao.getLatest5Results();
    }

    public LiveData<List<TestResult>> getPermanentlySavedResults() {
        return testResultDao.getPermanentlySavedResults();
    }

    public LiveData<List<TestResultItem>> getResultItemsForTest(long testResultId) {
        return testResultDao.getResultItemsForTest(testResultId);
    }

    public void insertTestResult(TestResult testResult, List<TestResultItem> items, com.example.flashcard.viewmodel.FlashcardViewModel.InsertCallback callback) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            long[] resultId = new long[1];
            db.runInTransaction(() -> {
                resultId[0] = testResultDao.insertTestResult(testResult);
                for (TestResultItem item : items) {
                    item.testResultId = resultId[0];
                }
                testResultDao.insertTestResultItems(items);
                testResultDao.deleteOldNonPermanentResults();
            });
            if (callback != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onInserted(resultId[0]);
                });
            }
        });
    }

    public void markPermanentlySavedByTimestamp(long timestamp) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            TestResult tr = testResultDao.getTestResultByTimestamp(timestamp);
            if (tr != null) {
                testResultDao.markPermanentlySaved(tr.id);
            }
        });
    }

    public void markPermanentlySaved(long testResultId) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            testResultDao.markPermanentlySaved(testResultId);
        });
    }

    public void deleteTestResult(long testResultId) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            testResultDao.deleteTestResult(testResultId);
            testResultDao.deleteOldNonPermanentResults();
        });
    }

    public LiveData<List<StudyDay>> getAllStudyDays() {
        return allStudyDays;
    }

    public LiveData<StudyDay> getStudyDayByDate(String date) {
        return studyDayDao.getStudyDayByDate(date);
    }

    public void incrementCardsStudiedToday(String date, int dailyGoal) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            StudyDay today = studyDayDao.getStudyDayByDateSync(date);
            if (today == null) {
                today = new StudyDay(date, 1, 0, 1 >= dailyGoal, dailyGoal);
                studyDayDao.insert(today);
            } else {
                today.cardsStudied += 1;
                if (!today.goalCompleted && today.cardsStudied >= dailyGoal) {
                    today.goalCompleted = true;
                }
                today.dailyGoalAtTheTime = dailyGoal; // Update to latest in case it changed today
                studyDayDao.update(today);
            }
        });
    }

    public void incrementTestsCompletedToday(String date, int dailyGoal) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            StudyDay today = studyDayDao.getStudyDayByDateSync(date);
            if (today == null) {
                today = new StudyDay(date, 0, 1, false, dailyGoal);
                studyDayDao.insert(today);
            } else {
                today.testsCompleted += 1;
                studyDayDao.update(today);
            }
        });
    }

    public void updateDailyGoalForToday(String date, int dailyGoal) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            StudyDay today = studyDayDao.getStudyDayByDateSync(date);
            if (today != null) {
                today.dailyGoalAtTheTime = dailyGoal;
                today.goalCompleted = today.cardsStudied >= dailyGoal;
                studyDayDao.update(today);
            }
        });
    }

    // --- Exam Plan methods ---

    public LiveData<List<ExamPlan>> getActiveExamPlans() {
        return examPlanDao.getActivePlans();
    }

    public LiveData<List<ExamPlan>> getArchivedExamPlans() {
        return examPlanDao.getArchivedPlans();
    }

    public LiveData<List<ExamPlan>> getAllExamPlans() {
        return examPlanDao.getAllPlans();
    }

    public LiveData<ExamPlan> getExamPlan(int planId) {
        return examPlanDao.getPlan(planId);
    }

    public LiveData<List<ExamPlanSubject>> getSubjectsForPlan(int planId) {
        return examPlanDao.getSubjectsForPlan(planId);
    }

    public LiveData<List<ExamPlanCard>> getCardsForPlan(int planId) {
        return examPlanDao.getCardsForPlan(planId);
    }

    public LiveData<Integer> getCardCountForPlan(int planId) {
        return examPlanDao.getCardCountForPlan(planId);
    }

    public LiveData<Integer> getCoveredCardCountForPlan(int planId) {
        return examPlanDao.getCoveredCardCountForPlan(planId);
    }

    public LiveData<List<ExamPlanDay>> getDaysForPlan(int planId) {
        return examPlanDao.getDaysForPlan(planId);
    }

    public LiveData<List<Flashcard>> getUncoveredFlashcardsForPlan(int planId) {
        return examPlanDao.getUncoveredFlashcardsForPlan(planId);
    }

    public interface InsertPlanCallback {
        void onInserted(long planId);
    }

    public void createExamPlan(ExamPlan plan, List<Integer> subjectIds, InsertPlanCallback callback) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            long planId = db.runInTransaction(() -> {
                long id = examPlanDao.insertPlan(plan);
                List<ExamPlanSubject> planSubjects = new ArrayList<>();
                List<ExamPlanCard> planCards = new ArrayList<>();

                for (int subjectId : subjectIds) {
                    Subject subject = subjectDao.getSubjectByIdSync(subjectId);
                    String subjectName = subject != null ? subject.getName() : "Unknown";
                    planSubjects.add(new ExamPlanSubject((int) id, subjectId, subjectName));

                    List<Flashcard> cards = flashcardDao.getFlashcardsForSubjectSync(subjectId);
                    for (Flashcard card : cards) {
                        planCards.add(new ExamPlanCard((int) id, card.getId(), card.getQuestion(), subjectName));
                    }
                }

                if (!planSubjects.isEmpty()) {
                    examPlanDao.insertPlanSubjects(planSubjects);
                }
                if (!planCards.isEmpty()) {
                    examPlanDao.insertPlanCards(planCards);
                }

                return id;
            });

            if (callback != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onInserted(planId);
                });
            }
        });
    }

    public void updateExamPlan(ExamPlan plan, List<Integer> selectedSubjectIds, Runnable onComplete) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            db.runInTransaction(() -> {
                examPlanDao.updatePlan(plan);

                // Update subjects
                List<ExamPlanSubject> existingSubjects = examPlanDao.getSubjectsForPlanSync(plan.id);
                List<Integer> existingSubjectIds = new ArrayList<>();
                for (ExamPlanSubject s : existingSubjects) {
                    existingSubjectIds.add(s.subjectId);
                }

                // Identify newly added subjects to add their cards
                List<ExamPlanSubject> newPlanSubjects = new ArrayList<>();
                List<ExamPlanCard> newPlanCards = new ArrayList<>();

                for (int subjectId : selectedSubjectIds) {
                    Subject subject = subjectDao.getSubjectByIdSync(subjectId);
                    String subjectName = subject != null ? subject.getName() : "Unknown";
                    newPlanSubjects.add(new ExamPlanSubject(plan.id, subjectId, subjectName));

                    if (!existingSubjectIds.contains(subjectId)) {
                        // Append new cards snapshot
                        List<Flashcard> cards = flashcardDao.getFlashcardsForSubjectSync(subjectId);
                        for (Flashcard card : cards) {
                            newPlanCards.add(new ExamPlanCard(plan.id, card.getId(), card.getQuestion(), subjectName));
                        }
                    }
                }

                examPlanDao.deleteSubjectsForPlan(plan.id);
                if (!newPlanSubjects.isEmpty()) {
                    examPlanDao.insertPlanSubjects(newPlanSubjects);
                }
                if (!newPlanCards.isEmpty()) {
                    examPlanDao.insertPlanCards(newPlanCards);
                }
            });

            if (onComplete != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    public void setExamPlanArchived(int planId, boolean archived, Runnable onComplete) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            ExamPlan plan = examPlanDao.getPlanSync(planId);
            if (plan != null) {
                plan.archived = archived;
                examPlanDao.updatePlan(plan);
            }
            if (onComplete != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    public void markExamPlanCompleted(int planId, boolean completed, Runnable onComplete) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            ExamPlan plan = examPlanDao.getPlanSync(planId);
            if (plan != null) {
                plan.completedAt = completed ? System.currentTimeMillis() : 0;
                examPlanDao.updatePlan(plan);
            }
            if (onComplete != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    public void deleteExamPlan(int planId, Runnable onComplete) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            ExamPlan plan = examPlanDao.getPlanSync(planId);
            if (plan != null) {
                examPlanDao.deletePlan(plan);
            }
            if (onComplete != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    public void recordExamPlanStudyCompletion(int planId, int flashcardId, String date, int recommendedTarget) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            db.runInTransaction(() -> {
                ExamPlanCard card = examPlanDao.getPlanCardSync(planId, flashcardId);
                boolean isNewCoverage = false;
                if (card != null && !card.covered) {
                    card.covered = true;
                    card.firstCoveredAt = System.currentTimeMillis();
                    examPlanDao.updatePlanCard(card);
                    isNewCoverage = true;
                }

                ExamPlanDay day = examPlanDao.getPlanDaySync(planId, date);
                if (day == null) {
                    day = new ExamPlanDay(planId, date, recommendedTarget);
                    day.reviewsCompletedThatDay = 1;
                    day.uniqueCardsCoveredThatDay = isNewCoverage ? 1 : 0;
                    examPlanDao.insertPlanDay(day);
                } else {
                    day.reviewsCompletedThatDay += 1;
                    if (isNewCoverage) {
                        day.uniqueCardsCoveredThatDay += 1;
                    }
                    day.recommendedTargetAtTheTime = recommendedTarget;
                    examPlanDao.updatePlanDay(day);
                }
            });
        });
    }

    public void clearAllData(Runnable onComplete) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            db.clearAllTables();
            if (onComplete != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    public void restoreDatabase(String jsonString, Runnable onComplete) {
        FlashcardDatabase.databaseWriteExecutor.execute(() -> {
            try {
                org.json.JSONObject obj = new org.json.JSONObject(jsonString);
                db.clearAllTables();

                org.json.JSONArray subjectsArr = obj.optJSONArray("subjects");
                if(subjectsArr != null) {
                    for(int i=0; i<subjectsArr.length(); i++) {
                        org.json.JSONObject sObj = subjectsArr.getJSONObject(i);
                        Subject s = new Subject("temp", "MEDIUM");
                        s.setId(sObj.getInt("id"));
                        s.setName(sObj.getString("name"));
                        s.setDifficulty(sObj.optString("difficulty", "MEDIUM"));
                        subjectDao.insert(s);
                    }
                }

                org.json.JSONArray flashcardsArr = obj.optJSONArray("flashcards");
                if(flashcardsArr != null) {
                    for(int i=0; i<flashcardsArr.length(); i++) {
                        org.json.JSONObject fObj = flashcardsArr.getJSONObject(i);
                        Flashcard f = new Flashcard(fObj.getString("question"), fObj.getString("answer"), fObj.getInt("subjectId"));
                        f.setId(fObj.getInt("id"));
                        f.setComment(fObj.optString("comment", ""));
                        f.setDifficulty(fObj.optString("difficulty", "MEDIUM"));
                        f.setRepetition(fObj.optInt("repetition", 0));
                        f.setInterval(fObj.optInt("interval", 0));
                        f.setEasinessFactor((float) fObj.optDouble("easinessFactor", 2.5));
                        f.setNextReviewDate(fObj.optLong("nextReviewDate", 0));
                        flashcardDao.insert(f);
                    }
                }

                org.json.JSONArray examPlansArr = obj.optJSONArray("exam_plans");
                if (examPlansArr != null) {
                    for (int i = 0; i < examPlansArr.length(); i++) {
                        org.json.JSONObject pObj = examPlansArr.getJSONObject(i);
                        ExamPlan p = new ExamPlan(
                                pObj.getString("name"),
                                pObj.getString("startDate"),
                                pObj.getString("examDate"),
                                pObj.getLong("createdAt")
                        );
                        p.id = pObj.getInt("id");
                        p.archived = pObj.optBoolean("archived", false);
                        p.completedAt = pObj.optLong("completedAt", 0);
                        examPlanDao.insertPlan(p);
                    }
                }

                org.json.JSONArray examPlanSubjectsArr = obj.optJSONArray("exam_plan_subjects");
                if (examPlanSubjectsArr != null) {
                    List<ExamPlanSubject> subjectsList = new ArrayList<>();
                    for (int i = 0; i < examPlanSubjectsArr.length(); i++) {
                        org.json.JSONObject psObj = examPlanSubjectsArr.getJSONObject(i);
                        ExamPlanSubject ps = new ExamPlanSubject(
                                psObj.getInt("planId"),
                                psObj.getInt("subjectId"),
                                psObj.getString("subjectNameSnapshot")
                        );
                        subjectsList.add(ps);
                    }
                    if (!subjectsList.isEmpty()) {
                        examPlanDao.insertPlanSubjects(subjectsList);
                    }
                }

                org.json.JSONArray examPlanCardsArr = obj.optJSONArray("exam_plan_cards");
                if (examPlanCardsArr != null) {
                    List<ExamPlanCard> cardsList = new ArrayList<>();
                    for (int i = 0; i < examPlanCardsArr.length(); i++) {
                        org.json.JSONObject pcObj = examPlanCardsArr.getJSONObject(i);
                        ExamPlanCard pc = new ExamPlanCard(
                                pcObj.getInt("planId"),
                                pcObj.getInt("flashcardId"),
                                pcObj.getString("questionSnapshot"),
                                pcObj.getString("subjectNameSnapshot")
                        );
                        pc.covered = pcObj.optBoolean("covered", false);
                        pc.firstCoveredAt = pcObj.optLong("firstCoveredAt", 0);
                        cardsList.add(pc);
                    }
                    if (!cardsList.isEmpty()) {
                        examPlanDao.insertPlanCards(cardsList);
                    }
                }

                org.json.JSONArray examPlanDaysArr = obj.optJSONArray("exam_plan_days");
                if (examPlanDaysArr != null) {
                    for (int i = 0; i < examPlanDaysArr.length(); i++) {
                        org.json.JSONObject pdObj = examPlanDaysArr.getJSONObject(i);
                        ExamPlanDay pd = new ExamPlanDay(
                                pdObj.getInt("planId"),
                                pdObj.getString("date"),
                                pdObj.optInt("recommendedTargetAtTheTime", 0)
                        );
                        pd.uniqueCardsCoveredThatDay = pdObj.optInt("uniqueCardsCoveredThatDay", 0);
                        pd.reviewsCompletedThatDay = pdObj.optInt("reviewsCompletedThatDay", 0);
                        examPlanDao.insertPlanDay(pd);
                    }
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
            if (onComplete != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    public String generateJsonBackupSync() {
        try {
            org.json.JSONObject obj = new org.json.JSONObject();
            org.json.JSONArray subjectsArr = new org.json.JSONArray();
            List<Subject> subjects = subjectDao.getAllSubjectsSync();
            for(Subject s : subjects) {
                org.json.JSONObject sObj = new org.json.JSONObject();
                sObj.put("id", s.getId());
                sObj.put("name", s.getName());
                sObj.put("difficulty", s.getDifficulty());
                subjectsArr.put(sObj);
            }
            obj.put("subjects", subjectsArr);

            org.json.JSONArray flashcardsArr = new org.json.JSONArray();
            List<Flashcard> flashcards = flashcardDao.getAllFlashcardsSync();
            for(Flashcard f : flashcards) {
                org.json.JSONObject fObj = new org.json.JSONObject();
                fObj.put("id", f.getId());
                fObj.put("subjectId", f.getSubjectId());
                fObj.put("question", f.getQuestion());
                fObj.put("answer", f.getAnswer());
                fObj.put("comment", f.getComment());
                fObj.put("difficulty", f.getDifficulty());
                fObj.put("repetition", f.getRepetition());
                fObj.put("interval", f.getInterval());
                fObj.put("easinessFactor", f.getEasinessFactor());
                fObj.put("nextReviewDate", f.getNextReviewDate());
                flashcardsArr.put(fObj);
            }
            obj.put("flashcards", flashcardsArr);

            // Export Exam Plans
            org.json.JSONArray examPlansArr = new org.json.JSONArray();
            org.json.JSONArray examPlanSubjectsArr = new org.json.JSONArray();
            org.json.JSONArray examPlanCardsArr = new org.json.JSONArray();
            org.json.JSONArray examPlanDaysArr = new org.json.JSONArray();

            List<ExamPlan> plans = examPlanDao.getAllPlansSync();
            for (ExamPlan p : plans) {
                org.json.JSONObject pObj = new org.json.JSONObject();
                pObj.put("id", p.id);
                pObj.put("name", p.name);
                pObj.put("startDate", p.startDate);
                pObj.put("examDate", p.examDate);
                pObj.put("createdAt", p.createdAt);
                pObj.put("archived", p.archived);
                pObj.put("completedAt", p.completedAt);
                examPlansArr.put(pObj);

                List<ExamPlanSubject> pSubjects = examPlanDao.getSubjectsForPlanSync(p.id);
                for (ExamPlanSubject ps : pSubjects) {
                    org.json.JSONObject psObj = new org.json.JSONObject();
                    psObj.put("planId", ps.planId);
                    psObj.put("subjectId", ps.subjectId);
                    psObj.put("subjectNameSnapshot", ps.subjectNameSnapshot);
                    examPlanSubjectsArr.put(psObj);
                }

                List<ExamPlanCard> pCards = examPlanDao.getCardsForPlanSync(p.id);
                for (ExamPlanCard pc : pCards) {
                    org.json.JSONObject pcObj = new org.json.JSONObject();
                    pcObj.put("planId", pc.planId);
                    pcObj.put("flashcardId", pc.flashcardId);
                    pcObj.put("questionSnapshot", pc.questionSnapshot);
                    pcObj.put("subjectNameSnapshot", pc.subjectNameSnapshot);
                    pcObj.put("covered", pc.covered);
                    pcObj.put("firstCoveredAt", pc.firstCoveredAt);
                    examPlanCardsArr.put(pcObj);
                }

                List<ExamPlanDay> pDays = examPlanDao.getDaysForPlanSync(p.id);
                for (ExamPlanDay pd : pDays) {
                    org.json.JSONObject pdObj = new org.json.JSONObject();
                    pdObj.put("planId", pd.planId);
                    pdObj.put("date", pd.date);
                    pdObj.put("recommendedTargetAtTheTime", pd.recommendedTargetAtTheTime);
                    pdObj.put("uniqueCardsCoveredThatDay", pd.uniqueCardsCoveredThatDay);
                    pdObj.put("reviewsCompletedThatDay", pd.reviewsCompletedThatDay);
                    examPlanDaysArr.put(pdObj);
                }
            }

            obj.put("exam_plans", examPlansArr);
            obj.put("exam_plan_subjects", examPlanSubjectsArr);
            obj.put("exam_plan_cards", examPlanCardsArr);
            obj.put("exam_plan_days", examPlanDaysArr);

            return obj.toString(4);
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String generateCsvBackupSync() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("id,subjectId,question,answer,difficulty,comment,repetition,interval,easinessFactor,nextReviewDate\n");
            List<Flashcard> flashcards = flashcardDao.getAllFlashcardsSync();
            for(Flashcard f : flashcards) {
                sb.append(f.getId()).append(",");
                sb.append(f.getSubjectId()).append(",");
                sb.append("\"").append(f.getQuestion().replace("\"", "\"\"")).append("\",");
                sb.append("\"").append(f.getAnswer().replace("\"", "\"\"")).append("\",");
                sb.append("\"").append(f.getDifficulty()).append("\",");
                sb.append("\"").append(f.getComment().replace("\"", "\"\"")).append("\",");
                sb.append(f.getRepetition()).append(",");
                sb.append(f.getInterval()).append(",");
                sb.append(f.getEasinessFactor()).append(",");
                sb.append(f.getNextReviewDate()).append("\n");
            }
            return sb.toString();
        } catch(Exception e) {
             e.printStackTrace();
             return null;
        }
    }
}
