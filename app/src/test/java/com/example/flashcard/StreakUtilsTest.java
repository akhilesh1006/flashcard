package com.example.flashcard;

import com.example.flashcard.data.StudyDay;
import com.example.flashcard.utils.StreakUtils;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit tests for StreakUtils streak calculation logic.
 */
public class StreakUtilsTest {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private String daysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return sdf.format(cal.getTime());
    }

    private String today() {
        return daysAgo(0);
    }

    private StudyDay activeDay(String date, int cards) {
        return new StudyDay(date, cards, 0, false, 20);
    }

    private StudyDay testDay(String date) {
        return new StudyDay(date, 0, 1, false, 20);
    }

    private StudyDay inactiveDay(String date) {
        return new StudyDay(date, 0, 0, false, 20);
    }

    // --- isActive tests ---

    @Test
    public void isActive_nullDay_returnsFalse() {
        assertFalse(StreakUtils.isActive(null));
    }

    @Test
    public void isActive_noStudy_returnsFalse() {
        assertFalse(StreakUtils.isActive(inactiveDay(today())));
    }

    @Test
    public void isActive_withCardsStudied_returnsTrue() {
        assertTrue(StreakUtils.isActive(activeDay(today(), 5)));
    }

    @Test
    public void isActive_withTestsCompleted_returnsTrue() {
        assertTrue(StreakUtils.isActive(testDay(today())));
    }

    // --- calculateCurrentStreak tests ---

    @Test
    public void calculateStreak_nullList_returnsZero() {
        assertEquals(0, StreakUtils.calculateCurrentStreak(null));
    }

    @Test
    public void calculateStreak_emptyList_returnsZero() {
        assertEquals(0, StreakUtils.calculateCurrentStreak(new ArrayList<>()));
    }

    @Test
    public void calculateStreak_onlyTodayActive_returnsOne() {
        List<StudyDay> days = new ArrayList<>();
        days.add(activeDay(today(), 10));
        assertEquals(1, StreakUtils.calculateCurrentStreak(days));
    }

    @Test
    public void calculateStreak_todayInactive_yesterdayActive_returnsOne() {
        List<StudyDay> days = new ArrayList<>();
        days.add(activeDay(daysAgo(1), 10));
        // Streak is still alive if yesterday was active, returns 1
        assertEquals(1, StreakUtils.calculateCurrentStreak(days));
    }

    @Test
    public void calculateStreak_todayAndYesterdayActive_returnsTwo() {
        List<StudyDay> days = new ArrayList<>();
        days.add(activeDay(today(), 5));
        days.add(activeDay(daysAgo(1), 5));
        assertEquals(2, StreakUtils.calculateCurrentStreak(days));
    }

    @Test
    public void calculateStreak_threeDayStreak_returnsThree() {
        List<StudyDay> days = new ArrayList<>();
        days.add(activeDay(today(), 5));
        days.add(activeDay(daysAgo(1), 5));
        days.add(activeDay(daysAgo(2), 5));
        assertEquals(3, StreakUtils.calculateCurrentStreak(days));
    }

    @Test
    public void calculateStreak_gapBreaksStreak() {
        // Active today, skip yesterday, active 2 days ago
        List<StudyDay> days = new ArrayList<>();
        days.add(activeDay(today(), 5));
        days.add(inactiveDay(daysAgo(1)));
        days.add(activeDay(daysAgo(2), 5));
        // Gap yesterday breaks the streak — only today counts
        assertEquals(1, StreakUtils.calculateCurrentStreak(days));
    }

    @Test
    public void calculateStreak_todayInactive_twoDayOldInactive_returnsZero() {
        // Neither today nor yesterday are active
        List<StudyDay> days = new ArrayList<>();
        days.add(activeDay(daysAgo(2), 10));
        assertEquals(0, StreakUtils.calculateCurrentStreak(days));
    }

    @Test
    public void calculateStreak_inactiveDayDoesNotCount() {
        List<StudyDay> days = new ArrayList<>();
        days.add(inactiveDay(today()));
        assertEquals(0, StreakUtils.calculateCurrentStreak(days));
    }

    @Test
    public void calculateStreak_testCompletionCountsAsActive() {
        List<StudyDay> days = new ArrayList<>();
        days.add(testDay(today()));
        assertEquals(1, StreakUtils.calculateCurrentStreak(days));
    }
}
