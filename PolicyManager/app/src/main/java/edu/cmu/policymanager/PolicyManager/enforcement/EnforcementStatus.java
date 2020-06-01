package edu.cmu.policymanager.PolicyManager.enforcement;

/**
 * Result status of a policy enforcement algorithm fragment. Mostly useful for unit tests
 * to know how the algorithm is terminated/what its doing from the outside.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public final class EnforcementStatus {
    public enum Code {
        SUCCESS,
        STATICANALYSIS_INCONSISTENT_PURPOSE,
        STATICANALYSIS_NO_STACKTRACES,
        STATICANALYSIS_NO_PURPOSES,
        STATICANALYSIS_NO_DEVELOPER_SPECIFIED_PURPOSE,
        POLICYPROFILE_NOT_ACTIVE,
        POLICYPROFILE_ENFORCED_POLICY,
        POLICYPROFILE_DENIED,
        POLICYPROFILE_YIELDS,
        QUICKSETTING_DISABLED,
        USERSETTING_ALLOWED,
        USERSETTING_DENIED,
        USERSETTING_ASK,
        TERMINATED
    }
}