package com.example.flashcard.importer;

public class ImportError {
    private int entryNumber;
    private String description;

    public ImportError(int entryNumber, String description) {
        this.entryNumber = entryNumber;
        this.description = description;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    public String getDescription() {
        return description;
    }
}
