package com.example.flashcard;

import com.example.flashcard.data.StudyDay;
import com.example.flashcard.utils.GoalUtils;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit tests for GoalUtils daily goal progress calculation.
 */
public class GoalUtilsTest {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private String today() {
        return sdf.format(Calendar.getInstance().getTime());
    }

    // --- Null / empty inputs ---

    @Test
    public void goalProgress_nullList_returnsZeroProgress() {
        GoalUtils.GoalProgress gp = GoalUtils.calculateGoalProgress(null, 20);
        assertEquals(0, gp.cardsStudied);
        assertEquals(20, gp.currentGoal);
        assertEquals(0, gp.percentage);
        assertFalse(gp.isComplete);
        assertEquals(20, gp.remaining);
        assertEquals(0, gp.extra);
    }

    @Test
    public void goalProgress_emptyList_returnsZeroProgress() {
        GoalUtils.GoalProgress gp = GoalUtils.calculateGoalProgress(new ArrayList<>(), 20);
        assertEquals(0, gp.cardsStudied);
        assertFalse(gp.isComplete);
    }

    // --- Incomplete goal ---

    @Test
    public void goalProgress_partialStudy_showsCorrectRemaining() {
        List<StudyDay> days = new ArrayList<>();
        days.add(new StudyDay(today(), 8, 0, false, 20));

        GoalUtils.GoalProgress gp = GoalUtils.calculateGoalProgress(days, 20);
        assertEquals(8, gp.cardsStudied);
        assertEquals(20, gp.currentGoal);
        assertEquals(12, gp.remaining);
        assertEquals(0, gp.extra);
        assertFalse(gp.isComplete);
        assertEquals(40, gp.percentage); // 8/20 * 100 = 40
    }

    // --- Goal exactly met ---

    @Test
    public void goalProgress_exactlyMet_isComplete() {
        List<StudyDay> days = new ArrayList<>();
        days.add(new StudyDay(today(), 20, 0, true, 20));

        GoalUtils.GoalProgress gp = GoalUtils.calculateGoalProgress(days, 20);
        assertTrue(gp.isComplete);
        assertEquals(0, gp.remaining);
        assertEquals(0, gp.extra);
        assertEquals(100, gp.percentage);
    }

    // --- Goal exceeded ---

    @Test
    public void goalProgress_exceeded_showsExtra() {
        List<StudyDay> days = new ArrayList<>();
        days.add(new StudyDay(today(), 30, 0, true, 20));

        GoalUtils.GoalProgress gp = GoalUtils.calculateGoalProgress(days, 20);
        assertTrue(gp.isComplete);
        assertEquals(0, gp.remaining);
        assertEquals(10, gp.extra);
        assertEquals(100, gp.percentage); // capped at 100
    }

    // --- Day not found (studied yesterday, not today) ---

    @Test
    public void goalProgress_nothingToday_returnsZero() {
        List<StudyDay> days = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = sdf.format(cal.getTime());
        days.add(new StudyDay(yesterday, 25, 0, true, 20));

        GoalUtils.GoalProgress gp = GoalUtils.calculateGoalProgress(days, 20);
        assertEquals(0, gp.cardsStudied);
        assertFalse(gp.isComplete);
        assertEquals(20, gp.remaining);
    }

    // --- Percentage edge cases ---

    @Test
    public void goalProgress_oneCardOfTwenty_fivePercent() {
        List<StudyDay> days = new ArrayList<>();
        days.add(new StudyDay(today(), 1, 0, false, 20));

        GoalUtils.GoalProgress gp = GoalUtils.calculateGoalProgress(days, 20);
        assertEquals(5, gp.percentage); // (int)(1.0/20 * 100) = 5
    }
}
