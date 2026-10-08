package com.example.flashcard.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "flashcard_table",
        foreignKeys = @ForeignKey(entity = Subject.class,
                parentColumns = "id",
                childColumns = "subjectId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("subjectId")})
public class Flashcard {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String question;
    private String answer;
    private int subjectId;

    @NonNull
    @ColumnInfo(defaultValue = "''")
    private String comment;

        @NonNull
    @ColumnInfo(defaultValue = "'MEDIUM'")
    private String difficulty;

    @ColumnInfo(defaultValue = "0")
    private int repetition;

    @ColumnInfo(defaultValue = "0")
    private int interval;

    @ColumnInfo(defaultValue = "2.5")
    private float easinessFactor;

    @ColumnInfo(defaultValue = "0")
    private long nextReviewDate;

        public Flashcard(String question, String answer, int subjectId, String comment, String difficulty) {
        this.question = question;
        this.answer = answer;
        this.subjectId = subjectId;
        this.comment = comment == null ? "" : comment;
        this.difficulty = difficulty == null ? "MEDIUM" : difficulty;
        this.repetition = 0;
        this.interval = 0;
        this.easinessFactor = 2.5f;
        this.nextReviewDate = 0;
    }

    @Ignore
    public Flashcard(String question, String answer, int subjectId) {
        this(question, answer, subjectId, "", "MEDIUM");
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment == null ? "" : comment;
    }

    public String getDifficulty() {
        return difficulty;
    }

        public void setDifficulty(String difficulty) {
        this.difficulty = difficulty == null ? "MEDIUM" : difficulty;
    }

    public int getRepetition() { return repetition; }
    public void setRepetition(int repetition) { this.repetition = repetition; }

    public int getInterval() { return interval; }
    public void setInterval(int interval) { this.interval = interval; }

    public float getEasinessFactor() { return easinessFactor; }
    public void setEasinessFactor(float easinessFactor) { this.easinessFactor = easinessFactor; }

    public long getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(long nextReviewDate) { this.nextReviewDate = nextReviewDate; }

    public void applySm2Rating(int quality) {
        if (quality < 0) quality = 0;
        if (quality > 5) quality = 5;
        
        if (quality < 3) {
            repetition = 0;
            interval = 1;
        } else {
            if (repetition == 0) {
                interval = 1;
            } else if (repetition == 1) {
                interval = 6;
            } else {
                interval = Math.round(interval * easinessFactor);
            }
            repetition++;
        }
        
        easinessFactor = easinessFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f));
        if (easinessFactor < 1.3f) easinessFactor = 1.3f;
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        if (quality < 3) {
            // "Again" - next review is immediately or today. Setting it to now puts it due immediately.
            nextReviewDate = cal.getTimeInMillis();
        } else {
            cal.add(java.util.Calendar.DAY_OF_YEAR, interval);
            nextReviewDate = cal.getTimeInMillis();
        }
    }
}