package com.example.flashcard.utils;

import com.example.flashcard.data.StudyDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StreakUtils {

    public static int calculateCurrentStreak(List<StudyDay> studyDays) {
        int currentStreak = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        String today = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = sdf.format(cal.getTime());

        int todayIndex = -1;
        int yesterdayIndex = -1;

        if (studyDays != null) {
            for (int i = 0; i < studyDays.size(); i++) {
                if (studyDays.get(i).date.equals(today)) todayIndex = i;
                if (studyDays.get(i).date.equals(yesterday)) yesterdayIndex = i;
            }
        }

        boolean isActiveToday = todayIndex != -1 && isActive(studyDays.get(todayIndex));
        boolean isActiveYesterday = yesterdayIndex != -1 && isActive(studyDays.get(yesterdayIndex));

        boolean streakAlive = isActiveToday || isActiveYesterday;

        if (streakAlive) {
            String checkDate = isActiveToday ? today : yesterday;

            for (StudyDay day : studyDays) {
                if (day.date.equals(checkDate) && isActive(day)) {
                    currentStreak++;
                    try {
                        Calendar c = Calendar.getInstance();
                        c.setTime(sdf.parse(checkDate));
                        c.add(Calendar.DAY_OF_YEAR, -1);
                        checkDate = sdf.format(c.getTime());
                    } catch (Exception e) {}
                } else if (day.date.compareTo(checkDate) < 0) {
                    break;
                }
            }
        }
        return currentStreak;
    }

    public static boolean isActive(StudyDay day) {
        return day != null && (day.cardsStudied > 0 || day.testsCompleted > 0);
    }
}
