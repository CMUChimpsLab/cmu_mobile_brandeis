package edu.cmu.policymanager.ui.configure.cards.globalsetting;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import edu.cmu.policymanager.DataRepository.DataRepository;
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
import edu.cmu.policymanager.ui.configure.globalsettings.ActivityGlobalConfigureLibraries;
import edu.cmu.policymanager.util.PolicyManagerDebug;
import edu.cmu.policymanager.validation.Precondition;
import edu.cmu.policymanager.viewmodel.W4PData;

/**
 * Controls a global policy for a permission and specific purpose. Can expand into
 * additional controls for individual apps, or third party controls/link.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class GlobalSettingPurposeControl {
    private View mCard;
    private final LinearLayout mUsedByDrawer, mUsedByContainer;
    private ImageView mExpandIcon, mCollapseIcon;
    private ConfigureSwitch mPurposeControl;
    private UserPolicy mPermissionPurposePolicy;
    private ConsistentStateTree mPurposeControlStateTree;
    private boolean mDrawerIsVisible = false;

    public static final String PURPOSE_KEY = "PURPOSE",
                               PERMISSION_KEY = "PERMISSION";

    private GlobalSettingPurposeControl(Builder config) {
        mCard = LayoutInflater.from(config.mContext)
                              .inflate(
                                      R.layout.template_global_setting_purpose_card,
                                      config.mContainer,
                                      false
                              );

        TextView advancedButton = mCard.findViewById(R.id.global_settings_purpose_card_advanced);
        mExpandIcon = mCard.findViewById(R.id.global_setting_purpose_card_usedby_expand);
        mCollapseIcon = mCard.findViewById(R.id.global_setting_purpose_card_usedby_collapse);
        mUsedByDrawer = mCard.findViewById(R.id.global_setting_used_by_drawer);
        mUsedByContainer = mCard.findViewById(R.id.global_setting_used_by_container);
        mPurposeControl = mCard.findViewById(R.id.global_setting_purpose_switch);

        TextView purposeDescription = mCard.findViewById(R.id.global_setting_purpose_description);
        purposeDescription.setText(config.mPurpose.description);

        mPermissionPurposePolicy =
                UserPolicy.createGlobalPolicy(
                        config.mPermission,
                        config.mPurpose,
                        ThirdPartyLibraries.ALL
                );

        mPurposeControl.setPolicy(mPermissionPurposePolicy);

        TextView purposeText = mCard.findViewById(R.id.global_setting_purpose_card_title);
        purposeText.setText(config.mPurpose.name);

        ImageView iconView = mCard.findViewById(R.id.global_setting_purpose_card_icon);

        PolicyManagerApplication.ui
                                .getIconManager()
                                .getPurposeIcon(config.mPurpose)
                                .addIconToView(iconView);

        mCard.setOnClickListener(handleDrawerVisibility());

        mPurposeControlStateTree = new ConsistentStateTree(mPurposeControl);
        mPurposeControl.setReferenceToContainingTree(mPurposeControlStateTree);

        if(config.mStateTree != null) {
            config.mStateTree.addChild(mPurposeControlStateTree);
        }

        if(Purposes.isThirdPartyUse(config.mPurpose)) {
            advancedButton.setVisibility(View.VISIBLE);
            advancedButton.setOnClickListener(navToLibraryControls(config));

            config.mNoSettingCard.remove();

            PurposeUsedByControl.builder(config.mContext)
                                .setPermission(config.mPermission)
                                .setPurpose(config.mPurpose)
                                .setStateTree(mPurposeControlStateTree)
                                .setCategory(ThirdPartyLibraries.CATEGORY_THIRD_PARTY_USE)
                                .setTitle("Third Parties")
                                .attachTo(mUsedByContainer);
        } else {
            DataRepository.getInstance()
                          .requestAppsUsingPermissionAndPurpose(
                                  config.mPermission,
                                  config.mPurpose
                          )
                          .thenAccept(renderAppControls(config));
        }

        PolicyManager.getInstance()
                     .requestEnforcedPolicy(mPermissionPurposePolicy)
                     .exceptionally(disableControlOnError(mPurposeControl))
                     .thenAccept(UIFunctions.setThumbOnPolicy(mPurposeControl));

        config.mContainer.addView(mCard);
    }

    private Function<Throwable, UserPolicy> disableControlOnError(final ConfigureSwitch control) {
        return new Function<Throwable, UserPolicy>() {
            @Override
            public UserPolicy apply(Throwable throwable) {
                PolicyManagerDebug.logException(throwable);
                control.disabledByError();
                return mPermissionPurposePolicy;
            }
        };
    }

    private Consumer<List<W4PData>> renderAppControls(final Builder config) {
        return new Consumer<List<W4PData>>() {
            @Override
            public void accept(final List<W4PData> apps) {
                if(apps == null || apps.isEmpty() || apps.size() == 1) {
                    mCard.setVisibility(View.GONE);
                    config.mStateTree.removeStateTree(mPurposeControlStateTree);
                } else {
                    mUsedByContainer.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    config.mNoSettingCard.remove();
                                    PurposeUsedByControl lastControl = null;

                                    for(W4PData app : apps) {
                                        String all = PolicyManagerApplication.SYMBOL_ALL;

                                        if(!app.getAndroidSystemName().equalsIgnoreCase(all)) {
                                            lastControl = createPurposeControlForApp(app, config);
                                        }
                                    }

                                    if(lastControl != null) {
                                        lastControl.hideDivider();
                                    }
                                }
                            }
                    );
                }
            }
        };
    }

    private PurposeUsedByControl createPurposeControlForApp(W4PData app,
                                                            Builder config) {
        return PurposeUsedByControl.builder(config.mContext)
                                   .setPermission(config.mPermission)
                                   .setPurpose(config.mPurpose)
                                   .setStateTree(mPurposeControlStateTree)
                                   .setCategory(ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE)
                                   .setTitle(app.getDisplayName())
                                   .setApp(app)
                                   .showDivider()
                                   .attachTo(mUsedByContainer);
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(Context context) {
        Precondition.checkIfNull(context, "Cannot create with null context");
        return new Builder(context);
    }

    private View.OnClickListener handleDrawerVisibility() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDrawerIsVisible) {
                    mUsedByDrawer.setVisibility(View.GONE);
                    mCollapseIcon.setVisibility(View.GONE);
                    mExpandIcon.setVisibility(View.VISIBLE);
                } else {
                    mUsedByDrawer.setVisibility(View.VISIBLE);
                    mCollapseIcon.setVisibility(View.VISIBLE);
                    mExpandIcon.setVisibility(View.GONE);
                }

                mDrawerIsVisible = !mDrawerIsVisible;
            }
        };
    }

    private View.OnClickListener navToLibraryControls(final Builder config) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Class activity = ActivityGlobalConfigureLibraries.class;
                Intent configureLibraryActivity = new Intent(config.mContext, activity);
                configureLibraryActivity.putExtra(PURPOSE_KEY, config.mPurpose);
                configureLibraryActivity.putExtra(PERMISSION_KEY, config.mPermission);
                config.mContext.startActivity(configureLibraryActivity);
            }
        };
    }

    /**
     * Builder class for GlobalSettingPurposeControls.
     * */
    public static class Builder {
        private final Context mContext;
        private Purpose mPurpose;
        private SensitiveData mPermission;
        private ViewGroup mContainer;
        private ConsistentStateTree mStateTree;
        private NoGlobalSettingsCard mNoSettingCard;

        private Builder(Context context) { mContext = context; }

        /**
         * Set the purpose for this control.
         *
         * @param purpose the purpose to control a policy for
         * @return the Builder instance
         * */
        public Builder setPurpose(Purpose purpose) {
            Precondition.checkIfNull(purpose, "Purpose cannot be null");

            mPurpose = purpose;
            return this;
        }

        /**
         * Set the permission for this control.
         *
         * @param permission the permission to control a policy for
         * @return the Builder instance
         * */
        public Builder setPermission(SensitiveData permission) {
            Precondition.checkIfNull(permission, "Must set permission");

            mPermission = permission;
            return this;
        }

        /**
         * Hide the given NoGlobalSettingsCard if this card has any actual settings to
         * configure. This is not optional and an exception will be thrown if this component
         * is not provided. Without it, the Activity that builds the purpose control will
         * display a "no settings to control" card.
         *
         * @param card the NoGlobalSettingsCard to hide from the parent Activity
         * @return the Builder instance
         * */
        public Builder hideCardOnVisualization(NoGlobalSettingsCard card) {
            Precondition.checkIfNull(card, "Must provide a no setting card");

            mNoSettingCard = card;
            return this;
        }

        /**
         * Sets the consistent state tree that controls this component's control switch state.
         *
         * @param stateTree the state tree that controls this component
         * @return the Builder instance
         * */
        public Builder setControlStateTree(ConsistentStateTree stateTree) {
            Precondition.checkIfNull(stateTree, "State tree cannot be null");

            mStateTree = stateTree;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the GlobalSettingPurposeControl instance this Builder created
         * */
        public GlobalSettingPurposeControl attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");

            Precondition.checkState(mPurpose != null, "Must have purpose");
            Precondition.checkState(mNoSettingCard != null, "Must have card");
            Precondition.checkState(mPermission != null, "Must have permission");
            Precondition.checkState(mStateTree != null, "Must have tree");
            Precondition.checkState(mNoSettingCard != null, "Must have card");

            mContainer = container;
            return new GlobalSettingPurposeControl(this);
        }
    }
}