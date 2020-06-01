package edu.cmu.policymanager.ui.configure.cards.appsetting;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.function.Function;

import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.common.ConfigureSwitch;
import edu.cmu.policymanager.ui.common.datastructures.ConsistentStateTree;
import edu.cmu.policymanager.ui.common.functions.UIFunctions;
import edu.cmu.policymanager.ui.configure.ActivityLibrarySettings;
import edu.cmu.policymanager.util.PolicyManagerDebug;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Displays a purpose in a permission control card. If there is just one
 * purpose in a card, then it should expose no policy controls. If the
 * purpose is related to third party use(such as advertising), then it
 * should display an "advanced" button for further configuration.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PurposeControl {
    private final ConfigureSwitch mControl;
    private final Context mContext;
    private final UserPolicy mPolicy;

    private PurposeControl(Builder config) {
        mContext = config.mContext;
        mPolicy = config.mPolicy;

        View purposeControl = LayoutInflater.from(config.mContext)
                                            .inflate(
                                                    R.layout.component_app_setting_purpose_control,
                                                    config.mContainer,
                                                    false
                                            );

        TextView title = purposeControl.findViewById(R.id.app_setting_purpose_control_title),
                 description =
                         purposeControl.findViewById(R.id.app_setting_purpose_control_description),
                 advanced = purposeControl.findViewById(R.id.app_setting_purpose_control_advanced);

        String controlTitle = "For " + mPolicy.purpose.name;
        title.setText(controlTitle);
        description.setText(mPolicy.purpose.description);

        if(Purposes.isThirdPartyUse(mPolicy.purpose)) {
            advanced.setVisibility(View.VISIBLE);
            advanced.setOnClickListener(handlePress());
        }

        mControl = purposeControl.findViewById(R.id.app_setting_purpose_control_switch);
        mControl.setPolicy(mPolicy);

        PolicyManager.getInstance()
                     .requestEnforcedPolicy(mPolicy)
                     .exceptionally(disableControlOnError(mControl))
                     .thenAccept(UIFunctions.setThumbOnPolicy(mControl));

        config.mContainer.addView(purposeControl);
    }

    /**
     * Observe state changes to a master control switch, and update the state
     * of this control accordingly.
     *
     * @param masterSwitch the switch to observe
     * */
    public void observe(ConsistentStateTree masterSwitch) {
        Precondition.checkIfNull(masterSwitch, "Cannot listen on null switch");

        ConsistentStateTree tree = new ConsistentStateTree(mControl);
        mControl.setReferenceToContainingTree(tree);
        masterSwitch.addChild(tree);
    }

    private Function<Throwable, UserPolicy> disableControlOnError(final ConfigureSwitch control) {
        return new Function<Throwable, UserPolicy>() {
            @Override
            public UserPolicy apply(Throwable throwable) {
                PolicyManagerDebug.logException(throwable);
                control.disabledByError();
                return mPolicy;
            }
        };
    }

    private View.OnClickListener handlePress() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                String app = mPolicy.app,
                       purpose = mPolicy.purpose.name.toString(),
                       permission = mPolicy.permission.androidPermission.toString();

                Intent intent = new Intent(mContext, ActivityLibrarySettings.class);
                intent.putExtra(ActivityLibrarySettings.LIBRARY_SETTING_APP, app);
                intent.putExtra(ActivityLibrarySettings.LIBRARY_SETTING_PURPOSE, purpose);
                intent.putExtra(ActivityLibrarySettings.LIBRARY_SETTINGS_PERMISSION, permission);

                mContext.startActivity(intent);
            }
        };
    }

    /**
     * Show the policy control for this purpose control. Cards will want to
     * expose the controls for purposes when there is more than one purpose for
     * that permission.
     * */
    public void showPolicyControl() {
        mControl.setVisibility(View.VISIBLE);
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(Context context) {
        Precondition.checkIfNull(context, "Cannot create builder from null context");
        return new Builder(context);
    }

    /**
     * Builder class for PurposeControl.
     * */
    public static class Builder {
        private final Context mContext;
        private UserPolicy mPolicy;
        private ViewGroup mContainer;

        private Builder(Context context) { mContext = context; }

        /**
         * Set the user policy for this control.
         *
         * @param policy the UserPolicy
         * @return this Builder instance
         * */
        public Builder setPolicy(UserPolicy policy) {
            Precondition.checkIfNull(policy, "Cannot set null policy");

            mPolicy = policy;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the PurposeControl instance this Builder created
         * */
        public PurposeControl attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");
            Precondition.checkState(mPolicy != null, "Must set a policy");
            Precondition.checkUiThread();

            mContainer = container;
            return new PurposeControl(this);
        }
    }
}