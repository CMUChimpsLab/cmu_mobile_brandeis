package edu.cmu.policymanager.PolicyManager.enforcement;

/**
 * A stub/dummy policy enforcement fragment that is used to
 * build an actual policy enforcement algorithm upon.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PolicyStub implements PolicyEnforcement {
    private PermissionRequest permissionRequest;
    private boolean terminted = false;

    public PolicyStub(final PermissionRequest permissionRequest) {
        this.permissionRequest = permissionRequest;
    }

    @Override
    public boolean didTerminate() { return terminted; }

    @Override
    public void terminate() { terminted = true; }

    @Override
    public PermissionRequest getPermissionRequest() { return permissionRequest; }

    @Override
    public EnforcementStatus.Code isAllowed() {
        return EnforcementStatus.Code.SUCCESS;
    }

    @Override
    public void updatePermissionRequest(final PermissionRequest permissionRequest) {
        this.permissionRequest = permissionRequest;
    }
}