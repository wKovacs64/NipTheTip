package com.wkovacs64.nipthetip.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wkovacs64.nipthetip.R;

/**
 * Uninstantiable general-purpose utility class.
 */
public final class GeneralUtils {

    /**
     * Suppress default constructor to prevent instantiation.
     */
    private GeneralUtils() {
        throw new AssertionError();
    }

    /**
     * Tints the status bar according to Google Material Design on Android KitKat.
     *
     * @param activity the Activity to tint
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void tintStatusBarOnKitKat(Activity activity) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            // Get the colorPrimaryDark value from the activity's theme
            TypedValue typedValue = new TypedValue();
            activity.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
            // Enable window translucency
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            // Set a custom tint color for all system bars to the theme's colorPrimaryDark value
            tintManager.setTintColor(typedValue.data);
            // Apply status bar tint
            tintManager.setStatusBarTintEnabled(true);
        }
    }
}
