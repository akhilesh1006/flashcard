package com.example.flashcard.utils;

import com.example.flashcard.data.StudyDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GoalUtils {

    public static class GoalProgress {
        public int currentGoal;
        public int cardsStudied;
        public int percentage;
        public boolean isComplete;
        public int remaining;
        public int extra;
    }

    public static GoalProgress calculateGoalProgress(List<StudyDay> studyDays, int currentGoal) {
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        StudyDay todayDay = null;
        if (studyDays != null) {
            for (StudyDay day : studyDays) {
                if (day.date.equals(todayStr)) {
                    todayDay = day;
                    break;
                }
            }
        }

        int cardsStudied = todayDay != null ? todayDay.cardsStudied : 0;

        GoalProgress gp = new GoalProgress();
        gp.currentGoal = currentGoal;
        gp.cardsStudied = cardsStudied;

        gp.isComplete = cardsStudied >= currentGoal;
        if (gp.isComplete) {
            gp.extra = cardsStudied - currentGoal;
            gp.remaining = 0;
        } else {
            gp.remaining = currentGoal - cardsStudied;
            gp.extra = 0;
        }

        int percent = (int) (((float) cardsStudied / currentGoal) * 100);
        if (percent > 100) percent = 100;
        if (percent < 0) percent = 0;

        gp.percentage = percent;

        return gp;
    }
}
