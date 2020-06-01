package edu.cmu.policymanager.ui.common.functions;

import java.util.function.Consumer;

import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.ui.common.ConfigureSwitch;

/**
 * Provides a set of functions for handling common UI tasks.
 * */
public class UIFunctions {
    private static final String sErrorControlNull = "Cannot visualize null policy controls.";

    private static final long sMeasureDelay = 150L;

    /**
     * Sets the thumb for the master policy control. If no policy exists
     * for this user policy tuple, then place the control switch thumb on the given UserPolicy.
     *
     * @param masterPolicy the policy to query for if present, otherwise set the control to this
     *                     policy.
     * @param masterControl the control to adjust the thumb for.
     * @return the Consumer object to feed into the async PolicyManager policy request.
     * */
    public static Consumer<UserPolicy> setThumbOnMasterPolicy(final UserPolicy masterPolicy,
                                                              final ConfigureSwitch masterControl) {
        if(masterControl == null) {
            throw new IllegalArgumentException(sErrorControlNull);
        }

        return new Consumer<UserPolicy>() {
            @Override
            public void accept(UserPolicy userPolicy) {
                UserPolicy policyInEffect = masterPolicy;

                if(userPolicy != null) { policyInEffect = userPolicy; }

                masterControl.postDelayed(
                        new VisualizePolicyControl(policyInEffect, masterControl),
                        sMeasureDelay
                );
            }
        };
    }

    /**
     * Sets the thumb for the master policy control. If no policy exists
     * for this user policy tuple, then place the control switch thumb on the given UserPolicy.
     *
     * @param control the control to adjust the thumb for.
     * @return the Consumer object to feed into the async PolicyManager policy request.
     * */
    public static Consumer<UserPolicy> setThumbOnPolicy(final ConfigureSwitch control) {
        if(control == null) {
            throw new IllegalArgumentException(sErrorControlNull);
        }

        return new Consumer<UserPolicy>() {
            @Override
            public void accept(UserPolicy userPolicy) {
                control.postDelayed(
                        new VisualizePolicyControl(userPolicy, control),
                        sMeasureDelay
                );
            }
        };
    }
}