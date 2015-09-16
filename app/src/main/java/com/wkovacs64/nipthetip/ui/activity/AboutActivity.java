package com.wkovacs64.nipthetip.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsFragment;
import com.wkovacs64.nipthetip.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class AboutActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize Butter Knife bindings
        ButterKnife.bind(this);

        // Initialize the Toolbar
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // Initialize the UI
        initFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Handle the Up navigation button in the toolbar
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initFragment() {
        // Create the Support fragment
        LibsFragment supportFragment = new LibsBuilder()
                .withFields(R.string.class.getFields())
                .withAboutIconShown(true)
                .withAboutVersionShownName(true)
                .withAboutDescription(getString(R.string.app_description))
                .withLibraries(getResources().getStringArray(R.array.undetected_libraries))
                .fragment();

        // Put it in the layout
        if (supportFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, supportFragment)
                    .commit();
        }
    }
}
