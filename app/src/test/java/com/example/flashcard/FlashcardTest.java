package com.example.flashcard;

import com.example.flashcard.data.Flashcard;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Flashcard SM-2 spaced repetition logic.
 */
public class FlashcardTest {

    private Flashcard card;

    @Before
    public void setUp() {
        card = new Flashcard("What is Java?", "A programming language", 1);
    }

    // --- Default state tests ---

    @Test
    public void newCard_hasDefaultSm2Values() {
        assertEquals(0, card.getRepetition());
        assertEquals(0, card.getInterval());
        assertEquals(2.5f, card.getEasinessFactor(), 0.001f);
        assertEquals(0L, card.getNextReviewDate());
    }

    // --- Quality clamping ---

    @Test
    public void applySm2Rating_clampsNegativeQualityToZero() {
        card.applySm2Rating(-5);
        // quality < 3 resets repetition to 0, interval to 1
        assertEquals(0, card.getRepetition());
        assertEquals(1, card.getInterval());
    }

    @Test
    public void applySm2Rating_clampsQualityAboveFiveToFive() {
        card.applySm2Rating(10);
        // quality >= 3 increments repetition; first correct rep sets interval = 1
        assertEquals(1, card.getRepetition());
        assertEquals(1, card.getInterval());
    }

    // --- Quality < 3 (failure/again) ---

    @Test
    public void applySm2Rating_qualityZero_resetsRepetition() {
        // First, do a correct review to build up state
        card.applySm2Rating(4);
        assertEquals(1, card.getRepetition());

        // Then fail
        card.applySm2Rating(0);
        assertEquals(0, card.getRepetition());
        assertEquals(1, card.getInterval());
    }

    @Test
    public void applySm2Rating_qualityOne_resetsRepetition() {
        card.applySm2Rating(5);
        card.applySm2Rating(1);
        assertEquals(0, card.getRepetition());
        assertEquals(1, card.getInterval());
    }

    @Test
    public void applySm2Rating_qualityTwo_resetsRepetition() {
        card.applySm2Rating(5);
        card.applySm2Rating(2);
        assertEquals(0, card.getRepetition());
        assertEquals(1, card.getInterval());
    }

    @Test
    public void applySm2Rating_qualityLessThanThree_setsNextReviewToNow() {
        long before = System.currentTimeMillis();
        card.applySm2Rating(1);
        long after = System.currentTimeMillis();
        // nextReviewDate should be approximately "now"
        assertTrue(card.getNextReviewDate() >= before);
        assertTrue(card.getNextReviewDate() <= after);
    }

    // --- Quality >= 3 (success) ---

    @Test
    public void applySm2Rating_firstCorrect_setsIntervalToOne() {
        card.applySm2Rating(4); // first correct
        assertEquals(1, card.getRepetition());
        assertEquals(1, card.getInterval());
    }

    @Test
    public void applySm2Rating_secondCorrect_setsIntervalToSix() {
        card.applySm2Rating(4); // repetition 0 -> 1, interval = 1
        card.applySm2Rating(4); // repetition 1 -> 2, interval = 6
        assertEquals(2, card.getRepetition());
        assertEquals(6, card.getInterval());
    }

    @Test
    public void applySm2Rating_thirdCorrect_multipliesIntervalByEF() {
        card.applySm2Rating(4); // rep 0->1, interval=1
        card.applySm2Rating(4); // rep 1->2, interval=6

        float efBefore = card.getEasinessFactor();
        card.applySm2Rating(4); // rep 2->3, interval = round(6 * EF)

        assertEquals(3, card.getRepetition());
        int expectedInterval = Math.round(6 * efBefore);
        assertEquals(expectedInterval, card.getInterval());
    }

    @Test
    public void applySm2Rating_correctReview_setsNextReviewInFuture() {
        long before = System.currentTimeMillis();
        card.applySm2Rating(5);
        // nextReviewDate should be in the future (at least 1 day ahead)
        assertTrue(card.getNextReviewDate() > before);
    }

    // --- Easiness factor tests ---

    @Test
    public void applySm2Rating_perfectQuality_increasesEF() {
        float initialEF = card.getEasinessFactor();
        card.applySm2Rating(5);
        // EF formula: EF + (0.1 - (5-5)*(0.08+(5-5)*0.02)) = EF + 0.1
        assertTrue(card.getEasinessFactor() > initialEF);
        assertEquals(initialEF + 0.1f, card.getEasinessFactor(), 0.001f);
    }

    @Test
    public void applySm2Rating_qualityFour_slightlyDecreasesEF() {
        float initialEF = card.getEasinessFactor();
        card.applySm2Rating(4);
        // EF + (0.1 - 1*(0.08 + 1*0.02)) = EF + 0.1 - 0.10 = EF + 0.0
        assertEquals(initialEF, card.getEasinessFactor(), 0.001f);
    }

    @Test
    public void applySm2Rating_qualityThree_decreasesEF() {
        float initialEF = card.getEasinessFactor();
        card.applySm2Rating(3);
        // EF + (0.1 - 2*(0.08 + 2*0.02)) = EF + 0.1 - 0.24 = EF - 0.14
        assertTrue(card.getEasinessFactor() < initialEF);
        assertEquals(initialEF - 0.14f, card.getEasinessFactor(), 0.001f);
    }

    @Test
    public void applySm2Rating_efNeverDropsBelowMinimum() {
        // Repeatedly apply low quality to drive EF down
        for (int i = 0; i < 50; i++) {
            card.applySm2Rating(0);
        }
        assertTrue(card.getEasinessFactor() >= 1.3f);
    }

    // --- Reset after failure ---

    @Test
    public void applySm2Rating_failAfterMultipleCorrect_resetsRepAndInterval() {
        card.applySm2Rating(5);
        card.applySm2Rating(5);
        card.applySm2Rating(5);
        assertEquals(3, card.getRepetition());
        assertTrue(card.getInterval() > 1);

        // Now fail
        card.applySm2Rating(0);
        assertEquals(0, card.getRepetition());
        assertEquals(1, card.getInterval());
    }

    // --- Mastery level classification ---

    @Test
    public void masteryLevel_newCard_isLearning() {
        // interval == 0 means "Learning"
        assertEquals(0, card.getInterval());
    }

    @Test
    public void masteryLevel_afterFirstCorrect_isYoung() {
        card.applySm2Rating(4);
        // interval = 1, which is > 0 and < 21 → "Young"
        assertTrue(card.getInterval() > 0 && card.getInterval() < 21);
    }

    @Test
    public void masteryLevel_afterManyCorrect_isMature() {
        // Need interval >= 21 for "Mature"
        // Apply many perfect reviews to grow the interval
        for (int i = 0; i < 10; i++) {
            card.applySm2Rating(5);
        }
        assertTrue("Expected interval >= 21 for mature, got: " + card.getInterval(),
                card.getInterval() >= 21);
    }

    // --- Edge cases ---

    @Test
    public void applySm2Rating_qualityExactlyThree_isStillCorrect() {
        card.applySm2Rating(3);
        // quality 3 is >= 3, so repetition should increment
        assertEquals(1, card.getRepetition());
        assertEquals(1, card.getInterval());
    }

    @Test
    public void applySm2Rating_rapidSuccessionDoesNotCorrupt() {
        card.applySm2Rating(5);
        card.applySm2Rating(5);
        card.applySm2Rating(5);
        card.applySm2Rating(5);
        card.applySm2Rating(5);

        assertEquals(5, card.getRepetition());
        assertTrue(card.getInterval() > 0);
        assertTrue(card.getEasinessFactor() >= 2.5f);
    }
}
