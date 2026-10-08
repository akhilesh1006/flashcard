package com.example.flashcard.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_days")
public class StudyDay {
    @PrimaryKey
    @NonNull
    public String date; // Format: "yyyy-MM-dd"

    public int cardsStudied;
    public int testsCompleted;
    public boolean goalCompleted;
    public int dailyGoalAtTheTime;

    public StudyDay(@NonNull String date, int cardsStudied, int testsCompleted, boolean goalCompleted, int dailyGoalAtTheTime) {
        this.date = date;
        this.cardsStudied = cardsStudied;
        this.testsCompleted = testsCompleted;
        this.goalCompleted = goalCompleted;
        this.dailyGoalAtTheTime = dailyGoalAtTheTime;
    }
}