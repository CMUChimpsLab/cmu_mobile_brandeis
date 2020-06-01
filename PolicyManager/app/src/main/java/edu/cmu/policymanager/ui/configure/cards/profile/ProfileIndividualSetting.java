package edu.cmu.policymanager.ui.configure.cards.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Displays the policy profile setting: the permission, its use and the policy that
 * is enforced (which will always be "off" or "deny").
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class ProfileIndividualSetting {
    private ProfileIndividualSetting(Builder config) {
        View layout;
        TextView usedByDisplay, usedForDisplay, settingText, using, usingValue;

        layout = LayoutInflater.from(config.mContext)
                               .inflate(
                                       R.layout.component_profile_setting_display,
                                       config.mContainer,
                                       false
                               );

        usedByDisplay = layout.findViewById(R.id.policy_profile_used_by_value);
        usedForDisplay = layout.findViewById(R.id.policy_profile_used_for_value);
        settingText = layout.findViewById(R.id.allyqian_profile_setting_label);
        using = layout.findViewById(R.id.policy_profile_using);
        usingValue = layout.findViewById(R.id.policy_profile_using_value);

        if(config.mPolicy.thirdPartyLibrary != null) {
            usedByDisplay.setText(config.mPolicy.thirdPartyLibrary.name);
        } else {
            usedByDisplay.setText(ThirdPartyLibraries.APP_INTERNAL_USE);
        }

        usedForDisplay.setText(config.mPolicy.purpose.name);

        if(!config.mPolicy.app.equalsIgnoreCase(PolicyManagerApplication.SYMBOL_ALL)) {
            using.setVisibility(View.VISIBLE);
            usingValue.setVisibility(View.VISIBLE);

            usingValue.setText(config.mPolicy.permission.getDisplayPermission());
        }

        String policyString = (config.mPolicy.isAllowed() ? "On" : "");
        policyString = (config.mPolicy.isDenied() ? "Off" : policyString);
        settingText.setText(policyString);

        config.mContainer.addView(layout);
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(Context context) {
        Precondition.checkIfNull(context, "Cannot create from null context");
        return new Builder(context);
    }

    /**
     * Builder class for ProfileIndividualSetting.
     * */
    public static class Builder {
        private Context mContext;
        private UserPolicy mPolicy;
        private ViewGroup mContainer;

        private Builder(Context context) { mContext = context; }

        /**
         * Set the UserPolicy this profile controls.
         *
         * @param policy the policy set by this profile
         * @return the Builder instance
         * */
        public Builder setPolicy(UserPolicy policy) {
            Precondition.checkIfPolicyIsValid(policy);

            mPolicy = policy;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the ProfileIndividualSetting instance this Builder created
         * */
        public ProfileIndividualSetting attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");
            Precondition.checkState(mPolicy != null, "Need a policy to render");

            mContainer = container;

            return new ProfileIndividualSetting(this);
        }
    }
}