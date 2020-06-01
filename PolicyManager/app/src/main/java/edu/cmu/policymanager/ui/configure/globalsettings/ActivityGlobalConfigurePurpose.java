package edu.cmu.policymanager.ui.configure.globalsettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.common.ConfigureSwitch;
import edu.cmu.policymanager.ui.common.datastructures.ConsistentStateTree;
import edu.cmu.policymanager.ui.common.functions.UIFunctions;
import edu.cmu.policymanager.ui.configure.cards.globalsetting.GroupCardPermissionOption;
import edu.cmu.policymanager.ui.configure.cards.globalsetting.GlobalSettingPurposeControl;
import edu.cmu.policymanager.ui.configure.cards.globalsetting.NoGlobalSettingsCard;

/**
 * Presents the user with a list of all purposes the selected permission
 * may be used for, and policy controls for each. Each purpose card can also
 * be expanded to show further configuration options such as third party use.
 *
 * Created by Mike Czapik (Carnegie Mellon University)
 * */
public class ActivityGlobalConfigurePurpose extends Activity {
    private Context mActivityContext = this;

    private ConfigureSwitch mMasterPermissionSwitch;
    private SensitiveData mAndroidPermission;
    private NoGlobalSettingsCard mNoSettingsCard;
    private ConsistentStateTree mControlStateTree;

    private ViewGroup mPurposeConfigureCardContainer;
    private ImageView mPermissionIcon;
    private TextView mPermissionDescription,
                     mScreenHeader,
                     mAllDescription,
                     mIndividualSettingsSubtext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_configure_purpose);

        mPermissionIcon = findViewById(R.id.global_settings_permission_icon);
        mPurposeConfigureCardContainer =
                findViewById(R.id.allysiqi_configure_permission_card_container);

        mPermissionDescription = findViewById(R.id.global_settings_permission_description);
        mMasterPermissionSwitch = findViewById(R.id.global_setting_master_permission_switch);
        mScreenHeader = findViewById(R.id.global_settings_permission_title);
        mAllDescription = findViewById(R.id.global_setting_configure_all_apps_description);

        mIndividualSettingsSubtext =
                findViewById(R.id.global_setting_configure_purpose_description);
    }

    private void renderPurposeConfigCards() {
        List<Purpose> purposesInAlphabeticalOrder = sortPurposes(mAndroidPermission.purposes);

        for(Purpose purpose : purposesInAlphabeticalOrder) {
            GlobalSettingPurposeControl.builder(mActivityContext)
                                       .setPurpose(purpose)
                                       .setPermission(mAndroidPermission)
                                       .setControlStateTree(mControlStateTree)
                                       .hideCardOnVisualization(mNoSettingsCard)
                                       .attachTo(mPurposeConfigureCardContainer);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        String displayPermission =
                getIntent().getStringExtra(GroupCardPermissionOption.DISPLAY_PERMISSION_KEY);

        String configureAllDescription =
                getResources().getString(
                        R.string.configure_all_description,
                        displayPermission.toLowerCase()
                );

        String configurePurposeDescription =
                getResources().getString(
                        R.string.configure_based_on_purpose_description,
                        displayPermission.toLowerCase()
                );

        mAndroidPermission =
                getIntent().getParcelableExtra(GroupCardPermissionOption.PERMISSION_KEY);

        mPermissionDescription.setText(mAndroidPermission.description);
        mAllDescription.setText(configureAllDescription);
        mIndividualSettingsSubtext.setText(configurePurposeDescription);

        mControlStateTree = new ConsistentStateTree(mMasterPermissionSwitch);

        mMasterPermissionSwitch.renderAsMasterSwitch();

        mScreenHeader.setText(displayPermission);

        PolicyManagerApplication.ui
                                .getIconManager()
                                .getPermissionIcon(mAndroidPermission)
                                .addIconToView(mPermissionIcon);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPurposeConfigureCardContainer.removeAllViews();

        mNoSettingsCard =
                NoGlobalSettingsCard.builder(mActivityContext)
                                    .setPermissionName(mAndroidPermission.getDisplayPermission())
                                    .attachTo(mPurposeConfigureCardContainer);

        UserPolicy globalPurposeSetting =
                UserPolicy.createGlobalPolicy(
                        mAndroidPermission,
                        Purposes.ALL,
                        ThirdPartyLibraries.ALL
                );

        mMasterPermissionSwitch.setPolicy(globalPurposeSetting);

        PolicyManager.getInstance()
                     .requestEnforcedPolicy(globalPurposeSetting)
                     .thenAccept(UIFunctions.setThumbOnMasterPolicy(
                             globalPurposeSetting,
                             mMasterPermissionSwitch
                     ));

        renderPurposeConfigCards();
    }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onDestroy() { super.onDestroy(); }

    public void goBack(View v) { finish(); }

    private List<Purpose> sortPurposes(final List<Purpose> unsorted) {
        Purpose[] arr = new Purpose[unsorted.size()];

        for(int i = 0; i < arr.length; i++) {
            arr[i] = unsorted.get(i);
        }

        Arrays.sort(arr, new Comparator<Purpose>() {
            @Override
            public int compare(Purpose o1, Purpose o2) {
                String first = o1.name.toString(),
                       second = o2.name.toString();

                return first.compareToIgnoreCase(second);
            }
        });

        List<Purpose> sorted = new ArrayList<Purpose>(unsorted.size());

        for(Purpose purpose : arr) {
            sorted.add(purpose);
        }

        return sorted;
    }
}