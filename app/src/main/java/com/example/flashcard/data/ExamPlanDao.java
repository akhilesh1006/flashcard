package com.example.flashcard.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface ExamPlanDao {

    // --- ExamPlan ---
    @Insert
    long insertPlan(ExamPlan plan);

    @Update
    void updatePlan(ExamPlan plan);

    @Delete
    void deletePlan(ExamPlan plan);

    @Query("SELECT * FROM exam_plans WHERE archived = 0 ORDER BY examDate ASC")
    LiveData<List<ExamPlan>> getActivePlans();

    @Query("SELECT * FROM exam_plans WHERE archived = 0 AND completedAt = 0 ORDER BY examDate ASC LIMIT 1")
    ExamPlan getNearestActivePlanSync();

    @Query("SELECT * FROM exam_plans WHERE archived = 1 ORDER BY examDate DESC")
    LiveData<List<ExamPlan>> getArchivedPlans();

    @Query("SELECT * FROM exam_plans ORDER BY examDate ASC")
    LiveData<List<ExamPlan>> getAllPlans();

    @Query("SELECT * FROM exam_plans ORDER BY examDate ASC")
    List<ExamPlan> getAllPlansSync();

    @Query("SELECT * FROM exam_plans WHERE id = :planId LIMIT 1")
    ExamPlan getPlanSync(int planId);

    @Query("SELECT * FROM exam_plans WHERE id = :planId LIMIT 1")
    LiveData<ExamPlan> getPlan(int planId);


    // --- ExamPlanSubject ---
    @Insert
    void insertPlanSubjects(List<ExamPlanSubject> subjects);

    @Query("SELECT * FROM exam_plan_subjects WHERE planId = :planId")
    LiveData<List<ExamPlanSubject>> getSubjectsForPlan(int planId);

    @Query("SELECT * FROM exam_plan_subjects WHERE planId = :planId")
    List<ExamPlanSubject> getSubjectsForPlanSync(int planId);

    @Query("DELETE FROM exam_plan_subjects WHERE planId = :planId")
    void deleteSubjectsForPlan(int planId);


    // --- ExamPlanCard ---
    @Insert
    void insertPlanCards(List<ExamPlanCard> cards);

    @Query("SELECT * FROM exam_plan_cards WHERE planId = :planId")
    LiveData<List<ExamPlanCard>> getCardsForPlan(int planId);

    @Query("SELECT * FROM exam_plan_cards WHERE planId = :planId")
    List<ExamPlanCard> getCardsForPlanSync(int planId);

    @Query("SELECT COUNT(*) FROM exam_plan_cards WHERE planId = :planId")
    LiveData<Integer> getCardCountForPlan(int planId);

    @Query("SELECT COUNT(*) FROM exam_plan_cards WHERE planId = :planId")
    int getCardCountForPlanSync(int planId);

    @Query("SELECT COUNT(*) FROM exam_plan_cards WHERE planId = :planId AND covered = 1")
    LiveData<Integer> getCoveredCardCountForPlan(int planId);

    @Query("SELECT COUNT(*) FROM exam_plan_cards WHERE planId = :planId AND covered = 1")
    int getCoveredCardCountForPlanSync(int planId);

    @Query("SELECT * FROM exam_plan_cards WHERE planId = :planId AND flashcardId = :flashcardId LIMIT 1")
    ExamPlanCard getPlanCardSync(int planId, int flashcardId);

    @Update
    void updatePlanCard(ExamPlanCard card);


    // --- ExamPlanDay ---
    @Insert
    void insertPlanDay(ExamPlanDay day);

    @Update
    void updatePlanDay(ExamPlanDay day);

    @Query("SELECT * FROM exam_plan_days WHERE planId = :planId ORDER BY date ASC")
    LiveData<List<ExamPlanDay>> getDaysForPlan(int planId);

    @Query("SELECT * FROM exam_plan_days WHERE planId = :planId ORDER BY date ASC")
    List<ExamPlanDay> getDaysForPlanSync(int planId);

    @Query("SELECT * FROM exam_plan_days WHERE planId = :planId AND date = :date LIMIT 1")
    ExamPlanDay getPlanDaySync(int planId, String date);

    @Query("SELECT f.* FROM flashcard_table f INNER JOIN exam_plan_cards epc ON f.id = epc.flashcardId WHERE epc.planId = :planId AND epc.covered = 0")
    LiveData<List<Flashcard>> getUncoveredFlashcardsForPlan(int planId);
}
