package edu.cmu.policymanager.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.cmu.policymanager.R;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Displays error message to a user that policy controls for a specific user policy
 * cannot be displayed.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PolicyControlErrorCard {
    private PolicyControlErrorCard(Builder config) {
        View card = LayoutInflater.from(config.mContext)
                                  .inflate(
                                          R.layout.component_policy_control_error_card,
                                          config.mContainer,
                                          false
                                  );

        config.mContainer.addView(card);
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(Context context) {
        Precondition.checkIfNull(context, "Cannot create builder from null context");
        return new Builder(context);
    }

    /**
     * Builder class for PolicyControlErrorCard
     * */
    public static class Builder {
        private final Context mContext;
        private ViewGroup mContainer;

        private Builder(Context context) { mContext = context; }

        /**
         * Attach this card to the given container.
         *
         * @param container the container to attach the card to
         * @return the PolicyControlErrorCard
         * */
        public PolicyControlErrorCard attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");
            mContainer = container;
            return new PolicyControlErrorCard(this);
        }
    }
}