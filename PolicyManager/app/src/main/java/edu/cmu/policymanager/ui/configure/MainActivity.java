package edu.cmu.policymanager.ui.configure;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.cards.homescreen.CategoryCard;
import edu.cmu.policymanager.ui.configure.cards.homescreen.GlobalSettingCategoryCard;
import edu.cmu.policymanager.ui.configure.globalsettings.ActivityGlobalSettings;
import edu.cmu.policymanager.ui.notification.PolicyManagerNotificationService;
import edu.cmu.policymanager.ui.common.UIPlugin;
import edu.cmu.policymanager.ui.configure.profiles.ActivityPolicyProfileMain;
import edu.cmu.policymanager.viewmodel.W4PData;
import edu.cmu.policymanager.viewmodel.W4PGraph;

/**
 * Presents the user with different permission settings options, and keeps
 * them informed of privacy on their device.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 12/18/2018.
 */

public class MainActivity extends Activity implements UIPlugin {
    private final Context mActivityContext = this;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private boolean mDrawerIsOpen = false;

    private LinearLayout mTopPermissionCards,
                         mBottomPermissionCards,
                         mCategoryCardContainer;

    private TextView mViewAllApps, mNoAppsMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        mTopPermissionCards = findViewById(R.id.homescreen_top_cards);
        mBottomPermissionCards = findViewById(R.id.homescreen_bottom_cards);
        mCategoryCardContainer = findViewById(R.id.category_card_container);
        mViewAllApps = findViewById(R.id.view_all_apps_link);
        mViewAllApps.setVisibility(View.GONE);

        mNoAppsMessage = createNoAppsMessage();

        GlobalSettingCategoryCard.createCalendar(mActivityContext, mTopPermissionCards);
        GlobalSettingCategoryCard.createLocation(mActivityContext, mTopPermissionCards);
        GlobalSettingCategoryCard.createContacts(mActivityContext, mTopPermissionCards);
        GlobalSettingCategoryCard.createCallLogs(mActivityContext, mTopPermissionCards);
        GlobalSettingCategoryCard.createSensor(mActivityContext, mTopPermissionCards);
        GlobalSettingCategoryCard.createMicrophone(mActivityContext, mBottomPermissionCards);
        GlobalSettingCategoryCard.createCamera(mActivityContext, mBottomPermissionCards);
        GlobalSettingCategoryCard.createStorage(mActivityContext, mBottomPermissionCards);
        GlobalSettingCategoryCard.createSMS(mActivityContext, mBottomPermissionCards);

        initializeToolbarWithMenu();
    }

    @Override
    public void onStart() { super.onStart(); }

    @Override
    public void onResume() {
        super.onResume();
        mCategoryCardContainer.removeAllViews();
        mCategoryCardContainer.addView(mNoAppsMessage);
        mNoAppsMessage.setVisibility(View.VISIBLE);
        mViewAllApps.setVisibility(View.GONE);

        String recentlyInstalled = "Recently Installed",
               allApps = "All Apps";

        DataRepository.getInstance()
                      .requestRecentlyInstalledApps()
                      .thenAccept(renderAppCards(recentlyInstalled));

        DataRepository.getInstance()
                      .requestAllApps()
                      .thenAccept(renderAppCards(allApps));

        setProfileInMenu(getActiveProfile());
    }

    @Override public void onPause() { super.onPause(); }
    @Override public void onStop() { super.onStop(); }
    @Override public void onDestroy() { super.onDestroy(); }

    private Consumer<List<W4PData>> renderAppCards(final String category) {
        return new Consumer<List<W4PData>>() {
            @Override
            public void accept(final List<W4PData> w4PData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(w4PData.size() > 0) {
                            mNoAppsMessage.setVisibility(View.GONE);
                            mViewAllApps.setVisibility(View.VISIBLE);
                            addCard(w4PData, category);
                        }
                    }
                });
            }
        };
    }

    private void addCard(List<W4PData> cardData,
                         String category) {
        CategoryCard.builder(mActivityContext)
                    .setCategory(category)
                    .setApps(cardData)
                    .attachTo(mCategoryCardContainer);
    }

    private TextView createNoAppsMessage() {
        TextView noApps = new TextView(mActivityContext);
        noApps.setText(R.string.no_apps_installed);
        noApps.setTextAppearance(R.style.text_medium);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 25, 0, 25);

        noApps.setLayoutParams(params);
        return noApps;
    }

    public Class getUI() { return MainActivity.class; }

    public void viewAllApps(View v) {
        Intent allApps = new Intent(mActivityContext, ActivityAllApps.class);
        allApps.putExtra(
                ActivityAllApps.INTENT_KEY_SELECTED_FILTER,
                ActivityAllApps.SELECTED_ALL_APPS
        );

        mActivityContext.startActivity(allApps);
    }

    public void viewAllGlobalSettings(View v) {
        Intent globalSettings = new Intent(mActivityContext, ActivityGlobalSettings.class);
        mActivityContext.startActivity(globalSettings);
    }

    public void setProfileInMenu(String profileName) {
        Menu menu = mNavView.getMenu();
        MenuItem item = menu.findItem(R.id.common_ui_profiles);
        item.setTitle("Privacy Mode: " + profileName);
    }

    public void initializeToolbarWithMenu() {
        Toolbar toolbar = findViewById(R.id.action_bar);
        setActionBar(toolbar);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        actionBar.setTitle(R.string.privacy_manager);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavView = findViewById(R.id.nav_view);

        addClickListenerToNav();

        setProfileInMenu(getActiveProfile());
    }

    private String getActiveProfile() {
        String policyProfile = PolicyManager.getInstance().getActivePolicyProfile().toString();

        if(policyProfile.equalsIgnoreCase(PolicyProfile.DEFAULT)) {
            return "Personal";
        }

        return "Organizational";
    }

    private void addClickListenerToNav() {
        mNavView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean selectionConfiguresProfiles = item.getGroupId() == R.id.common_menu_options;

                if(selectionConfiguresProfiles) {
                    Intent openProfileUI =
                            new Intent(mActivityContext, ActivityPolicyProfileMain.class);
                    mActivityContext.startActivity(openProfileUI);
                }

                mDrawerLayout.closeDrawer(GravityCompat.START);
                mDrawerLayout.setVisibility(View.GONE);
                mDrawerIsOpen = false;

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (mDrawerIsOpen) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    mDrawerLayout.setVisibility(View.GONE);
                } else {
                    mDrawerLayout.setVisibility(View.VISIBLE);
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }

                mDrawerIsOpen = !mDrawerIsOpen;

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}