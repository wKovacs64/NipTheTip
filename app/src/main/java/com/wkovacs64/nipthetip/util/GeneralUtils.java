/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Justin R. Hall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
