package com.example.flashcard.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StudyDayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StudyDay studyDay);

    @Update
    void update(StudyDay studyDay);

    @Query("SELECT * FROM study_days WHERE date = :date LIMIT 1")
    LiveData<StudyDay> getStudyDayByDate(String date);

    @Query("SELECT * FROM study_days WHERE date = :date LIMIT 1")
    StudyDay getStudyDayByDateSync(String date);

    @Query("SELECT * FROM study_days ORDER BY date DESC")
    LiveData<List<StudyDay>> getAllStudyDays();

    @Query("SELECT * FROM study_days ORDER BY date DESC")
    List<StudyDay> getAllStudyDaysSync();

    @Query("SELECT * FROM study_days WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    LiveData<List<StudyDay>> getStudyDaysBetween(String startDate, String endDate);

    @Query("SELECT * FROM study_days WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    List<StudyDay> getStudyDaysBetweenSync(String startDate, String endDate);

    @Query("SELECT * FROM study_days WHERE date >= :startDate ORDER BY date ASC")
    LiveData<List<StudyDay>> getStudyDaysSince(String startDate);

    @Query("SELECT COUNT(*) FROM study_days WHERE goalCompleted = 1")
    LiveData<Integer> getTotalGoalsCompleted();

    @Query("SELECT SUM(cardsStudied) FROM study_days")
    LiveData<Integer> getTotalCardsStudied();

    @Query("SELECT SUM(testsCompleted) FROM study_days")
    LiveData<Integer> getTotalTestsCompleted();

    @Query("DELETE FROM study_days")
    void deleteAll();
}
