package com.example.flashcard.utils;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flashcard.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Shows a short explanation once, when a user first opens each feature. */
public final class FeatureTutorial {
    private static final String PREFS_NAME = "feature_tutorials";
    private static final String KEY_PREFIX = "seen_";

    private FeatureTutorial() {}

    public static Step step(@StringRes int title, @StringRes int message) {
        return new Step(title, message);
    }

    public static void showOnce(AppCompatActivity activity, String featureKey, Step... steps) {
        showOnce(activity, featureKey, null, steps);
    }

    public static void showOnce(AppCompatActivity activity, String featureKey,
                                Runnable onComplete, Step... steps) {
        boolean seen = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_PREFIX + featureKey, false);
        if (!seen && steps != null && steps.length > 0) {
            showStep(activity, featureKey, steps, 0, onComplete);
        } else if (onComplete != null) {
            onComplete.run();
        }
    }

    private static void showStep(AppCompatActivity activity, String featureKey,
                                 Step[] steps, int index, Runnable onComplete) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        boolean lastStep = index == steps.length - 1;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setTitle(steps[index].title)
                .setMessage(steps[index].message)
                .setCancelable(false)
                .setPositiveButton(lastStep ? R.string.feature_tutorial_done : R.string.feature_tutorial_next,
                        (dialog, which) -> {
                            if (lastStep) {
                                markSeen(activity, featureKey);
                                if (onComplete != null) onComplete.run();
                            } else {
                                showStep(activity, featureKey, steps, index + 1, onComplete);
                            }
                        });

        if (!lastStep) {
            builder.setNegativeButton(R.string.feature_tutorial_skip,
                    (dialog, which) -> {
                        markSeen(activity, featureKey);
                        if (onComplete != null) onComplete.run();
                    });
        }
        builder.show();
    }

    private static void markSeen(Context context, String featureKey) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_PREFIX + featureKey, true)
                .apply();
    }

    public static final class Step {
        @StringRes final int title;
        @StringRes final int message;

        private Step(@StringRes int title, @StringRes int message) {
            this.title = title;
            this.message = message;
        }
    }
}
