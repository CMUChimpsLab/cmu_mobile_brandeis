package edu.cmu.policymanager.PolicyManager;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Determines a policy to enforce based on input UserPolicy. This class contains both
 * synchronous (necessary when querying policies in onDangerousPermissionRequest) and
 * asynchronous (necessary when querying policies in the user interface) methods for querying
 * user policies.
 *
 * Note - policy profiles are still experimental/prototyped. Any methods involving policy
 * profiles should be based around the default user profile or the organizational profile
 * found in DataRepository/network/BrandeisBackend.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 */
public class PolicyManager {
    private static PolicyManager sPolicyManager = null;
    private long mTimeframeAskResultIsValid = 5 * 60 * 1000;

    private static String mActiveProfile = PolicyProfile.DEFAULT;

    /**
     * Get instance of the PolicyManager.
     *
     * @return a policy manager instance
     * */
    public static PolicyManager getInstance() {
        if(sPolicyManager == null) { sPolicyManager = new PolicyManager(); }
        return sPolicyManager;
    }

    /**
     * Sets the time in milliseconds for how long a user's response to an ASK prompt is valid.
     * When the time expires, the user will be presented with another dialog, unless they
     * persist their answer.
     *
     * @param timeframeInMillis the timeframe in milliseconds a response is valid for
     * */
    public void setTimeframeAskResultIsValid(long timeframeInMillis) {
        Precondition.checkState(timeframeInMillis > 0, "Must set timeframe");
        mTimeframeAskResultIsValid = timeframeInMillis;
    }

    /**
     * Gets the policy profile name that is currently active on this device.
     *
     * @return the active policy profile
     * */
    public CharSequence getActivePolicyProfile() { return mActiveProfile; }

    /**
     * Asynchronously install a given policy profile. This will attempt to install the
     * sample profile from BrandeisBackend.
     *
     * @param profileName the name of the profile you wish to install
     * @return void CompletableFuture
     * */
    public CompletableFuture<Void> installPolicyProfile(final CharSequence profileName) {
        Precondition.checkEmptyCharSequence(profileName);

        return CompletableFuture.supplyAsync(
                new Supplier<Void>() {
                    @Override
                    public Void get() {
                        DataRepository.getInstance().installProfile(profileName.toString());
                        return null;
                    }
                }
        );
    }

    /**
     * Synchronously install a given policy profile. This will attempt to install the
     * sample profile from BrandeisBackend.
     *
     * @param profileName the name of the profile you wish to install
     * */
    public void syncInstallPolicyProfile(CharSequence profileName) {
        Precondition.checkEmptyCharSequence(profileName);
        DataRepository.getInstance().installProfile(profileName.toString());
    }

    /**
     * Asynchronously activate the given policy profile. Any policies that this profile
     * denies will now take precedence when querying for a policy to enforce.
     *
     * @param profileName the profile to activate
     * @return void CompletableFuture
     * */
    public CompletableFuture<Void> activatePolicyProfile(final CharSequence profileName) {
        Precondition.checkEmptyCharSequence(profileName);
        mActiveProfile = profileName.toString();

        return CompletableFuture.supplyAsync(
                new Supplier<Void>() {
                    @Override
                    public Void get() {
                        DataRepository.getInstance().activateProfile(profileName.toString());
                        return null;
                    }
                }
        );
    }

    /**
     * Synchronously activate the given policy profile. Any policies that this profile
     * denies will now take precedence when querying for a policy to enforce.
     *
     * @param profileName the profile to activate
     * */
    public void syncActivatePolicyProfile(CharSequence profileName) {
        Precondition.checkEmptyCharSequence(profileName);
        mActiveProfile = profileName.toString();

        DataRepository.getInstance().activateProfile(profileName.toString());
    }

    public CompletableFuture<List<UserPolicy>> getProfileSettings(final CharSequence profileName) {
        Precondition.checkEmptyCharSequence(profileName);

        return CompletableFuture.supplyAsync(
                new Supplier<List<UserPolicy>>() {
                    @Override
                    public List<UserPolicy> get() {
                        String profileString = profileName.toString();
                        return DataRepository.getInstance().getProfileSettings(profileString);
                    }
                }
        );
    }

    public List<UserPolicy> syncGetProfileSettings(CharSequence profileName) {
        Precondition.checkEmptyCharSequence(profileName);
        return DataRepository.getInstance().getProfileSettings(profileName.toString());
    }

    /**
     * Add a new user policy asynchronously to the policy manager.
     *
     * @param policy the policy to be added.
     * */
    public void add(final UserPolicy policy) {
        Precondition.checkIfNull(policy, "Cannot add a null policy");
        Precondition.checkIfPolicyIsValid(policy);

        CompletableFuture.runAsync(
                new Runnable() {
                    @Override
                    public void run() {
                        DataRepository.getInstance().addUserPolicy(policy);
                    }
                }
        );
    }

    /**
     * Add a new user policy synchronously to the policy manager.
     *
     * @param policy the policy to be added.
     * */
    public void syncAdd(final UserPolicy policy) {
        Precondition.checkIfNull(policy, "Cannot add a null policy");
        Precondition.checkIfPolicyIsValid(policy);

        DataRepository.getInstance().addUserPolicy(policy);
    }

    /**
     * Asynchronously get all app policies for the provided package name as a list of UserPolicies.
     *
     * @param packageName the package name to get policies for
     * @return list of UserPolicy for this app
     * */
    public CompletableFuture<List<UserPolicy>> getPoliciesForApp(final CharSequence packageName) {
        Precondition.checkEmptyCharSequence(packageName);

        return CompletableFuture.supplyAsync(new Supplier<List<UserPolicy>>() {
            @Override
            public List<UserPolicy> get() {
                return DataRepository.getInstance().getAllPoliciesForApp(packageName.toString());
            }
        });
    }

    /**
     * Get the UserPolicy that matches the input policy *exactly*. This is not influenced
     * by most recent setting, or any policy algorithms. Synchronously queries.
     *
     * @param policy the UserPolicy to search for
     * @return the matching UserPolicy
     * */
    public UserPolicy syncGetExactPolicy(UserPolicy policy) {
        Precondition.checkIfNull(policy, "Cannot query on a null policy");
        Precondition.checkIfPolicyIsValid(policy);

        return DataRepository.getInstance().getExactPolicy(policy);
    }

    /**
     * Asynchronously gets the list of UserPolicy objects that belong to the given policy profile.
     *
     * @param profileName the policy profile to query
     * @return list of UserPolicy objects that belong to the provided policy profile
     * */
    public CompletableFuture<List<UserPolicy>> getPolicyProfileSettings(
            final CharSequence profileName
    ) {
        Precondition.checkEmptyCharSequence(profileName);

        return CompletableFuture.supplyAsync(new Supplier<List<UserPolicy>>() {
            @Override
            public List<UserPolicy> get() {
                return DataRepository.getInstance().getProfileSettings(profileName.toString());
            }
        });
    }

    /**
     * Convenience method to just the UserPolicys from the sample organizational profile
     * for display.
     *
     * @return list of UserPolicy objects that belong to the sample profile
     * */
    public CompletableFuture<List<UserPolicy>> requestSampleProfilePolicies() {
        return CompletableFuture.supplyAsync(new Supplier<List<UserPolicy>>() {
            @Override
            public List<UserPolicy> get() {
                return DataRepository.getInstance().previewProfileSettings();
            }
        });
    }

    /**
     * Request the policy that is being enforced, given a tuple of
     * profile/app/permission/purpose/library. The UserPolicy returned from this query may not
     * be the same policy that was input. A global or profile policy could be returned if
     * they are the policies currently in effect.
     *
     * A policy is enforced in the following order:
     * 1. Policy profile
     * 2. Quick settings
     * 3. User settings
     *
     * User settings are enforced according to the most recently configured setting. If a global
     * setting was configured, but then an app setting was configured some time later, then the
     * app setting is the policy in effect.
     *
     * Please see other research documents for more detail on how exactly policies are enforced
     * by the CMU policy manager implementation.
     *
     * @param policyTuples the (profile, app, permission, purpose, library) tuples to query over.
     * @return CompletableFuture with the query result, which is the policy currently in effect.
     * */
    public CompletableFuture<UserPolicy> requestEnforcedPolicy(final UserPolicy policyTuples) {
        Precondition.checkIfNull(policyTuples, "Cannot query on a null policy");
        Precondition.checkIfPolicyIsValid(policyTuples);

        return CompletableFuture.supplyAsync(
                new Supplier<UserPolicy>() {
                    @Override
                    public UserPolicy get() {
                        return extractPolicyToEnforce(
                                DataRepository.getInstance().getUserPolicyAction(policyTuples)
                        );
                    }
                }
        );
    }

    /**
     * Request synchronously the policy that is being enforced, given a tuple of
     * profile/app/permission/purpose/library. The UserPolicy returned from this query may not
     * be the same policy that was input. A global or profile policy could be returned if
     * they are the policies currently in effect.
     *
     * Do not use this anywhere in the UI, as this is intended only for when an immediate
     * result is needed, such as in unit tests or the policy enforcement algorithms (which are
     * run in a background service).
     *
     * A policy is generally enforced in the following order:
     * 1. Policy profile
     * 2. Quick settings
     * 3. User settings
     *
     * User settings are enforced according to the most recently configured setting. If a global
     * setting was configured, but then an app setting was configured some time later, then the
     * app setting is the policy in effect.
     *
     * Please see other research documents for more detail on how exactly policies are enforced
     * by the CMU policy manager implementation.
     *
     * @param policyTuples the (profile, app, permission, purpose, library) tuples to query over.
     * @return CompletableFuture with the query result, which is the policy currently in effect.
     * */
    public UserPolicy syncRequestEnforcedPolicy(final UserPolicy policyTuples) {
        Precondition.checkIfNull(policyTuples, "Cannot query on a null policy");
        Precondition.checkIfPolicyIsValid(policyTuples);

        return extractPolicyToEnforce(
                DataRepository.getInstance().getUserPolicyAction(policyTuples)
        );
    }

    /**
     * Asynchronously logs the user's policy decision from an ASK prompt - allow or deny,
     * and tracks the timestamp this decision occurred.
     *
     * @param policyResult the UserPolicy object representing the user's policy decision
     * */
    public void logPolicyForAskPrompt(final UserPolicy policyResult) {
        Precondition.checkIfNull(policyResult, "Cannot persist a null policy");
        Precondition.checkIfPolicyIsValid(policyResult);

        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                DataRepository.getInstance().logUserResponseToAskPrompt(policyResult);
            }
        });
    }

    /**
     * Synchronously logs the user's policy decision from an ASK prompt - allow or deny,
     * and tracks the timestamp this decision occurred.
     *
     * @param policyResult the UserPolicy object representing the user's policy decision
     * */
    public void syncLogPolicyForAskPrompt(final UserPolicy policyResult) {
        Precondition.checkIfNull(policyResult, "Cannot persist a null policy");
        Precondition.checkIfPolicyIsValid(policyResult);

        DataRepository.getInstance().logUserResponseToAskPrompt(policyResult);
    }

    /**
     * Update the following user policy asynchronously. If the policy does not
     * exist, it will be created.
     *
     * @param policy the policy to update.
     * */
    public void update(final UserPolicy policy) {
        Precondition.checkIfNull(policy, "Cannot update a null policy");
        Precondition.checkIfPolicyIsValid(policy);

        CompletableFuture.runAsync(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataRepository.getInstance().updateUserPolicy(policy);
                        } catch(IllegalStateException doesNotExist) {
                            DataRepository.getInstance().addUserPolicy(policy);
                        }
                    }
                }
        );
    }

    /**
     * Update the following user policy synchronously
     *
     * @param policy the policy to update.
     * */
    public void syncUpdate(final UserPolicy policy) {
        Precondition.checkIfNull(policy, "Cannot update a null policy");
        Precondition.checkIfPolicyIsValid(policy);

        try {
            DataRepository.getInstance().updateUserPolicy(policy);
        } catch (IllegalStateException doesNotExist) {
            DataRepository.getInstance().addUserPolicy(policy);
        }
    }

    /*
    * If a policy is allow or deny, then return the allow or deny policy. However, ASK is
    * based on timers, and so the policy manager must fetch the last decision made for
    * this request. If there was no prior decision made, or if the timer is up, then
    * launch the runtime UI again to prompt for a decision.
    * */
    private UserPolicy extractPolicyToEnforce(final UserPolicy policy) {
        if(policy.isAllowed() || policy.isDenied()) { return policy; }

        UserPolicy askResult =
                DataRepository.getInstance()
                              .getUserResponseToAskPromptWithinTime(
                                      policy,
                                      mTimeframeAskResultIsValid
                              );

        return (askResult == null ? policy : askResult);
    }
}