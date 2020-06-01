package edu.cmu.policymanager.ui.configure.cards.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.function.Consumer;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveDataGroup;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.configure.cards.globalsetting.PermissionGroupCard;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Displays a policy profile as a card, and a control to enable or disable it. This will also
 * render a profile, and all of its relevant settings.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PolicyProfileCard extends Observable {
    private Switch mProfileToggle;
    public final String profileName;
    private LinearLayout mGlobalSettingsContainer, mAppSettingsContainer;
    private ImageView mProfileImageDisplay;
    private Builder mConfig;

    private PolicyProfileCard(Builder config) {
        mConfig = config;

        View card = LayoutInflater.from(config.context)
                                  .inflate(
                                          R.layout.component_policy_profile_card,
                                          config.container,
                                          false
                                  );

        mGlobalSettingsContainer = config.globalSettingsContainer;
        mAppSettingsContainer = config.appSettingsContainer;

        profileName = config.profileName;

        String profileDisplayName = config.profileName;

        if(profileDisplayName.equalsIgnoreCase(PolicyProfile.DEFAULT)) {
            profileDisplayName = "Personal mode";
        }

        TextView profileNameDisplay = card.findViewById(R.id.policy_profile_card_name);
        profileNameDisplay.setText(profileDisplayName);

        mProfileImageDisplay = card.findViewById(R.id.policy_profile_card_image);
        mProfileImageDisplay.setImageResource(config.cardImageId);

        mProfileToggle = card.findViewById(R.id.policy_profile_card_toggle);

        if(config.profileName.equalsIgnoreCase(PolicyProfile.DEFAULT)) {
            card.setOnClickListener(visualizeGlobalSettingsOnClick(config));
            visualizeGlobalSettings(config);
            mProfileToggle.setChecked(true);
        } else {
            card.setOnClickListener(visualizeProfileOnClick());
        }

        mProfileToggle.setOnCheckedChangeListener(handleToggleStateChange());

        config.container.addView(card);
    }

    private void clearContainers() {
        mGlobalSettingsContainer.removeAllViews();
        mAppSettingsContainer.removeAllViews();
    }

    private CompoundButton.OnCheckedChangeListener handleToggleStateChange() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isPressed() && isChecked) {
                    displayActivatePrompt(profileName);
                } else if(buttonView.isPressed() && !isChecked) {
                    mProfileToggle.toggle();
                    mProfileToggle.setChecked(true);
                    explainThatThereCanBeNoInactiveProfiles();
                }
            }
        };
    }

    public void disable() {
        mProfileToggle.toggle();
        mProfileToggle.setChecked(false);
        mProfileImageDisplay.setColorFilter(
                Color.argb(0xAA, 0xb3, 0xb3, 0xb3)
        );
    }

    public void enable() {
        mProfileToggle.toggle();
        mProfileToggle.setChecked(true);
        mProfileImageDisplay.setColorFilter(
                Color.argb(0x00, 0x00, 0x00, 0x00)
        );

        PolicyManager.getInstance()
                     .getPolicyProfileSettings(profileName)
                     .thenAccept(renderProfile());
    }

    private View.OnClickListener visualizeGlobalSettingsOnClick(final Builder config) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearContainers();
                visualizeGlobalSettings(config);
            }
        };
    }

    private void visualizeGlobalSettings(Builder config) {
        for(SensitiveDataGroup group : DangerousPermissions.ALL_SENSITIVE_DATA_GROUPS) {
            PermissionGroupCard.builder(config.context)
                               .setPermissionGroup(group)
                               .attachTo(mGlobalSettingsContainer);
        }

        mAppSettingsContainer.addView(createNoneAvailableText(config.context));
    }

    private View.OnClickListener visualizeProfileOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PolicyManager.getInstance()
                             .getPolicyProfileSettings(profileName)
                             .thenAccept(renderProfile());
            }
        };
    }

    private Consumer<List<UserPolicy>> renderProfile() {
        return new Consumer<List<UserPolicy>>() {
            @Override
            public void accept(final List<UserPolicy> profilePolicies) {
                mGlobalSettingsContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        final Map<String, List<UserPolicy>> permissions = new HashMap<String, List<UserPolicy>>(),
                                                            apps = new HashMap<String, List<UserPolicy>>();

                        for(UserPolicy policy : profilePolicies) {
                            if(policy.app.equalsIgnoreCase(PolicyManagerApplication.SYMBOL_ALL)) {
                                String permission =
                                        policy.permission.androidPermission.toString();

                                permissions.putIfAbsent(
                                        permission,
                                        new LinkedList<UserPolicy>()
                                );

                                permissions.get(permission).add(policy);
                            } else {
                                apps.putIfAbsent(policy.app, new LinkedList<UserPolicy>());
                                apps.get(policy.app).add(policy);
                            }
                        }

                        clearContainers();

                        if(permissions.keySet().size() > 0) {
                            visualizePolicyCards(permissions, mConfig, mGlobalSettingsContainer);
                        } else {
                            mGlobalSettingsContainer.addView(
                                    createNoneAvailableText(mConfig.context)
                            );
                        }

                        if(apps.keySet().size() > 0) {
                            visualizePolicyCards(apps, mConfig, mAppSettingsContainer);
                        } else {
                            mAppSettingsContainer.addView(createNoneAvailableText(mConfig.context));
                        }
                    }
                });
            }
        };
    }

    private void visualizePolicyCards(Map<String, List<UserPolicy>> policies,
                                      Builder config,
                                      LinearLayout container) {
        for(String entry : policies.keySet()) {
            ProfileSettingCard.builder(config.context)
                              .setPolicies(policies.get(entry))
                              .attachTo(container);
        }
    }

    private void displayActivatePrompt(String profileToActivate) {
        Context context = mConfig.context;

        String messageTitle = context.getString(R.string.activate_policy_profile_title,
                                                profileToActivate),
               messageBody = context.getString(R.string.activate_policy_profile_body);

        AlertDialog dialog = new AlertDialog.Builder(context)
                                            .setCancelable(false)
                                            .setTitle(messageTitle)
                                            .setMessage(messageBody)
                                            .setPositiveButton(R.string.yes_caps, enableProfile())
                                            .setNegativeButton(R.string.no_caps, undoToggleCheck())
                                            .create();

        dialog.show();
    }

    private void explainThatThereCanBeNoInactiveProfiles() {
        Context context = mConfig.context;

        String messageTitle = "Unable to perform action",
               messageBody = "You cannot have any inactive profiles. Please chose another " +
                             "profile to activate.";

        AlertDialog dialog = new AlertDialog.Builder(context)
                                            .setCancelable(false)
                                            .setTitle(messageTitle)
                                            .setMessage(messageBody)
                                            .setPositiveButton("OK", noop())
                                            .create();

        dialog.show();
    }

    private DialogInterface.OnClickListener noop() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        };
    }

    private DialogInterface.OnClickListener enableProfile() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mProfileToggle.setChecked(true);
                PolicyManager.getInstance()
                             .activatePolicyProfile(profileName)
                             .thenAccept(displaySelectedProfile());
            }
        };
    }

    private Consumer<Void> displaySelectedProfile() {
        return new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {
                setChanged();
                notifyObservers(profileName);
                mProfileImageDisplay.setColorFilter(
                        Color.argb(0x00, 0x00, 0x00, 0x00)
                );

                PolicyManager.getInstance()
                             .getPolicyProfileSettings(profileName)
                             .thenAccept(renderProfile());
            }
        };
    }

    private DialogInterface.OnClickListener undoToggleCheck() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mProfileToggle.setChecked(false);
            }
        };
    }

    private TextView createNoneAvailableText(Context context) {
        TextView noneAvailableDisplay = new TextView(context);

        noneAvailableDisplay.setTextSize(18);
        noneAvailableDisplay.setText(R.string.none_available);

        ViewGroup.MarginLayoutParams margins = new ViewGroup.MarginLayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        margins.setMargins(0, 0, 0, 0);
        noneAvailableDisplay.setLayoutParams(margins);

        return noneAvailableDisplay;
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
     * Builder class for PolicyProfileCard.
     * */
    public static class Builder {
        private final Context context;
        private int cardImageId;
        private String profileName;
        private ViewGroup container;
        private LinearLayout globalSettingsContainer, appSettingsContainer;

        private Builder(final Context context) { this.context = context; }

        /**
         * Set the card's image as a resource id
         *
         * @param cardImageId the card's image as a resource id
         * @return the Builder instance
         * */
        public Builder setCardBackgroundImage(int cardImageId) {
            Precondition.checkState(cardImageId > 0, "Invalid resource ID");

            this.cardImageId = cardImageId;
            return this;
        }

        /**
         * Set the name of the profile (card title)
         *
         * @param profileName name of the profile and the card's title
         * @return the Builder instance
         * */
        public Builder setProfileName(CharSequence profileName) {
            Precondition.checkEmptyCharSequence(profileName);

            this.profileName = profileName.toString();
            return this;
        }

        /**
         * Set the container the global settings cards should go.
         *
         * @param globalSettingsContainer the container for global settings
         * @return the Builder instance
         * */
        public Builder placeGlobalSettingsIn(LinearLayout globalSettingsContainer) {
            Precondition.checkIfNull(globalSettingsContainer, "Container is null");

            this.globalSettingsContainer = globalSettingsContainer;
            return this;
        }

        /**
         * Set the container the app settings cards should go.
         *
         * @param appSettingsContainer the container for app settings
         * @return the Builder instance
         * */
        public Builder placeAppSettingsIn(LinearLayout appSettingsContainer) {
            Precondition.checkIfNull(appSettingsContainer, "Container cannot be null");

            this.appSettingsContainer = appSettingsContainer;
            return this;
        }

        /**
         * Add this component to the provided container.
         *
         * @param container the container to add to
         * @return new PolicyProfileCard instance created by this builder
         * */
        public PolicyProfileCard attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot place in empty container");

            if(globalSettingsContainer == null && appSettingsContainer == null) {
                throw new IllegalStateException(
                        "Profile settings need to have at least one container to hold " +
                        "their settings."
                );
            } else if(profileName == null) {
                throw new IllegalStateException("Profile names cannot be empty.");
            }

            this.container = container;
            return new PolicyProfileCard(this);
        }
    }
}