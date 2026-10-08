package com.example.flashcard.data;

import androidx.room.Embedded;
import androidx.room.Relation;

public class FlashcardWithSubject {
    @Embedded
    public Flashcard flashcard;

    @Relation(parentColumn = "subjectId", entityColumn = "id")
    public Subject subject;
}
