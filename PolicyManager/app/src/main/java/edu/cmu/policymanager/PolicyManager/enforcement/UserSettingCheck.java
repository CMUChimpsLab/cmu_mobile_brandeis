package edu.cmu.policymanager.PolicyManager.enforcement;

import android.os.Bundle;
import android.util.Log;

import java.util.List;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.PolicyNotification;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.peandroid.PEAndroid;

import static edu.cmu.policymanager.ui.runtime.RuntimeUI.INTENT_KEY_PERMISSION_REQUEST;

/**
 * Checks user-configured policies to determine sensitive data access policy. User-configured
 * policies come from either app or global settings. Which one is used depends on which setting
 * was most recently configured. A global setting does not necessarily mean it will always
 * override an app setting.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class UserSettingCheck extends PolicyEnforcementDecorator {
    public UserSettingCheck(PolicyEnforcement policyEnforcer) {
        super(policyEnforcer);
    }

    private boolean userHasAlreadyDecidedOnThis() {
        UserPolicy policy = UserPolicy.fromPermissionRequest(getPermissionRequest());
        policy = PolicyManager.getInstance().syncGetExactPolicy(policy);

        return policy != null;
    }

    private void promptUserForPolicyDecision() {
        if(getPermissionRequest().recv != null) {
            Bundle runtimeData = new Bundle();
            runtimeData.putParcelable(INTENT_KEY_PERMISSION_REQUEST, getPermissionRequest());
            PolicyManagerApplication.ui.setRuntimeData(runtimeData);
            PolicyManagerApplication.ui.launchRuntimeUI(getPermissionRequest().context);
        }
    }

    /**
     * Checks app and global settings to determine what the policy for this sensitive
     * data access should be. If a request has come in that a user has not already
     * decided on (ex: an off-device policy did not include a policy for third-party libraries,
     * and a library is accessing data), then the user will be prompted to make a decision.
     *
     * @return EnforcementStatus.Code.TERMINATED if a previous enforcement step ended the checks,
     *         EnforcementStatus.Code.USERSETTING_ALLOWED if the policy is 'allow',
     *         EnforcementStatus.Code.USERSETTING_DENIED if the policy is 'deny',
     *         EnforcementStatus.Code.USERSETTING_ASK everything else
     * */
    public EnforcementStatus.Code isAllowed() {
        if(didTerminate()) {
            return EnforcementStatus.Code.TERMINATED;
        } else {
            PermissionRequest request = getPermissionRequest();

            if(userHasAlreadyDecidedOnThis()) {
                UserPolicy policy = UserPolicy.fromPermissionRequest(request);
                policy = PolicyManager.getInstance().syncRequestEnforcedPolicy(policy);

                if(policy.isAllowed()) {
                    terminate();
                    sendUserSettingAllowedNotification(policy);
                    PEAndroid.connectToReceiver(request.recv).allowPermission();
                    return EnforcementStatus.Code.USERSETTING_ALLOWED;
                } else if(policy.isDenied()) {
                    terminate();
                    sendUserSettingDeniedNotification(policy);
                    PEAndroid.connectToReceiver(request.recv).denyPermission();
                    return EnforcementStatus.Code.USERSETTING_DENIED;
                } else {
                    terminate();
                    promptUserForPolicyDecision();
                    return EnforcementStatus.Code.USERSETTING_ASK;
                }
            } else {
                promptUserForPolicyDecision();
                return EnforcementStatus.Code.USERSETTING_ASK;
            }
        }
    }

    private void sendUserSettingDeniedNotification(UserPolicy policy) {
        String source = "User Settings";

        if(isGlobalPolicy(policy)) {
            source = "Global Settings";
        }

        PermissionRequest req = getPermissionRequest();
        PolicyNotification.sendDeniedNotification(req.context, source, req);
    }

    private void sendUserSettingAllowedNotification(UserPolicy policy) {
        String source = "User Settings";

        if(isGlobalPolicy(policy)) {
            source = "Global Settings";
        }

        PermissionRequest req = getPermissionRequest();
        PolicyNotification.sendAllowedNotification(req.context, source, req);
    }

    private boolean isGlobalPolicy(UserPolicy policy) {
        return policy.app.equalsIgnoreCase(PolicyManagerApplication.SYMBOL_ALL) &&
               policy.getLastUpdatedMillis() > 0;
    }
}