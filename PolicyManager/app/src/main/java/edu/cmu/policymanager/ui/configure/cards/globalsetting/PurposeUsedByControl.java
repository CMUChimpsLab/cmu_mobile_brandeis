package edu.cmu.policymanager.ui.configure.cards.globalsetting;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.function.Function;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.common.ConfigureSwitch;
import edu.cmu.policymanager.ui.common.datastructures.ConsistentStateTree;
import edu.cmu.policymanager.ui.common.functions.UIFunctions;
import edu.cmu.policymanager.util.PolicyManagerDebug;
import edu.cmu.policymanager.validation.Precondition;
import edu.cmu.policymanager.viewmodel.W4PData;

/**
 * Displays a control for an app that uses this specific purpose.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PurposeUsedByControl {
    private View mDivider;
    private UserPolicy mErrorDefault;

    private PurposeUsedByControl(Builder config) {
        View settingTemplate = LayoutInflater.from(config.context)
                                             .inflate(
                                                     R.layout.template_used_by_global_settings,
                                                     config.mContainer,
                                                     false
                                             );

        mDivider = settingTemplate.findViewById(R.id.used_by_divider);

        if(config.mShowDivider) {
            showDivider();
        } else {
            hideDivider();
        }

        TextView title = settingTemplate.findViewById(R.id.global_setting_usedby_title);
        title.setText(config.mTitle);

        UserPolicy controlPolicy = UserPolicy.createGlobalPolicy(
                config.mPermission,
                config.mPurpose,
                config.mCategory
        );

        if(config.mApp != null) {
            ImageView appIcon = settingTemplate.findViewById(R.id.global_setting_usedby_app_icon);
            appIcon.setVisibility(View.VISIBLE);
            config.mApp.getIcon().addIconToView(appIcon);
            controlPolicy.app = config.mApp.getAndroidSystemName();
        }

        mErrorDefault = controlPolicy;

        ConfigureSwitch control = settingTemplate.findViewById(R.id.used_by_configure_switch);
        control.setPolicy(controlPolicy);

        if(config.mStateTree != null) {
            ConsistentStateTree stateTree = new ConsistentStateTree(control);
            config.mStateTree.addChild(stateTree);
        }

        config.mContainer.addView(settingTemplate);

        config.mContainer.getViewTreeObserver()
                         .addOnGlobalLayoutListener(
                                 renderControlWhenInView(control, controlPolicy)
                         );
    }

    protected void showDivider() {
        mDivider.setVisibility(View.VISIBLE);
    }

    protected void hideDivider() {
        mDivider.setVisibility(View.GONE);
    }

    private ViewTreeObserver.OnGlobalLayoutListener renderControlWhenInView(
            final ConfigureSwitch control,
            final UserPolicy policy
    ) {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                PolicyManager.getInstance()
                             .requestEnforcedPolicy(policy)
                             .exceptionally(disableControlOnError(control))
                             .thenAccept(UIFunctions.setThumbOnPolicy(control));
            }
        };
    }

    private Function<Throwable, UserPolicy> disableControlOnError(final ConfigureSwitch control) {
        return new Function<Throwable, UserPolicy>() {
            @Override
            public UserPolicy apply(Throwable throwable) {
                PolicyManagerDebug.logException(throwable);
                control.disabledByError();
                return mErrorDefault;
            }
        };
    }

    protected static Builder builder(Context context) {
        Precondition.checkIfNull(context, "Cannot create with null context");
        return new Builder(context);
    }

    protected static class Builder {
        private final Context context;
        private ViewGroup mContainer;
        private SensitiveData mPermission;
        private ThirdPartyLibrary mCategory;
        private Purpose mPurpose;
        private ConsistentStateTree mStateTree;
        private CharSequence mTitle;
        private W4PData mApp;
        private boolean mShowDivider;

        protected Builder(Context context) { this.context = context; }

        protected Builder setPermission(SensitiveData permission) {
            Precondition.checkIfNull(permission, "Cannot set null permission");

            mPermission = permission;
            return this;
        }

        protected Builder setPurpose(Purpose purpose) {
            Precondition.checkIfNull(purpose, "Cannot have null purpose");

            mPurpose = purpose;
            return this;
        }

        protected Builder showDivider() {
            mShowDivider = true;
            return this;
        }

        protected Builder hideDivider() {
            mShowDivider = false;
            return this;
        }

        protected Builder setCategory(ThirdPartyLibrary category) {
            Precondition.checkIfNull(category, "Category cannot be null");

            mCategory = category;
            return this;
        }

        protected Builder setStateTree(ConsistentStateTree stateTree) {
            Precondition.checkIfNull(stateTree, "State tree cannot be null");

            mStateTree = stateTree;
            return this;
        }

        protected Builder setTitle(CharSequence title) {
            Precondition.checkEmptyCharSequence(title);

            mTitle = title;
            return this;
        }

        protected Builder setApp(W4PData app) {
            Precondition.checkIfNull(app, "Cannot set null app");

            this.mApp = app;
            return this;
        }

        protected PurposeUsedByControl attachTo(ViewGroup container) {
            Precondition.checkUiThread();
            Precondition.checkIfNull(container, "Cannot attach to empty container");

            Precondition.checkState(mTitle != null, "Control must have title");
            Precondition.checkState(mPermission != null, "Must set permission");
            Precondition.checkState(mPurpose != null, "Must set purpose");
            Precondition.checkState(mCategory != null, "Must set category");

            mContainer = container;
            return new PurposeUsedByControl(this);
        }
    }
}