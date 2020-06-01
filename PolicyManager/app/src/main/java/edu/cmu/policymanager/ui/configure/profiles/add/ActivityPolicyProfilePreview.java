package edu.cmu.policymanager.ui.configure.profiles.add;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.CMUPolicyManagerService;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.cards.profile.ProfileSettingCard;
import edu.cmu.policymanager.ui.configure.profiles.ActivityPolicyProfileMain;

public class ActivityPolicyProfilePreview extends Activity {
    private final Context mActivityContext = this;
    private ViewGroup mGlobalSettingContainer;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_profile_preview);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mActivityContext);
        mGlobalSettingContainer = findViewById(R.id.profile_preview_global_settings_container);
    }

    @Override
    public void onStart() { super.onStart(); }

    @Override
    public void onResume() {
        super.onResume();

        PolicyManager.getInstance()
                     .requestSampleProfilePolicies()
                     .thenAccept(renderProfileSettings());
    }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onDestroy() { super.onDestroy(); }

    private Consumer<List<UserPolicy>> renderProfileSettings() {
        return new Consumer<List<UserPolicy>>() {
            @Override
            public void accept(final List<UserPolicy> policies) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<List<UserPolicy>> groups =
                                createPermissionPurposeGroups(policies);

                        for(List<UserPolicy> group : groups) {
                            ProfileSettingCard.builder(mActivityContext)
                                              .setPolicies(group)
                                              .attachTo(mGlobalSettingContainer);
                        }
                    }
                });
            }
        };
    }

    private List<List<UserPolicy>> createPermissionPurposeGroups(final List<UserPolicy> policies) {
        Map<String, List<UserPolicy>> permissionPolicyMap =
                new HashMap<String, List<UserPolicy>>();

        for(UserPolicy policy : policies) {
            String key = policy.permission.androidPermission.toString();
            permissionPolicyMap.putIfAbsent(key, new LinkedList<UserPolicy>());

            permissionPolicyMap.get(key).add(policy);
        }

        List<List<UserPolicy>> permissionPurposeGroup = new LinkedList<>();

        for(String key : permissionPolicyMap.keySet()) {
            permissionPurposeGroup.add(permissionPolicyMap.get(key));
        }

        return permissionPurposeGroup;
    }

    public void cancel(View v) {
        Intent profileMainActivity = new Intent(mActivityContext, ActivityPolicyProfileMain.class);
        startActivity(profileMainActivity);
    }

    public void back(View v) { finish(); }

    public void installProfile(View v) {
        PolicyManager.getInstance()
                     .installPolicyProfile(PolicyProfile.ORGANIZATIONAL)
                     .thenAccept(returnToProfileHome());
    }

    private Consumer<Void> returnToProfileHome() {
        return new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {
                Intent quickSettingAdd = new Intent(CMUPolicyManagerService.POLICY_PROFILE_ADDED);
                quickSettingAdd.putExtra(
                        CMUPolicyManagerService.POLICY_PROFILE_NAME,
                        PolicyProfile.ORGANIZATIONAL
                );

                mLocalBroadcastManager.sendBroadcast(quickSettingAdd);

                Intent backToMainProfileScreen =
                        new Intent(mActivityContext, ActivityPolicyProfileMain.class);

                startActivity(backToMainProfileScreen);
            }
        };
    }
}