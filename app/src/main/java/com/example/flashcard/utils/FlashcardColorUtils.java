package com.example.flashcard.utils;

import android.content.Context;
import android.graphics.Color;

public class FlashcardColorUtils {
    
    private static final int[] VIBRANT_COLORS = {
        Color.parseColor("#00E5FF"), // Bright Cyan
        Color.parseColor("#E040FB"), // Bright Purple
        Color.parseColor("#69F0AE"), // Bright Mint Green
        Color.parseColor("#FFAB40"), // Bright Orange
        Color.parseColor("#FF4081"), // Bright Pink
        Color.parseColor("#FFFF00"), // Yellow
        Color.parseColor("#00B0FF"), // Electric Blue
        Color.parseColor("#1DE9B6"), // Turquoise
        Color.parseColor("#311B92"), // Dark Deep Purple
        Color.parseColor("#0D47A1"), // Dark Navy Blue
        Color.parseColor("#B71C1C"), // Dark Deep Red
    };

    /**
     * Get a stable color for a card based on its ID.
     */
    public static int getColorForId(int id) {
        return VIBRANT_COLORS[Math.abs(id) % VIBRANT_COLORS.length];
    }

    /**
     * Get a stable color based on subject ID, with safe fallback for invalid IDs.
     */
    public static int getColorForSubject(Context context, int subjectId) {
        if (subjectId <= 0) {
            return VIBRANT_COLORS[0];
        }
        return getColorForId(subjectId);
    }

    /**
     * Overload for getting color by subject ID directly.
     */
    public static int getColorForSubject(int subjectId) {
        return getColorForSubject(null, subjectId);
    }
    
    /**
     * Get a stable color based on string hash (for subject names).
     */
    public static int getColorForString(String text) {
        if (text == null || text.isEmpty()) return VIBRANT_COLORS[0];
        return VIBRANT_COLORS[Math.abs(text.hashCode()) % VIBRANT_COLORS.length];
    }

    /**
     * Dynamically determines whether the primary text should be light or dark based on contrast.
     */
    public static int getPrimaryTextColor(int backgroundColor) {
        if (isLight(backgroundColor)) {
            // Very dark charcoal for bright backgrounds
            return Color.parseColor("#121212");
        } else {
            // White for dark backgrounds
            return Color.parseColor("#FFFFFF");
        }
    }

    /**
     * Dynamically determines secondary text color.
     */
    public static int getSecondaryTextColor(int backgroundColor) {
        if (isLight(backgroundColor)) {
            // Dark gray for bright backgrounds
            return Color.parseColor("#444444");
        } else {
            // Light gray/white-transparent for dark backgrounds
            return Color.parseColor("#E0E0E0");
        }
    }
    
    private static boolean isLight(int color) {
        double luminance = (0.299 * Color.red(color) + 
                            0.587 * Color.green(color) + 
                            0.114 * Color.blue(color)) / 255.0;
        return luminance > 0.5;
    }
}
