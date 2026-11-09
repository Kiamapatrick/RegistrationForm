package com.example.registrationform.util;

import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;

/**
 * Utility for motion-related checks and operations.
 */
public final class MotionUtils {

    private MotionUtils() {}

    /**
     * Returns true if the user has disabled animations in system settings
     * or if battery saver mode is active.
     */
    public static boolean isMotionReduced(Context context) {
        float scale = Settings.Global.getFloat(context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        if (scale == 0f) {
            return true;
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null && pm.isPowerSaveMode()) {
            return true;
        }

        return false;
    }

    /**
     * Runs the given animation only if motion is not reduced.
     * Otherwise, runs the fallback immediately (if provided).
     */
    public static void runIfNotReduced(Context context, Runnable animation, Runnable fallback) {
        if (isMotionReduced(context)) {
            if (fallback != null) {
                fallback.run();
            }
        } else {
            animation.run();
        }
    }

    /**
     * Runs the given animation only if motion is not reduced.
     */
    public static void runIfNotReduced(Context context, Runnable animation) {
        runIfNotReduced(context, animation, null);
    }
}