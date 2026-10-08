package com.example.flashcard.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SubjectDao {
    @Insert
    long insert(Subject subject);

    @Update
    void update(Subject subject);

    @Delete
    void delete(Subject subject);

    @Query("SELECT * FROM subject_table ORDER BY name ASC")
    LiveData<List<Subject>> getAllSubjects();

    @Query("SELECT * FROM subject_table ORDER BY name ASC")
    List<Subject> getAllSubjectsSync();

    @Query("SELECT s.id, s.name, s.difficulty, COUNT(f.id) AS flashcardCount FROM subject_table s LEFT JOIN flashcard_table f ON s.id = f.subjectId GROUP BY s.id ORDER BY s.name ASC")
    LiveData<List<SubjectWithCount>> getAllSubjectsWithCount();
    
    @Query("SELECT * FROM subject_table WHERE id = :subjectId LIMIT 1")
    Subject getSubjectByIdSync(int subjectId);

    @Query("SELECT COUNT(*) FROM flashcard_table WHERE subjectId = :subjectId")
    LiveData<Integer> getFlashcardCountForSubject(int subjectId);
}


