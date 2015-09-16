package com.wkovacs64.nipthetip.injection;

import com.wkovacs64.nipthetip.ui.activity.CalcActivity;
import com.wkovacs64.nipthetip.ui.fragment.SettingsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        SharedPreferencesModule.class})
public interface AppComponent {
    void inject(CalcActivity calcActivity);

    void inject(SettingsFragment settingsFragment);
}
