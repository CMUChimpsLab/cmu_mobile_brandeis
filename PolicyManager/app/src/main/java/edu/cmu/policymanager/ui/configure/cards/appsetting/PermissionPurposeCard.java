package edu.cmu.policymanager.ui.configure.cards.appsetting;

import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.common.ConfigureSwitch;
import edu.cmu.policymanager.ui.common.datastructures.ConsistentStateTree;
import edu.cmu.policymanager.ui.common.functions.UIFunctions;
import edu.cmu.policymanager.util.PermissionUtil;
import edu.cmu.policymanager.util.PolicyManagerDebug;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Display and expose a control for a dangerous permission this app requests,
 * along with the purpose for that access.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PermissionPurposeCard {
    private final ViewGroup mPurposeContainer;
    private final Context mContext;
    private UserPolicy mAllPurposesPolicy;
    private PurposeControl mFirstPurposeControl;
    private final ConfigureSwitch mTopLevelControl;
    private ConsistentStateTree mControlStateTree;

    private PermissionPurposeCard(Builder config) {
        mAllPurposesPolicy = UserPolicy.createAppPolicy(config.mApp,
                                                        config.mPermission,
                                                        Purposes.ALL,
                                                        config.mCategory);

        mContext = config.mContext;

        View card = LayoutInflater.from(mContext)
                                  .inflate(
                                          R.layout.component_app_setting_permission_control_card,
                                          config.mContainer,
                                          false
                                  );

        mTopLevelControl = card.findViewById(R.id.app_permission_control_switch);

        TextView title = card.findViewById(R.id.app_permission_control_permission_title);
        title.setText(config.mPermission.getDisplayPermission());

        TextView description =
                card.findViewById(R.id.app_permission_control_permission_description);
        description.setText(config.mPermission.description);

        ImageView permissionIcon = card.findViewById(R.id.app_permission_control_permission_icon);

        PolicyManagerApplication.ui
                                .getIconManager()
                                .getPermissionIcon(config.mPermission)
                                .addIconToView(permissionIcon);

        mPurposeContainer = card.findViewById(R.id.app_permission_control_purpose_container);

        mControlStateTree = new ConsistentStateTree(mTopLevelControl);
        mTopLevelControl.setReferenceToContainingTree(mControlStateTree);

        config.mContainer.addView(card);
    }

    /**
     * Adds a purpose for this permission control. If there is more than one purpose,
     * it will expose a purpose control, or just display the purpose otherwise. If this
     * purpose is a third-party purpose such as for advertising, then an "advanced" button
     * will display.
     *
     * @param purpose the purpose for the access of this sensitive data
     * */
    public void addPurposeControl(Purpose purpose) {
        Precondition.checkIfNull(purpose, "Cannot have control for null purpose");

        UserPolicy policy = (UserPolicy)mAllPurposesPolicy.clone();
        policy.purpose = purpose;

        if(mFirstPurposeControl != null) {
            mPurposeContainer.addView(createDivider(mContext));
        }

        PurposeControl control = PurposeControl.builder(mContext)
                                               .setPolicy(policy)
                                               .attachTo(mPurposeContainer);

        if(mFirstPurposeControl == null) {
            mFirstPurposeControl = control;
            mFirstPurposeControl.observe(mControlStateTree);

            if(Purposes.isThirdPartyUse(policy.purpose)) {
                initTopLevelControl(mAllPurposesPolicy);
            } else {
                initTopLevelControl(policy);
            }
        } else {
            initTopLevelControl(mAllPurposesPolicy);
            mFirstPurposeControl.showPolicyControl();
            control.showPolicyControl();
            control.observe(mControlStateTree);
        }
    }

    private void initTopLevelControl(UserPolicy policy) {
        mTopLevelControl.setPolicy(policy);

        Consumer<UserPolicy> renderPolicyControl =
                UIFunctions.setThumbOnPolicy(mTopLevelControl);

        PolicyManager.getInstance()
                     .requestEnforcedPolicy(policy)
                     .exceptionally(disableControlOnError(mTopLevelControl))
                     .thenAccept(renderPolicyControl);
    }

    private Function<Throwable, UserPolicy> disableControlOnError(final ConfigureSwitch control) {
        return new Function<Throwable, UserPolicy>() {
            @Override
            public UserPolicy apply(Throwable throwable) {
                PolicyManagerDebug.logException(throwable);
                control.disabledByError();
                return mAllPurposesPolicy;
            }
        };
    }

    private View createDivider(Context context) {
        ContextThemeWrapper theme = new ContextThemeWrapper(context, R.style.divider);
        View divider = new View(theme, null, 0);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2
        );

        params.setMargins(210, 0, 25, 0);
        divider.setLayoutParams(params);

        return divider;
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(Context context) {
        Precondition.checkIfNull(context, "Context cannot be null");

        return new Builder(context);
    }

    /**
     * Builder class for PermissionPurposeCards.
     * */
    public static class Builder {
        private final Context mContext;
        private CharSequence mApp;
        private SensitiveData mPermission;
        private ThirdPartyLibrary mCategory;
        private ViewGroup mContainer;

        private static final String sErrorMissingContainer =
                "Must provide a ViewGroup to attach this card to.";

        private static final String sErrorMissingParams =
                "Cannot construct component without app, permission or category.";

        private static final String sErrorWrongCategory =
                "Category must either be third party or app internal use.";

        private Builder(Context context) {
            mContext = context;
        }

        /**
         * Set the app's package name for the control switch.
         *
         * @param app the app's package name
         * @return this Builder instance
         * */
        public Builder app(CharSequence app) {
            Precondition.checkEmptyCharSequence(app);

            mApp = app;
            return this;
        }

        /**
         * Set the permission name for the control switch, and for the card's
         * display.
         *
         * @param permission the Android permission being controlled and displayed
         * @return this Builder instance
         * */
        public Builder permission(SensitiveData permission) {
            Precondition.checkIfNull(permission, "Permission cannot be null");

            mPermission = permission;
            return this;
        }

        /**
         * Set the permission category this control belongs to. Valid values are
         * either ThirdPartyLibraries.APP_INTERNAL_USE or ThirdPartyLibraries.THIRD_PARTY_USE.
         *
         * @param category the permission category
         * @return this Builder instance
         * */
        public Builder category(CharSequence category) {
            Precondition.checkEmptyCharSequence(category);

            if(!category.toString().equalsIgnoreCase(ThirdPartyLibraries.THIRD_PARTY_USE) &&
               !category.toString().equalsIgnoreCase(ThirdPartyLibraries.APP_INTERNAL_USE)) {
                throw new IllegalStateException(sErrorWrongCategory + " Its value is " + category);
            }

            if(category.toString().equalsIgnoreCase(ThirdPartyLibraries.APP_INTERNAL_USE)) {
                mCategory = null;
            } else if(category.toString().equalsIgnoreCase(ThirdPartyLibraries.THIRD_PARTY_USE)) {
                mCategory = ThirdPartyLibraries.CATEGORY_THIRD_PARTY_USE;
            }

            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the PermissionPurposeCard instance this Builder created
         * */
        public PermissionPurposeCard attachTo(ViewGroup container) {
            Precondition.checkUiThread();
            Precondition.checkIfNull(container, sErrorMissingContainer);

            boolean appPermissionSet = mApp != null && mPermission != null;
            Precondition.checkState(appPermissionSet, sErrorMissingParams);

            mContainer = container;
            return new PermissionPurposeCard(this);
        }
    }
}