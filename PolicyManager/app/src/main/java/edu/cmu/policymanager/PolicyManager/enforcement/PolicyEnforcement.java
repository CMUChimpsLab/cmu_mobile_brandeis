package edu.cmu.policymanager.PolicyManager.enforcement;

/**
 * PolicyEnforcement interface needed to create a policy enforcement chain algorithm.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public interface PolicyEnforcement {
    /**
     * Check to see if the previous fragment of the policy enforcement algorithm
     * has allowed us to enforce this part of the algorithm or not.
     *
     * @return true if the algorithm terminated, false otherwise
     * */
    public boolean didTerminate();

    /**
     * Terminate the policy enforcement algorithm.
     * */
    public void terminate();

    /**
     * Enforce this fragment of the policy enforcement algorithm
     * chain.
     *
     * @return EnforcementStatus - Unique code that describes the
     * result of privacy policy enforcement. Used only for unit testing.
     * */
    public EnforcementStatus.Code isAllowed();

    /**
     * Get the PermissionRequest object.
     *
     * @return the PermissionRequest
     * */
    public PermissionRequest getPermissionRequest();

    /**
     * During the course of the policy checks, a detail of the original
     * permission request has changed (ex: stacktrace analysis discovered
     * a third-party library), so update with a new permission request.
     * */
    public void updatePermissionRequest(final PermissionRequest permissionRequest);
}