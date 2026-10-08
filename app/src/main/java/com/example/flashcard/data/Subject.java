package com.example.flashcard.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.Ignore;

@Entity(tableName = "subject_table", indices = {@Index(value = "name", unique = true)})
public class Subject {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    @NonNull
    @ColumnInfo(defaultValue = "'MEDIUM'")
    private String difficulty;

    public Subject(String name, String difficulty) {
        this.name = name;
        this.difficulty = difficulty == null ? "MEDIUM" : difficulty;
    }

    @Ignore
    public Subject(String name) {
        this(name, "MEDIUM");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty == null ? "MEDIUM" : difficulty;
    }
}