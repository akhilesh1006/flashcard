package com.example.flashcard.importer;

public class ParsedFlashcard {
    private String question;
    private String answer;

    public ParsedFlashcard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}
