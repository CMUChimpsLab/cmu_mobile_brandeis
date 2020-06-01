package edu.cmu.policymanager.ui.common.functions;

import android.os.Looper;
import android.util.Log;

import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.ui.common.ConfigureSwitch;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Place the policy switch thumb over the policy that is being enforced
 * in the given UserPolicy object. Typically we don't want to overwrite the policy
 * the provided switch controls, just want to show the policy currently in effect.
 * */
public class VisualizePolicyControl implements Runnable {
    private final UserPolicy mPolicy;
    private final ConfigureSwitch mControl;

    /**
     * Creates a new function to project a given UserPolicy object policy action onto a
     * ConfigureSwitch policy control widget.
     * */
    VisualizePolicyControl(UserPolicy policy,
                           ConfigureSwitch control) {
        Precondition.checkIfNull(policy, "Cannot have null policy");
        Precondition.checkIfNull(control, "Cannot have null control");

        mPolicy = policy;
        mControl = control;
    }

    @Override
    public void run() {
        mControl.putThumbOnPolicy(mPolicy);
    }
}