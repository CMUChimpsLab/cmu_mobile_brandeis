package edu.cmu.policymanager.PolicyManager.enforcement;

/**
 * PolicyEnforcementDecorator - this is what each policy enforcement fragment will
 * extend.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PolicyEnforcementDecorator implements PolicyEnforcement {
    protected PolicyEnforcement policyEnforcer;

    public PolicyEnforcementDecorator(final PolicyEnforcement policyEnforcer) {
        this.policyEnforcer = policyEnforcer;
    }

    public boolean didTerminate() { return policyEnforcer.didTerminate(); }
    public void terminate() { policyEnforcer.terminate(); }

    public PermissionRequest getPermissionRequest() { return policyEnforcer.getPermissionRequest(); }

    public EnforcementStatus.Code isAllowed() {
        return policyEnforcer.isAllowed();
    }

    @Override
    public void updatePermissionRequest(final PermissionRequest permissionRequest) {
        policyEnforcer.updatePermissionRequest(permissionRequest);
    }
}