package com.example.flashcard.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Re-schedules the daily study reminder after the device reboots.
 * Registered in AndroidManifest with RECEIVE_BOOT_COMPLETED.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (ReminderPreferences.isReminderEnabled(context)) {
                ReminderScheduler.scheduleDaily(context);
            }
        }
    }
}
