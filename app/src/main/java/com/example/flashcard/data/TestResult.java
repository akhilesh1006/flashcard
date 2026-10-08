package com.example.flashcard.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "test_results")
public class TestResult {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public int subjectId;
    public String subjectName;
    public String difficulty;
    public int totalCards;
    public int correctAnswers;
    public int incorrectAnswers;
    public int unanswered;
    public int score;
    public long timeLimitMillis;
    public long timeUsedMillis;
    public long completedAt;
    public boolean wasTimeUp;
    public boolean isPermanentlySaved;
}
