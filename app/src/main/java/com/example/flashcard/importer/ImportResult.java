package com.example.flashcard.importer;

import java.util.List;

public class ImportResult {
    private List<ParsedFlashcard> validCards;
    private List<ImportError> errors;

    public ImportResult(List<ParsedFlashcard> validCards, List<ImportError> errors) {
        this.validCards = validCards;
        this.errors = errors;
    }

    public List<ParsedFlashcard> getValidCards() {
        return validCards;
    }

    public List<ImportError> getErrors() {
        return errors;
    }
}
