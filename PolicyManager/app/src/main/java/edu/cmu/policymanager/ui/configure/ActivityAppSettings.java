package edu.cmu.policymanager.ui.configure;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.Util;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.common.PolicyControlErrorCard;
import edu.cmu.policymanager.ui.common.UIPlugin;
import edu.cmu.policymanager.ui.configure.cards.appsetting.NoSettingsCard;
import edu.cmu.policymanager.ui.configure.cards.appsetting.PermissionPurposeCard;
import edu.cmu.policymanager.util.PolicyManagerDebug;
import edu.cmu.policymanager.viewmodel.W4PGraph;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 12/27/2018.
 *
 * Provides the user with a complete set of controls to configure their privacy settings
 * when an app is installed (when an app policy is present) or after (in the configure UI).
 */

public class ActivityAppSettings extends Activity implements UIPlugin {
    private final Context mActivityContext = this;

    private String mPackageName, mAppName;

    public static final String INTENT_KEY_PACKAGE_NAME = "packageName";

    private Map<String, PermissionPurposeCard> cards = new HashMap<String, PermissionPurposeCard>();

    private LinearLayout mInternalUseContainer, mThirdPartyUseContainer;
    private TextView mDescription, mAppCategory;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        initPackageNameViaInstallOrInAppNavigation();

        mDescription = findViewById(R.id.app_settings_internal_use_description);
        mAppCategory = findViewById(R.id.app_settings_app_category);
        mInternalUseContainer = findViewById(R.id.app_settings_internal_use_container);
        mThirdPartyUseContainer = findViewById(R.id.app_settings_third_party_container);
    }

    private void initPackageNameViaInstallOrInAppNavigation() {
        String lastInstalledPackage = DataRepository.getInstallInfo().packageName;
        mPackageName = lastInstalledPackage;

        String intentData = getIntent().getStringExtra(INTENT_KEY_PACKAGE_NAME);

        if(intentData != null) {
            mPackageName = intentData;
            initializeToolbarWithMenu();
        } else {
            findViewById(R.id.action_bar).setVisibility(View.GONE);
            findViewById(R.id.install_ui_policy_setting_explanation).setVisibility(View.VISIBLE);
            findViewById(R.id.install_ui_finish_container).setVisibility(View.VISIBLE);
        }
    }

    public void onStart() {
        super.onStart();

        mAppName = Util.getAppCommonName(mActivityContext, mPackageName);

        String descriptionContents =
                getResources().getString(R.string.internal_use_description, mAppName, mAppName);

        mDescription.setText(descriptionContents);
        mAppCategory.setText(R.string.peandroid_app);
    }

    public void onResume() {
        super.onResume();
        mInternalUseContainer.removeAllViews();
        mThirdPartyUseContainer.removeAllViews();
        cards = new HashMap<String, PermissionPurposeCard>();

        showAppSettingsDescription(true);

        DataRepository.getInstance()
                      .requestAppGraph(mPackageName)
                      .thenAccept(renderApp());

        PolicyManager.getInstance()
                     .getPoliciesForApp(mPackageName)
                     .exceptionally(displayErrorCards())
                     .thenAccept(renderSettings());
    }

    public void onPause() { super.onPause(); }
    public void onStop() { super.onStop(); }
    public void onDestroy() { super.onDestroy(); }

    private Consumer<W4PGraph> renderApp() {
        return new Consumer<W4PGraph>() {
            @Override
            public void accept(final W4PGraph appGraph) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView appName = findViewById(R.id.app_settings_app_name);
                        ImageView appIcon = findViewById(R.id.app_settings_app_icon);

                        appName.setText(appGraph.getW4PData().getDisplayName());
                        appGraph.getW4PData().getIcon().addIconToView(appIcon);
                    }
                });
            }
        };
    }

    private Function<Throwable, List<UserPolicy>> displayErrorCards() {
        return new Function<Throwable, List<UserPolicy>>() {
            @Override
            public List<UserPolicy> apply(Throwable throwable) {
                PolicyManagerDebug.logException(throwable);
                PolicyControlErrorCard.builder(mActivityContext).attachTo(mInternalUseContainer);
                PolicyControlErrorCard.builder(mActivityContext).attachTo(mThirdPartyUseContainer);
                return new LinkedList<UserPolicy>();
            }
        };
    }

    private void showAppSettingsDescription(boolean isVisible) {
        if(isVisible) {
            findViewById(R.id.app_settings_app_description).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.app_settings_app_description).setVisibility(View.GONE);
        }
    }

    private Consumer<List<UserPolicy>> renderSettings() {
        return new Consumer<List<UserPolicy>>() {
            @Override
            public void accept(final List<UserPolicy> policies) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Set<String> cardsCreated = new HashSet<String>(),
                                    librariesAdded = new HashSet<String>();

                        if(policies.size() == 0) {
                            showAppSettingsDescription(false);
                        }

                        for(UserPolicy policy : policies) {
                            if (notAGlobalControl(policy)) {
                                String key = generateCardKey(policy);
                                ViewGroup container = getCardContainer(policy.purpose);

                                if (!cardsCreated.contains(key)) {
                                    createNewCard(key, policy, container);
                                    cardsCreated.add(key);
                                }

                                if(isNotIndividualLibraryControl(policy.thirdPartyLibrary)) {
                                    addPurposeControl(key, policy.purpose);
                                } else {
                                    String permission =
                                            policy.permission.androidPermission.toString();
                                    String purpose = policy.purpose.name.toString();

                                    String purposeKey = permission + purpose;

                                    if(!librariesAdded.contains(purposeKey)) {
                                        addPurposeControl(key, policy.purpose);
                                    }

                                    librariesAdded.add(purposeKey);
                                }
                            }
                        }

                        displayNoInternalUsagesIfNecessary();
                        displayNoThirdPartyUsagesIfNecessary();
                    }
                });
            }
        };
    }

    private boolean isNotIndividualLibraryControl(ThirdPartyLibrary library) {
        if(library == null) { return true; }

        return library.equals(ThirdPartyLibraries.CATEGORY_THIRD_PARTY_USE) ||
               library.equals(ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE);
    }

    private void createNewCard(String key,
                               UserPolicy policy,
                               ViewGroup container) {
        String category = policy.thirdPartyLibrary.category;

        PermissionPurposeCard card = PermissionPurposeCard.builder(mActivityContext)
                                                          .app(policy.app)
                                                          .permission(policy.permission)
                                                          .category(category)
                                                          .attachTo(container);

        cards.putIfAbsent(key, card);
    }

    private void addPurposeControl(String key,
                                   Purpose purpose) {
        PermissionPurposeCard card = cards.get(key);
        card.addPurposeControl(purpose);
    }

    private ViewGroup getCardContainer(Purpose purpose) {
        if(Purposes.isThirdPartyUse(purpose)) {
            return mThirdPartyUseContainer;
        }

        return mInternalUseContainer;
    }

    private void displayNoInternalUsagesIfNecessary() {
        if(mInternalUseContainer.getChildCount() == 0) {
            NoSettingsCard.builder(mActivityContext)
                          .setAppName(mAppName)
                          .isAppInternalCategory()
                          .attachTo(mInternalUseContainer);
        }
    }

    private void displayNoThirdPartyUsagesIfNecessary() {
        if(mThirdPartyUseContainer.getChildCount() == 0) {
            NoSettingsCard.builder(mActivityContext)
                          .setAppName(mAppName)
                          .isThirdPartyCategory()
                          .attachTo(mThirdPartyUseContainer);
        }
    }

    private String generateCardKey(UserPolicy policy) {
        String category = ThirdPartyLibraries.APP_INTERNAL_USE;

        if(Purposes.isThirdPartyUse(policy.purpose)) {
            category = ThirdPartyLibraries.THIRD_PARTY_USE;
        }

        return policy.permission.androidPermission + category;
    }

    private void initializeToolbarWithMenu() {
        Toolbar toolbar = findViewById(R.id.action_bar);
        setActionBar(toolbar);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setTitle(R.string.app_settings);
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

    private boolean notAGlobalControl(UserPolicy policy) {
        if(policy.thirdPartyLibrary == null && !policy.purpose.equals(Purposes.ALL)) {
            return true;
        }

        return !policy.thirdPartyLibrary.equals(ThirdPartyLibraries.CATEGORY_THIRD_PARTY_USE) &&
               !policy.thirdPartyLibrary.equals(ThirdPartyLibraries.ALL) &&
               !policy.purpose.equals(Purposes.ALL);
    }

    public void finishInstall(View v) {
        finishAndRemoveTask();
    }

    public Class getUI() { return ActivityAppSettings.class; }
}