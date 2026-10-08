package com.example.flashcard.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExamPlanUtils {

    public enum PlanStatus {
        AHEAD,
        ON_TRACK,
        BEHIND,
        COMPLETED,
        NO_CARDS
    }

    private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        }
    };

    /**
     * Helper to safely parse dates. Returns 1970 if parsing fails.
     */
    private static long parseDateSafe(String dateStr) {
        if (dateStr == null) return 0;
        try {
            Date d = dateFormat.get().parse(dateStr);
            return d != null ? d.getTime() : 0;
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * Calculates inclusive days between two dates.
     * Example: "2026-09-08" to "2026-09-09" returns 2.
     * If start > end, returns 0.
     */
    public static int getDaysBetweenInclusive(String startStr, String endStr) {
        long start = parseDateSafe(startStr);
        long end = parseDateSafe(endStr);
        if (start == 0 || end == 0) return 1;

        if (start > end) return 0;

        // 86400000 = 24 * 60 * 60 * 1000
        long diff = end - start;
        return (int) (diff / 86400000L) + 1;
    }

    /**
     * Target calculation: ceil((totalPlanCards - uniquelyCoveredCards) / remainingStudyDays).
     */
    public static int calculateTarget(int totalCards, int coveredCards, String today, String examDate) {
        int remainingCards = totalCards - coveredCards;
        if (remainingCards <= 0) {
            return 0; // Completed or no cards
        }

        int remainingStudyDays = getDaysBetweenInclusive(today, examDate);

        // If today is past the exam date, remaining will be 0. We force target to be all remaining cards.
        if (remainingStudyDays <= 0) {
            return remainingCards;
        }

        return (int) Math.ceil((double) remainingCards / remainingStudyDays);
    }

    /**
     * Status calculation based on expected coverage.
     */
    public static PlanStatus calculateStatus(int totalCards, int coveredCards, String startDate, String today, String examDate, int tolerance) {
        if (totalCards == 0) {
            return PlanStatus.NO_CARDS;
        }
        if (coveredCards >= totalCards) {
            return PlanStatus.COMPLETED;
        }

        int totalPlanDays = getDaysBetweenInclusive(startDate, examDate);
        if (totalPlanDays <= 0) {
            // Exam date is before start date (invalid state), just return ON_TRACK or BEHIND depending on coverage
            return PlanStatus.ON_TRACK;
        }

        long start = parseDateSafe(startDate);
        long current = parseDateSafe(today);
        long diff = current - start;

        int elapsedDays = 0;
        if (diff >= 0) {
            elapsedDays = (int) (diff / 86400000L) + 1;
        }

        if (elapsedDays > totalPlanDays) {
            elapsedDays = totalPlanDays;
        }
        if (elapsedDays <= 0) {
            // Plan hasn't started yet
            return PlanStatus.ON_TRACK;
        }

        int expectedCoveredByToday = (int) Math.ceil((double) totalCards * elapsedDays / totalPlanDays);
        if (expectedCoveredByToday > totalCards) expectedCoveredByToday = totalCards;

        int difference = coveredCards - expectedCoveredByToday;

        if (difference > tolerance) {
            return PlanStatus.AHEAD;
        } else if (difference < -tolerance) {
            return PlanStatus.BEHIND;
        } else {
            return PlanStatus.ON_TRACK;
        }
    }
}
