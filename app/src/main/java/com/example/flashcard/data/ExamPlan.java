package com.example.flashcard.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exam_plans")
public class ExamPlan {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    // Using format "yyyy-MM-dd"
    public String startDate;
    public String examDate;

    public long createdAt;
    public boolean archived;
    public long completedAt; // 0 if not completed

    public ExamPlan(String name, String startDate, String examDate, long createdAt) {
        this.name = name;
        this.startDate = startDate;
        this.examDate = examDate;
        this.createdAt = createdAt;
        this.archived = false;
        this.completedAt = 0;
    }
}