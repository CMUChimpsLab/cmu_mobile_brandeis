package edu.cmu.policymanager.ui.configure.globalsettings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.function.Function;

import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
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
import edu.cmu.policymanager.ui.configure.cards.globalsetting.GlobalSettingPurposeControl;
import edu.cmu.policymanager.util.PolicyManagerDebug;

/**
 * Configure global settings for third-party libraries.
 *
 * Created by Mike Czapik (Carnegie Mellon University)
 * */
public class ActivityGlobalConfigureLibraries extends Activity {
    private Context mActivityContext = this;

    private ConfigureSwitch mMasterLibrarySwitch;
    private UserPolicy mMasterLibraryPolicy;
    private ConsistentStateTree mControlTree;

    private TextView mPurposeDescription,
                     mTitle,
                     mAllThirdPartiesDescription,
                     mConfigureIndividualThirdPartyDesc;
    private ImageView mPurposeIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_configure_libraries);

        mPurposeDescription = findViewById(R.id.activity_global_setting_library_purpose_desc);
        mPurposeIcon = findViewById(R.id.activity_global_setting_library_icon);
        mTitle = findViewById(R.id.activity_global_setting_library_title);
        mAllThirdPartiesDescription = findViewById(R.id.activity_global_setting_library_all_desc);
        mConfigureIndividualThirdPartyDesc =
                findViewById(R.id.global_setting_library_config_individually_desc);

        mMasterLibrarySwitch = findViewById(R.id.master_global_library_switch);
    }

    @Override
    public void onStart() {
        super.onStart();

        SensitiveData permission =
                getIntent().getParcelableExtra(GlobalSettingPurposeControl.PERMISSION_KEY);
        Purpose purpose = getIntent().getParcelableExtra(GlobalSettingPurposeControl.PURPOSE_KEY);

        String description = getResources().getString(
                R.string.configure_all_third_parties_description,
                permission.getDisplayPermission().toString().toLowerCase(),
                purpose.name.toString().toLowerCase()
        );

        PolicyManagerApplication.ui
                                .getIconManager()
                                .getPurposeIcon(purpose)
                                .addIconToView(mPurposeIcon);

        mMasterLibraryPolicy = UserPolicy.createGlobalPolicy(
                permission,
                purpose,
                ThirdPartyLibraries.ALL
        );

        mMasterLibrarySwitch.setPolicy(mMasterLibraryPolicy);
        mMasterLibrarySwitch.renderAsMasterSwitch();
        mControlTree = new ConsistentStateTree(mMasterLibrarySwitch);

        mPurposeDescription.setText(purpose.description);
        mTitle.setText(purpose.name);
        mAllThirdPartiesDescription.setText(description);
        mConfigureIndividualThirdPartyDesc.setText(description);
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewGroup container = findViewById(R.id.global_setting_library_container);
        container.removeAllViews();

        PolicyManager.getInstance()
                     .requestEnforcedPolicy(mMasterLibraryPolicy)
                     .exceptionally(disableControlOnError(mMasterLibrarySwitch))
                     .thenAcceptAsync(UIFunctions.setThumbOnMasterPolicy(
                             mMasterLibraryPolicy,
                             mMasterLibrarySwitch
                     ));

        renderLibraryCards();
    }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onDestroy() { super.onDestroy(); }

    private Function<Throwable, UserPolicy> disableControlOnError(final ConfigureSwitch control) {
        return new Function<Throwable, UserPolicy>() {
            @Override
            public UserPolicy apply(Throwable throwable) {
                PolicyManagerDebug.logException(throwable);
                control.disabledByError();
                return mMasterLibraryPolicy;
            }
        };
    }

    private void renderLibraryCards() {
        ViewGroup container = findViewById(R.id.global_setting_library_container);
        SensitiveData permission =
                getIntent().getParcelableExtra(GlobalSettingPurposeControl.PERMISSION_KEY);
        Purpose purpose = getIntent().getParcelableExtra(GlobalSettingPurposeControl.PURPOSE_KEY);

        for(ThirdPartyLibrary library : ThirdPartyLibraries.AS_LIST) {
            if(library.purpose.equals(purpose)) {
                UserPolicy libraryPolicy = UserPolicy.createGlobalPolicy(
                        permission,
                        purpose,
                        library
                );

                LibraryControl.builder(mActivityContext)
                              .setPolicy(libraryPolicy)
                              .observe(mControlTree)
                              .attachTo(container);
            }
        }
    }

    public void goBack(View v) { finish(); }
}