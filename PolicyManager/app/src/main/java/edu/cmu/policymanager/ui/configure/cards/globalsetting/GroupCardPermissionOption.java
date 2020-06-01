package edu.cmu.policymanager.ui.configure.cards.globalsetting;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.globalsettings.ActivityGlobalConfigurePurpose;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Allows a user to navigate to a screen to configure all settings for
 * a permission for all apps.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class GroupCardPermissionOption {
    public static final String PERMISSION_KEY = "PERMISSION",
                               DISPLAY_PERMISSION_KEY = "DISPLAY_PERMISSION";

    private GroupCardPermissionOption(final Builder config) {
        View option = LayoutInflater.from(config.mContext)
                                    .inflate(R.layout.template_global_setting_permission_option,
                                             config.mContainer,
                                            false);

        TextView optionText = option.findViewById(R.id.permission_option);
        optionText.setText(config.mDisplayPermission);

        option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent globalPermissionActivity =
                        new Intent(config.mContext, ActivityGlobalConfigurePurpose.class);
                globalPermissionActivity.putExtra(PERMISSION_KEY, config.mPermission);
                globalPermissionActivity.putExtra(DISPLAY_PERMISSION_KEY,
                                                  config.mDisplayPermission);
                config.mContext.startActivity(globalPermissionActivity);
            }
        });

        config.mContainer.addView(option);
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(final Context context) {
        Precondition.checkIfNull(context, "Context cannot be null");
        return new Builder(context);
    }

    /**
     * Builder class for GroupCardPermissionOption.
     * */
    public static class Builder {
        private final Context mContext;
        private SensitiveData mPermission;
        private CharSequence mDisplayPermission;
        private ViewGroup mContainer;

        private static final String sErrorMissingContainer =
                "Must provide a ViewGroup to attach this card to.";

        private Builder(final Context context) { mContext = context; }

        /**
         * Sets the human-readable version of this permission in the navigation
         * option.
         *
         * @param displayPermission the human-readable version of the permission
         * @return this Builder instance
         * */
        public Builder setDisplayPermission(final CharSequence displayPermission) {
            Precondition.checkEmptyCharSequence(displayPermission);

            mDisplayPermission = displayPermission;
            return this;
        }

        /**
         * Set the permission this option will navigate to when pressed. This also
         * will set the display permission, so setting it manually is not required.
         *
         * @param permission the permission to navigate to configure for all apps
         * @return this Builder instance
         * */
        public Builder setPermission(final SensitiveData permission) {
            Precondition.checkIfNull(permission, "Cannot set null permission");

            mPermission = permission;
            mDisplayPermission = mPermission.getDisplayPermission();
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this navigation option
         * @return the GroupCardPermissionOption instance this Builder created
         * */
        public GroupCardPermissionOption attachTo(final ViewGroup container) {
            Precondition.checkIfNull(container, sErrorMissingContainer);
            Precondition.checkState(mPermission != null, "Must set permission");

            mContainer = container;
            return new GroupCardPermissionOption(this);
        }
    }
}