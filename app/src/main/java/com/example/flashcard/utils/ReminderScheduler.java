package com.example.flashcard.utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Helper that schedules/cancels the daily study reminder using WorkManager.
 * Uses a OneTimeWorkRequest with an initial delay calculated to fire at the
 * user's chosen time, then re-schedules itself from inside the worker.
 */
public class ReminderScheduler {

    private static final String WORK_NAME = "daily_study_reminder";

    /**
     * Schedule a one-shot work request that fires at the user's preferred
     * reminder time. Called on enable, after boot, and from the worker itself
     * for the next-day repeat.
     */
    public static void scheduleDaily(Context context) {
        if (!ReminderPreferences.isReminderEnabled(context)) {
            return;
        }

        int hour = ReminderPreferences.getReminderHour(context);
        int minute = ReminderPreferences.getReminderMinute(context);

        long delay = computeDelay(hour, minute);

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(StudyReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request);
    }

    /**
     * Cancel any pending reminder work.
     */
    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }

    /**
     * Calculate milliseconds from now until the next occurrence of hour:minute.
     * If that time has already passed today, targets tomorrow.
     */
    static long computeDelay(int hour, int minute) {
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long now = System.currentTimeMillis();
        if (target.getTimeInMillis() <= now) {
            // Already passed today — schedule for tomorrow
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        return target.getTimeInMillis() - now;
    }
}
