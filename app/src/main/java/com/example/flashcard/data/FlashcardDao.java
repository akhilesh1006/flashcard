package com.example.flashcard.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlashcardDao {
    @Insert
    void insert(Flashcard flashcard);

    @Insert
    void insertMultiple(List<Flashcard> flashcards);

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);

    @Query("SELECT * FROM flashcard_table ORDER BY id ASC")
    LiveData<List<Flashcard>> getAllFlashcards();

    @Query("SELECT * FROM flashcard_table")
    List<Flashcard> getAllFlashcardsSync();

    @Query("SELECT * FROM flashcard_table WHERE subjectId = :subjectId")
    List<Flashcard> getFlashcardsForSubjectSync(int subjectId);

    @Query("SELECT * FROM flashcard_table WHERE subjectId = :subjectId ORDER BY id ASC")
    LiveData<List<Flashcard>> getFlashcardsForSubject(int subjectId);

    @Query("DELETE FROM flashcard_table")
    void deleteAllFlashcards();

    @Query("SELECT * FROM flashcard_table WHERE nextReviewDate <= :currentTimeInMillis ORDER BY nextReviewDate ASC")
    LiveData<List<Flashcard>> getCardsDue(long currentTimeInMillis);

    @Query("SELECT COUNT(*) FROM flashcard_table WHERE nextReviewDate <= :currentTimeInMillis")
    LiveData<Integer> getCardsDueCount(long currentTimeInMillis);

    @Query("SELECT COUNT(*) FROM flashcard_table WHERE nextReviewDate <= :currentTimeInMillis")
    int getCardsDueCountSync(long currentTimeInMillis);

    @Query("SELECT COUNT(*) FROM flashcard_table WHERE `interval` == 0")
    LiveData<Integer> getLearningCardsCount();

    @Query("SELECT COUNT(*) FROM flashcard_table WHERE `interval` > 0 AND `interval` < 21")
    LiveData<Integer> getYoungCardsCount();

    @Query("SELECT COUNT(*) FROM flashcard_table WHERE `interval` >= 21")
    LiveData<Integer> getMatureCardsCount();

    @Query("SELECT f.id, f.question, f.answer, f.subjectId, f.comment, f.difficulty, f.repetition, f.interval, f.easinessFactor, f.nextReviewDate, s.name AS subjectName FROM flashcard_table f INNER JOIN subject_table s ON f.subjectId = s.id ORDER BY f.id ASC")
    LiveData<List<FlashcardWithSubjectName>> getAllFlashcardsWithSubjectName();

    @Query("SELECT f.id, f.question, f.answer, f.subjectId, f.comment, f.difficulty, f.repetition, f.interval, f.easinessFactor, f.nextReviewDate, s.name AS subjectName FROM flashcard_table f INNER JOIN subject_table s ON f.subjectId = s.id WHERE f.nextReviewDate <= :currentTimeInMillis ORDER BY f.nextReviewDate ASC")
    LiveData<List<FlashcardWithSubjectName>> getCardsDueWithSubjectName(long currentTimeInMillis);
}
