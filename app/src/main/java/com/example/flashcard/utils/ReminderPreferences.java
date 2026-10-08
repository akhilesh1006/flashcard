package com.example.flashcard.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences wrapper for study reminder settings.
 */
public class ReminderPreferences {

    private static final String PREF_NAME = "StudyReminderPrefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";

    public static boolean isReminderEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false);
    }

    public static void setReminderEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
    }

    public static int getReminderHour(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_REMINDER_HOUR, 20); // Default 8:00 PM
    }

    public static int getReminderMinute(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_REMINDER_MINUTE, 0);
    }

    public static void setReminderTime(Context context, int hour, int minute) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .apply();
    }
}
