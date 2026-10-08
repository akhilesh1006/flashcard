package com.example.flashcard.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "exam_plan_cards",
        primaryKeys = {"planId", "flashcardId"},
        foreignKeys = @ForeignKey(
                entity = ExamPlan.class,
                parentColumns = "id",
                childColumns = "planId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("planId")}
)
public class ExamPlanCard {
    public int planId;
    public int flashcardId;

    @NonNull
    public String questionSnapshot;

    @NonNull
    public String subjectNameSnapshot;

    public boolean covered;
    public long firstCoveredAt;

    public ExamPlanCard(int planId, int flashcardId, @NonNull String questionSnapshot, @NonNull String subjectNameSnapshot) {
        this.planId = planId;
        this.flashcardId = flashcardId;
        this.questionSnapshot = questionSnapshot;
        this.subjectNameSnapshot = subjectNameSnapshot;
        this.covered = false;
        this.firstCoveredAt = 0;
    }
}