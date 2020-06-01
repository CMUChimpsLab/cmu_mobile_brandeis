package edu.cmu.policymanager.PolicyManager.enforcement;

import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.PolicyNotification;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.peandroid.PEAndroid;

/**
 * Checks to see if a policy profile is active other than the user default profile. If a
 * non-default profile is active, then check to see if this profile denies access to
 * the requested data.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PolicyProfileCheck extends PolicyEnforcementDecorator {
    public PolicyProfileCheck(PolicyEnforcement policyEnforcer) {
        super(policyEnforcer);
    }

    /**
     * Refers to a non-default policy profile to determine if access to this sensitive data
     * is allowed or not. If a default policy profile is active, begin the next step in the
     * policy enforcement algorithm.
     *
     * @return EnforcementStatus.Code.TERMINATED if a previous step in the enforcement chain ended,
     *         EnforcementStatus.Code.POLICYPROFILE_DENIED if this profile denies access
     * */
    public EnforcementStatus.Code isAllowed() {
        if(didTerminate()) {
            return EnforcementStatus.Code.TERMINATED;
        } else {
            String policyProfile = PolicyManager.getInstance().getActivePolicyProfile().toString();

            if(policyProfile.equalsIgnoreCase(PolicyProfile.DEFAULT)) {
                return super.isAllowed();
            } else {
                PermissionRequest request = getPermissionRequest();

                UserPolicy profilePolicy = UserPolicy.fromPermissionRequest(request);

                profilePolicy = PolicyManager.getInstance()
                                             .syncRequestEnforcedPolicy(profilePolicy);

                if(activeProfileMatchesPolicy(profilePolicy) && profilePolicy.isDenied()) {
                    terminate();
                    PolicyNotification.sendDeniedNotification(request.context,
                                                              policyProfile,
                                                              request);

                    PEAndroid.connectToReceiver(request.recv).denyPermission();
                    return EnforcementStatus.Code.POLICYPROFILE_DENIED;
                }

                return super.isAllowed();
            }
        }
    }

    private boolean activeProfileMatchesPolicy(final UserPolicy policy) {
        String policyProfile = PolicyManager.getInstance().getActivePolicyProfile().toString();

        return policyProfile.equalsIgnoreCase(policy.profile);
    }
}