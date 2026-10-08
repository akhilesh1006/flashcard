package com.example.flashcard.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DailyGoalPreferences {
    private static final String PREF_NAME = "DailyGoalPrefs";
    private static final String KEY_DAILY_GOAL = "daily_goal_cards";

    public static int getDailyGoal(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DAILY_GOAL, 20); // Default 20
    }

    public static void setDailyGoal(Context context, int expectedCards) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_DAILY_GOAL, expectedCards).apply();
    }
}
