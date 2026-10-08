package com.example.flashcard.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "exam_plan_days",
        primaryKeys = {"planId", "date"},
        foreignKeys = @ForeignKey(
                entity = ExamPlan.class,
                parentColumns = "id",
                childColumns = "planId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("planId")}
)
public class ExamPlanDay {
    public int planId;

    @NonNull
    public String date; // "yyyy-MM-dd"

    public int recommendedTargetAtTheTime;
    public int uniqueCardsCoveredThatDay;
    public int reviewsCompletedThatDay;

    public ExamPlanDay(int planId, @NonNull String date, int recommendedTargetAtTheTime) {
        this.planId = planId;
        this.date = date;
        this.recommendedTargetAtTheTime = recommendedTargetAtTheTime;
        this.uniqueCardsCoveredThatDay = 0;
        this.reviewsCompletedThatDay = 0;
    }
}