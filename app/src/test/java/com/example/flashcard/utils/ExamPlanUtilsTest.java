package com.example.flashcard.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ExamPlanUtilsTest {

    @Test
    public void testGetDaysBetweenInclusive() {
        assertEquals(1, ExamPlanUtils.getDaysBetweenInclusive("2026-09-08", "2026-09-08"));
        assertEquals(3, ExamPlanUtils.getDaysBetweenInclusive("2026-09-08", "2026-09-10"));
        assertEquals(0, ExamPlanUtils.getDaysBetweenInclusive("2026-09-10", "2026-09-08"));
    }

    @Test
    public void testCalculateTarget() {
        // 100 cards, 0 covered, 10 days => 10/day
        assertEquals(10, ExamPlanUtils.calculateTarget(100, 0, "2026-09-01", "2026-09-10"));

        // 100 cards, 5 covered, 10 days => 95 / 10 = 9.5 -> 10/day
        assertEquals(10, ExamPlanUtils.calculateTarget(100, 5, "2026-09-01", "2026-09-10"));

        // 5 cards, 0 covered, 10 days => 5 / 10 = 0.5 -> 1/day
        assertEquals(1, ExamPlanUtils.calculateTarget(5, 0, "2026-09-01", "2026-09-10"));

        // Target caps at remaining when exam has passed vs remaining study days
        // Today is past the exam date -> remainingDays is 0 -> fallback target is remaining cards
        assertEquals(30, ExamPlanUtils.calculateTarget(100, 70, "2026-09-11", "2026-09-10"));

        // Completed plan
        assertEquals(0, ExamPlanUtils.calculateTarget(100, 100, "2026-09-01", "2026-09-10"));
        assertEquals(0, ExamPlanUtils.calculateTarget(100, 105, "2026-09-01", "2026-09-10"));
    }

    @Test
    public void testCalculateStatus() {
        // 100 total, 10 days plan -> expected 10/day

        // Day 1 (elapsed=1): expected 10
        // Covered = 15; Tolerance = 2 => Ahead
        assertEquals(ExamPlanUtils.PlanStatus.AHEAD,
            ExamPlanUtils.calculateStatus(100, 15, "2026-09-01", "2026-09-01", "2026-09-10", 2));

        // Covered = 10; Expected = 10 => On Track
        assertEquals(ExamPlanUtils.PlanStatus.ON_TRACK,
            ExamPlanUtils.calculateStatus(100, 10, "2026-09-01", "2026-09-01", "2026-09-10", 2));

        // Covered = 8; Expected = 10. Within tolerance (2) => On Track
        assertEquals(ExamPlanUtils.PlanStatus.ON_TRACK,
            ExamPlanUtils.calculateStatus(100, 8, "2026-09-01", "2026-09-01", "2026-09-10", 2));

        // Covered = 7; Expected = 10. Below tolerance (2) => Behind
        assertEquals(ExamPlanUtils.PlanStatus.BEHIND,
            ExamPlanUtils.calculateStatus(100, 7, "2026-09-01", "2026-09-01", "2026-09-10", 2));
    }

    @Test
    public void testCalculateStatusExtremes() {
        // Plan hasn't started yet
        assertEquals(ExamPlanUtils.PlanStatus.ON_TRACK,
            ExamPlanUtils.calculateStatus(100, 0, "2026-09-05", "2026-09-01", "2026-09-10", 2));

        // Plan is already finished covering all cards
        assertEquals(ExamPlanUtils.PlanStatus.COMPLETED,
            ExamPlanUtils.calculateStatus(100, 100, "2026-09-01", "2026-09-05", "2026-09-10", 2));

        // Over total capacity (some extra cards covered?)
        assertEquals(ExamPlanUtils.PlanStatus.COMPLETED,
            ExamPlanUtils.calculateStatus(100, 150, "2026-09-01", "2026-09-05", "2026-09-10", 2));

        // Exceeding the end date - elapsed > total days => elapsed capped at total days. Expected = 100.
        // Today is 09-12 (2 days later). Expected = 100.
        // Covered = 90. Behind!
        assertEquals(ExamPlanUtils.PlanStatus.BEHIND,
            ExamPlanUtils.calculateStatus(100, 90, "2026-09-01", "2026-09-12", "2026-09-10", 2));
    }
}
