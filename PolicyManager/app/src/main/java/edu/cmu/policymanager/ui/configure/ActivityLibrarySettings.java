package edu.cmu.policymanager.ui.configure;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.common.ConfigureSwitch;
import edu.cmu.policymanager.ui.common.LibraryControl;
import edu.cmu.policymanager.ui.common.datastructures.ConsistentStateTree;
import edu.cmu.policymanager.ui.common.functions.UIFunctions;

/**
 * Configure settings for third-party libraries.
 *
 * Created by Mike Czapik (Carnegie Mellon University)
 */

public class ActivityLibrarySettings extends Activity {
    private Context mActivityContext = this;

    public static final String LIBRARY_SETTINGS_PERMISSION = "permission",
                               LIBRARY_SETTING_PURPOSE = "purpose",
                               LIBRARY_SETTING_APP = "app";

    private String mApp, mPermissionToConfigure, mPurposeToConfigure;
    private ConfigureSwitch mMasterControl;
    private UserPolicy mMasterPolicy;
    private ConsistentStateTree mControlTree;

    private ViewGroup mContainer;
    private ImageView mIcon;
    private TextView mPermissionTitle,
                     mPermissionDescription,
                     mPurposeTitle,
                     mDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_settings);

        mActivityContext = this;

        mApp = getIntent().getStringExtra(LIBRARY_SETTING_APP);
        mPermissionToConfigure = getIntent().getStringExtra(LIBRARY_SETTINGS_PERMISSION);
        mPurposeToConfigure = getIntent().getStringExtra(LIBRARY_SETTING_PURPOSE);

        mMasterControl = findViewById(R.id.library_settings_master_switch);
        mContainer = findViewById(R.id.library_settings_control_container);
        mIcon = findViewById(R.id.library_settings_permission_icon);
        mPermissionTitle = findViewById(R.id.library_settings_permission_title);
        mPermissionDescription = findViewById(R.id.library_settings_permission_description);
        mPurposeTitle = findViewById(R.id.library_settings_purpose_title);
        mDescription = findViewById(R.id.library_settings_purpose_description);
    }

    @Override
    public void onStart() {
        super.onStart();

        String purposeTitleContent =
                getResources().getString(
                        R.string.library_settings_for_purpose,
                        mPurposeToConfigure
                );

        SensitiveData permission = DangerousPermissions.from(mPermissionToConfigure);
        Purpose purpose = Purposes.from(mPurposeToConfigure);

        PolicyManagerApplication.ui
                                .getIconManager()
                                .getPermissionIcon(permission)
                                .addIconToView(mIcon);

        mPermissionDescription.setText(permission.description);
        mPermissionTitle.setText(permission.getDisplayPermission());
        mPurposeTitle.setText(purposeTitleContent);
        mDescription.setText(purpose.description);

        mMasterPolicy = UserPolicy.createAppPolicy(
                mApp,
                permission,
                purpose,
                ThirdPartyLibraries.CATEGORY_THIRD_PARTY_USE
        );

        mMasterControl.setPolicy(mMasterPolicy);
        mMasterControl.renderAsMasterSwitch();
        mControlTree = new ConsistentStateTree(mMasterControl);
    }

    @Override
    public void onResume() {
        super.onResume();
        mContainer.removeAllViews();

        PolicyManager.getInstance().getPoliciesForApp(mApp).thenAccept(renderControls());
        PolicyManager.getInstance()
                     .requestEnforcedPolicy(mMasterPolicy)
                     .thenAccept(UIFunctions.setThumbOnMasterPolicy(mMasterPolicy, mMasterControl));
    }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onDestroy() { super.onDestroy(); }

    private Consumer<List<UserPolicy>> renderControls() {
        return new Consumer<List<UserPolicy>>() {
            @Override
            public void accept(final List<UserPolicy> policies) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<UserPolicy> filtered = filter(policies);

                        for(UserPolicy policy : filtered) {
                            LibraryControl.builder(mActivityContext)
                                          .setPolicy(policy)
                                          .observe(mControlTree)
                                          .attachTo(mContainer);
                        }
                    }
                });
            }
        };
    }

    private List<UserPolicy> filter(List<UserPolicy> list) {
        if(list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Cannot have empty app policies to filter.");
        }

        List<UserPolicy> filtered = new ArrayList<>(5);

        for(UserPolicy item : list) {
            boolean matchesIntentParams =
                    item.permission.androidPermission.toString().equals(mPermissionToConfigure) &&
                    item.purpose.name.toString().equals(mPurposeToConfigure);

            if(matchesIntentParams) {
                if(notAGlobalControl(item)) {
                    filtered.add(item);
                }
            }
        }

        return filtered;
    }

    private boolean notAGlobalControl(UserPolicy policy) {
        return !policy.thirdPartyLibrary.equals(ThirdPartyLibraries.CATEGORY_THIRD_PARTY_USE) &&
               !policy.thirdPartyLibrary.equals(ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE) &&
               !policy.thirdPartyLibrary.equals(ThirdPartyLibraries.ALL);
    }

    public void goBack(View v) {
        finish();
    }
}