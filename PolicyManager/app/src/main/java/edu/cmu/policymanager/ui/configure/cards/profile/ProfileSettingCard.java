package edu.cmu.policymanager.ui.configure.cards.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.cmu.policymanager.PolicyManager.Util;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.util.PermissionUtil;

public class ProfileSettingCard {
    private View card;
    private TextView cardTitle, permissionCounts;
    private ImageView cardIcon, expandButton;
    private ViewGroup policyContainer;
    private boolean isExpanded = false;

    private ProfileSettingCard(final Builder config) {
        card = LayoutInflater.from(config.context)
                             .inflate(R.layout.component_profile_setting_card,
                                      config.parent,
                                      false);

        cardTitle = card.findViewById(R.id.allyqian_policy_setting_card_name);
        cardIcon = card.findViewById(R.id.allyqian_policy_setting_card_icon);
        permissionCounts = card.findViewById(R.id.allyqian_policy_setting_card_count);
        policyContainer = card.findViewById(R.id.allyqian_profile_policy_container);
        expandButton = card.findViewById(R.id.allyqian_policy_setting_card_expand);

        if(config.policies.get(0).app.equalsIgnoreCase(PolicyManagerApplication.SYMBOL_ALL)) {
            String permission = config.policies.get(0).permission.androidPermission.toString();
            String permissionToConfigure = PermissionUtil.getDisplayPermission(permission);
            cardTitle.setText(permissionToConfigure);
            PolicyManagerApplication.ui
                                    .getIconManager()
                                    .getPermissionIcon(config.policies.get(0).permission)
                                    .addIconToView(cardIcon);
        } else {
            String packageName = config.policies.get(0).app;
            String appName = Util.getAppCommonName(config.context, packageName);
            cardTitle.setText(appName);
            PolicyManagerApplication.ui
                                    .getIconManager()
                                    .getAppIcon(packageName)
                                    .addIconToView(cardIcon);
        }

        int numberRequired = 0, numberDenied = 0;

        for(UserPolicy policy : config.policies) {
            ProfileIndividualSetting.builder(config.context)
                                    .setPolicy(policy)
                                    .attachTo(policyContainer);

            if(policy.isAllowed()) { numberRequired++; }
            if(policy.isDenied()) { numberDenied++; }
        }

        String countString = "";

        if((numberRequired == 0) && (numberDenied > 0)) {
            countString =
                    (numberDenied + (numberDenied == 1 ? " Permission off" : " Permissions off"));
        } else if((numberRequired > 0) && (numberDenied == 0)) {
            countString =
                    (numberRequired + (numberRequired == 1 ? " Permission on" : " Permissions on"));
        } else if((numberRequired > 0) && (numberDenied > 0)) {
                countString = (numberRequired +
                        ((numberRequired == 1) ? " Permisson on, " : " Permissions on, ") +
                        numberDenied +
                        ((numberDenied == 1) ? " Permission off" : " Permissions off"));
        }

        permissionCounts.setText(countString);

        expandButton.setOnClickListener(handlePress());

        config.parent.addView(card);
    }

    private View.OnClickListener handlePress() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExpanded = !isExpanded;

                if(isExpanded) {
                    policyContainer.setVisibility(View.VISIBLE);
                    expandButton.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_24dp);
                }
                else {
                    policyContainer.setVisibility(View.GONE);
                    expandButton.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_24dp);
                }
            }
        };
    }

    public static Builder builder(final Context context) {
        if(context == null) {
            throw new IllegalArgumentException("Cannot create builder with null context");
        }

        return new Builder(context);
    }

    public static class Builder {
        private Context context;
        private ViewGroup parent;
        private List<UserPolicy> policies;

        public Builder(final Context context) {
            this.context = context;
        }

        public Builder setPolicies(final List<UserPolicy> policies) {
            if(policies == null || policies.size() == 0) {
                throw new IllegalArgumentException("Need policies to render profile card.");
            }

            this.policies = policies;
            return this;
        }

        public void attachTo(final ViewGroup parent) {
            if(parent == null) {
                throw new IllegalArgumentException("Cannot attach component to null parent.");
            } else if(policies == null) {
                throw new IllegalStateException("Cannot create card with no policies.");
            }

            this.parent = parent;
            new ProfileSettingCard(this);
        }
    }
}