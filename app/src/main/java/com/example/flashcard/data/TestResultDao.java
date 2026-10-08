package com.example.flashcard.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TestResultDao {
    @Insert
    long insertTestResult(TestResult testResult);

    @Insert
    void insertTestResultItems(List<TestResultItem> items);

    @Query("SELECT * FROM test_results WHERE isPermanentlySaved = 1 ORDER BY completedAt DESC")
    LiveData<List<TestResult>> getPermanentlySavedResults();

    @Query("SELECT * FROM test_results WHERE isPermanentlySaved = 0 ORDER BY completedAt DESC LIMIT 5")
    LiveData<List<TestResult>> getLatest5Results();

    @Query("DELETE FROM test_results WHERE isPermanentlySaved = 0 AND id NOT IN (SELECT id FROM test_results WHERE isPermanentlySaved = 0 ORDER BY completedAt DESC LIMIT 5)")
    void deleteOldNonPermanentResults();

    @Query("UPDATE test_results SET isPermanentlySaved = 1 WHERE id = :id")
    void markPermanentlySaved(long id);

    @Query("DELETE FROM test_results WHERE id = :id")
    void deleteTestResult(long id);

    @Query("SELECT * FROM test_results WHERE id = :id")
    TestResult getTestResultById(long id);

    @Query("SELECT * FROM test_results WHERE completedAt = :timestamp LIMIT 1")
    TestResult getTestResultByTimestamp(long timestamp);

    @Query("SELECT * FROM test_result_items WHERE testResultId = :testResultId")
    LiveData<List<TestResultItem>> getResultItemsForTest(long testResultId);

    @Query("SELECT * FROM test_result_items WHERE testResultId = :testResultId")
    List<TestResultItem> getResultItemsForTestSync(long testResultId);
}
