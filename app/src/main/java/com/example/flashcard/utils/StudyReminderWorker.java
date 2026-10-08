package com.example.flashcard.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.flashcard.MainActivity;
import com.example.flashcard.R;

/**
 * WorkManager worker that builds and shows a daily study reminder notification.
 * Re-schedules itself for the next day after firing.
 */
public class StudyReminderWorker extends Worker {

    public static final String CHANNEL_ID = "study_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;

    public StudyReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        createNotificationChannel();
        showNotification();

        // Re-schedule for the next day
        ReminderScheduler.scheduleDaily(getApplicationContext());

        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Study Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Daily reminders to study your flashcards");

            NotificationManager manager = getApplicationContext()
                    .getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification() {
        Context context = getApplicationContext();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int dailyGoal = DailyGoalPreferences.getDailyGoal(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Time to study! 📚")
                .setContentText("You have " + dailyGoal + " cards to review today. Keep your streak alive!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
