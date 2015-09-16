package com.wkovacs64.nipthetip;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import com.mikepenz.iconics.Iconics;
import com.mikepenz.octicons_typeface_library.Octicons;
import com.squareup.leakcanary.LeakCanary;
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

    private static AppComponent sAppComponent;

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
    }

    /**
     * Retrieves the static instance of AppComponent for dependency injection.
     *
     * @return the current AppComponent
     */
    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    private void initializeDebugTools() {
        if (BuildConfig.DEBUG) {
            LeakCanary.install(this);
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
        sAppComponent = DaggerAppComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initializeIconics() {
        // An alternative to keeping R from ProGuard
        Iconics.init(this);
        Iconics.registerFont(new Octicons());
    }
}
