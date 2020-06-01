package edu.cmu.policymanager.PolicyManager.enforcement;

public class AllowPolicy extends PolicyEnforcementDecorator {
    public AllowPolicy(final PolicyEnforcement policyEnforcer) {
        super(policyEnforcer);
    }

    @Override
    public EnforcementStatus.Code isAllowed() { return EnforcementStatus.Code.SUCCESS; }
}
