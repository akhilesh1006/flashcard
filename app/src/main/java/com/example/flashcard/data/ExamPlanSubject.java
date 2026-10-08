package com.example.flashcard.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "exam_plan_subjects",
        primaryKeys = {"planId", "subjectId"},
        foreignKeys = @ForeignKey(
                entity = ExamPlan.class,
                parentColumns = "id",
                childColumns = "planId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("planId")}
)
public class ExamPlanSubject {
    public int planId;
    public int subjectId;

    @NonNull
    public String subjectNameSnapshot;

    public ExamPlanSubject(int planId, int subjectId, @NonNull String subjectNameSnapshot) {
        this.planId = planId;
        this.subjectId = subjectId;
        this.subjectNameSnapshot = subjectNameSnapshot;
    }
}