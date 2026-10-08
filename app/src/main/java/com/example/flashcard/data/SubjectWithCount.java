package com.example.flashcard.data;

import androidx.room.Embedded;

public class SubjectWithCount {
    @Embedded
    public Subject subject;
    
    public int flashcardCount;
}
