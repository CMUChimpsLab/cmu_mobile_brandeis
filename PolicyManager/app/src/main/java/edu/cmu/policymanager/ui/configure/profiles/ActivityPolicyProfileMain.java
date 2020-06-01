package edu.cmu.policymanager.ui.configure.profiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Consumer;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.MainActivity;
import edu.cmu.policymanager.ui.configure.profiles.add.ActivityAddProfileStepOne;
import edu.cmu.policymanager.ui.configure.cards.profile.PolicyProfileCard;

public class ActivityPolicyProfileMain extends Activity
        implements Observer {
    private final Context mActivityContext = this;
    private final Observer mObservingActivity = this;
    private String mActiveProfile = "";
    private LinearLayout profileCardContainer, globalSettingsContainer, appSettingsContainer;
    private List<PolicyProfileCard> policyProfileCards = new ArrayList<>(2);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_profiles);

        profileCardContainer = findViewById(R.id.policy_profile_container);
        globalSettingsContainer = findViewById(R.id.policy_profile_global_settings_container);
        appSettingsContainer = findViewById(R.id.policy_profile_app_settings_container);
    }

    @Override
    public void onResume() {
        super.onResume();

        findViewById(R.id.policy_profile_add_button).setVisibility(View.VISIBLE);

        mActiveProfile = PolicyManager.getInstance().getActivePolicyProfile().toString();

        globalSettingsContainer.removeAllViews();
        appSettingsContainer.removeAllViews();
        profileCardContainer.removeAllViews();

        PolicyManager.getInstance()
                     .getPolicyProfileSettings(PolicyProfile.DEFAULT)
                     .thenAccept(renderPolicyCards(PolicyProfile.DEFAULT));

        PolicyManager.getInstance()
                     .getPolicyProfileSettings(PolicyProfile.ORGANIZATIONAL)
                     .thenAccept(renderPolicyCards(PolicyProfile.ORGANIZATIONAL));
    }

    @Override public void onStart() { super.onStart(); }
    @Override public void onPause() { super.onPause(); }
    @Override public void onStop() { super.onStop(); }
    @Override public void onDestroy() { super.onDestroy(); }

    private Consumer<List<UserPolicy>> renderPolicyCards(final CharSequence profile) {
        return new Consumer<List<UserPolicy>>() {
            @Override
            public void accept(final List<UserPolicy> policies) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(policies != null && policies.size() > 0) {
                            if(PolicyProfile.ORGANIZATIONAL.equals(profile.toString())) {
                                findViewById(R.id.policy_profile_add_button)
                                        .setVisibility(View.GONE);
                            }

                            createPolicyProfileCard(
                                    profile,
                                    R.drawable.global_settings_background
                            );

                            activateToggleForActiveProfile();
                        }
                    }
                });
            }
        };
    }

    @Override
    public void update(Observable o, Object arg) {
        String profileName = String.valueOf(arg);

        for(PolicyProfileCard profileCard : policyProfileCards) {
            if(!profileCard.profileName.equalsIgnoreCase(profileName)) {
                profileCard.disable();
            }
        }
    }

    private void activateToggleForActiveProfile() {
        for(PolicyProfileCard card : policyProfileCards) {
            if(card.profileName.equalsIgnoreCase(mActiveProfile)) {
                card.enable();
            } else {
                card.disable();
            }
        }
    }

    private void createPolicyProfileCard(CharSequence policyProfileName,
                                         int cardBackgroundImageResource) {
        String profileName = policyProfileName.toString();

        PolicyProfileCard card =
                PolicyProfileCard.builder(mActivityContext)
                                 .setCardBackgroundImage(cardBackgroundImageResource)
                                 .setProfileName(profileName)
                                 .placeGlobalSettingsIn(globalSettingsContainer)
                                 .placeAppSettingsIn(appSettingsContainer)
                                 .attachTo(profileCardContainer);

        policyProfileCards.add(card);
        card.addObserver(mObservingActivity);
    }

    public void exitToHomescreen(View v) {
        Intent homescreenActivity = new Intent(mActivityContext, MainActivity.class);
        mActivityContext.startActivity(homescreenActivity);
    }

    public void beginProfileAdd(View v) {
        Intent addStepOne = new Intent(mActivityContext, ActivityAddProfileStepOne.class);
        startActivity(addStepOne);
    }
}