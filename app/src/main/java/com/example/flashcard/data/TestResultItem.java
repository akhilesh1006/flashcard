package com.example.flashcard.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "test_result_items",
        foreignKeys = @ForeignKey(entity = TestResult.class,
                parentColumns = "id",
                childColumns = "testResultId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("testResultId")})
public class TestResultItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long testResultId;
    public int flashcardId;
    public String question;
    public int result;
}
