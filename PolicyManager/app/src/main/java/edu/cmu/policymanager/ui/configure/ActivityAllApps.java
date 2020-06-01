package edu.cmu.policymanager.ui.configure;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.List;
import java.util.function.Consumer;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.cards.allapps.AppCard;
import edu.cmu.policymanager.viewmodel.W4PData;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 12/28/2018.
 *
 * Filters apps based on category, and displays all apps in that category. Pressing on
 * an app card will take you to its app settings screen.
 */

public class ActivityAllApps extends Activity {
    private Context mActivityContext = this;

    private ViewGroup mAppsContainer;
    private Spinner mAppFilterDropdown;
    private String mSelectedOption;
    private boolean mIsNotDefaultSelection = false;

    public static final String INTENT_KEY_SELECTED_FILTER = "selectedFilter",
                               SELECTED_ALL_APPS = "allApps",
                               SELECTED_RECENTLY_INSTALLED_APPS = "recentlyInstalledApps";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_apps);

        mAppsContainer = findViewById(R.id.all_apps_container);
        mAppFilterDropdown = findViewById(R.id.all_apps_filter);
        mAppFilterDropdown.setOnItemSelectedListener(handleDropdownSelection());

        ImageView dropdownIcon = findViewById(R.id.all_apps_filter_dropdown);
        dropdownIcon.setOnClickListener(expandSpinner());

        if(getIntent() != null) {
            mSelectedOption = getIntent().getStringExtra(INTENT_KEY_SELECTED_FILTER);
        }

        initializeToolbarWithMenu();
    }

    @Override public void onStart() { super.onStart(); }

    @Override public void onResume() {
        super.onResume();
        mAppsContainer.removeAllViews();

        if(mSelectedOption.equalsIgnoreCase(SELECTED_ALL_APPS)) {
            mAppFilterDropdown.setSelection(0);
            displayAllApps();
        } else if(mSelectedOption.equalsIgnoreCase(SELECTED_RECENTLY_INSTALLED_APPS)) {
            mAppFilterDropdown.setSelection(1);
            displayRecentlyInstalledApps();
        }
    }

    @Override public void onPause() { super.onPause(); }
    @Override public void onStop() { super.onStop(); }
    @Override public void onDestroy() { super.onDestroy(); }

    private AdapterView.OnItemSelectedListener handleDropdownSelection() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mIsNotDefaultSelection) {
                    mAppsContainer.removeAllViews();

                    if (id == 0) { displayAllApps(); }
                    if (id == 1) { displayRecentlyInstalledApps(); }
                } else {
                    mIsNotDefaultSelection = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
    }

    private View.OnClickListener expandSpinner() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppFilterDropdown.performClick();
            }
        };
    }

    private void displayAllApps() {
        DataRepository.getInstance()
                      .requestAllApps()
                      .thenAccept(renderAppCards());
    }

    private void displayRecentlyInstalledApps() {
        DataRepository.getInstance()
                      .requestRecentlyInstalledApps()
                      .thenAccept(renderAppCards());
    }

    private Consumer<List<W4PData>> renderAppCards() {
        return new Consumer<List<W4PData>>() {
            @Override
            public void accept(final List<W4PData> apps) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(apps.size() > 0) {
                            for(W4PData app : apps) {
                                AppCard.builder(mActivityContext)
                                        .setApp(app)
                                        .attachTo(mAppsContainer);
                            }
                        } else {
                            displayNoApps();
                        }
                    }
                });
            }
        };
    }

    private void displayNoApps() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView noApps = new TextView(mActivityContext);
                noApps.setText(R.string.filter_does_not_match);
                noApps.setTextAppearance(R.style.text_medium);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(100, 25, 100, 25);

                noApps.setLayoutParams(params);
                mAppsContainer.addView(noApps);
            }
        });
    }

    private void initializeToolbarWithMenu() {
        Toolbar toolbar = findViewById(R.id.action_bar);
        setActionBar(toolbar);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setTitle(R.string.apps);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}