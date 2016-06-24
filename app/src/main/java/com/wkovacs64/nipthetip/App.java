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

package com.wkovacs64.nipthetip;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.itemanimators.SlideUpAlphaAnimator;
import com.mikepenz.octicons_typeface_library.Octicons;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.wkovacs64.nipthetip.injection.AppComponent;
import com.wkovacs64.nipthetip.injection.ApplicationModule;
import com.wkovacs64.nipthetip.injection.DaggerAppComponent;

import timber.log.Timber;

/**
 * A custom Application.
 */
public final class App extends Application {
    /*
     * SharedPreferences keys.
     */
    public static final String KEY_PREF_TIP_PERCENT = "tip_percent";
    public static final String KEY_PREF_NUM_PEOPLE = "number_of_people";

    private static AppComponent appComponent;
    private static RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize debugging tools
        initializeDebugTools();

        // Initialize logging
        initializeLogging();

        // Initialize Dagger dependency injection
        initializeInjector();

        // Initialize Android-Iconics
        initializeIconics();

        // Initialize AboutLibraries
        initializeAboutLibraries();
    }

    /**
     * Retrieves the static instance of AppComponent for dependency injection.
     *
     * @return the current AppComponent
     */
    public static AppComponent getAppComponent() {
        return appComponent;
    }

    /**
     * Retrieves the static RefWatcher instance for detecting leaks.
     *
     * @return the current RefWatcher
     */
    public static RefWatcher refWatcher() {
        return refWatcher;
    }

    private void initializeDebugTools() {
        refWatcher = LeakCanary.install(this);
        if (BuildConfig.DEBUG) {
            enabledStrictMode();
        }
    }

    private void enabledStrictMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
        }
    }

    private void initializeLogging() {
        if (BuildConfig.DEBUG) {
            // Initialize Timber logging library for debugging
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initializeInjector() {
        appComponent = DaggerAppComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initializeIconics() {
        // An alternative to keeping R from ProGuard
        Iconics.init(this);
        Iconics.registerFont(new Octicons());
    }

    private void initializeAboutLibraries() {
        LibsConfiguration.getInstance().setItemAnimator(new SlideUpAlphaAnimator());
    }
}
