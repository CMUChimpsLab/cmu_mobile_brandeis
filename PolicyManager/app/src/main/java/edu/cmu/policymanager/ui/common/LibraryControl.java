package edu.cmu.policymanager.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Observer;
import java.util.function.Function;

import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.common.datastructures.ConsistentStateTree;
import edu.cmu.policymanager.ui.common.functions.UIFunctions;
import edu.cmu.policymanager.util.PolicyManagerDebug;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Controls a user policy for libraries that send data to a third party,
 * such as an advertising library.
 *
 * Created by Mike Czapik (Carnegie Mellon University)
 * */
public class LibraryControl {
    private final ConfigureSwitch mControl;
    private UserPolicy mPolicy;

    private LibraryControl(Builder config) {
        View card = LayoutInflater.from(config.mContext)
                                  .inflate(R.layout.component_library_control,
                                           config.mContainer,
                                           false);

        mControl = card.findViewById(R.id.library_control_switch);

        TextView title = card.findViewById(R.id.library_control_title),
                 description = card.findViewById(R.id.library_control_description);

        ImageView icon = card.findViewById(R.id.library_control_icon);

        ThirdPartyLibrary library = config.mPolicy.thirdPartyLibrary;

        if(library == null) {
            String errorString = "Cannot render this control - no such library ";
            throw new IllegalStateException(errorString);
        }

        title.setText(library.name);
        description.setText(library.description);

        PolicyManagerApplication.ui
                                .getIconManager()
                                .getPurposeIcon(config.mPolicy.thirdPartyLibrary.purpose)
                                .addIconToView(icon);

        mControl.setPolicy(config.mPolicy);
        mPolicy = config.mPolicy;
        ConsistentStateTree controlTree = new ConsistentStateTree(mControl);

        if(config.mStateTree != null) {
            config.mStateTree.addChild(controlTree);
        }

        PolicyManager.getInstance()
                     .requestEnforcedPolicy(config.mPolicy)
                     .exceptionally(disableControlOnError(mControl))
                     .thenAccept(UIFunctions.setThumbOnPolicy(mControl));

        config.mContainer.addView(card);
    }

    private Function<Throwable, UserPolicy> disableControlOnError(final ConfigureSwitch control) {
        return new Function<Throwable, UserPolicy>() {
            @Override
            public UserPolicy apply(Throwable throwable) {
                PolicyManagerDebug.logException(throwable);
                control.disabledByError();
                return mPolicy;
            }
        };
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(Context context) {
        return new Builder(context);
    }

    /**
     * Builder class for LibraryControls.
     * */
    public static class Builder {
        private final Context mContext;
        private UserPolicy mPolicy;
        private ViewGroup mContainer;
        private ConsistentStateTree mStateTree;

        private Builder(Context context) { mContext = context; }

        /**
         * Sets the user policy to render this control for.
         *
         * @param policy the policy to render a control for
         * @return this Builder instance
         * */
        public Builder setPolicy(UserPolicy policy) {
            Precondition.checkIfNull(policy, "Cannot set null policy");
            Precondition.checkIfPolicyIsValid(policy);

            mPolicy = policy;
            return this;
        }

        /**
         * Observe the state of some master policy control, and sync
         * state with that control.
         *
         * @param stateTree the control to observe
         * @return this Builder instance
         * */
        public Builder observe(ConsistentStateTree stateTree) {
            Precondition.checkIfNull(stateTree, "Cannot observe null control");

            mStateTree = stateTree;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the LibraryControl instance this Builder created
         * */
        public LibraryControl attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");
            Precondition.checkState(mPolicy != null, "Must set a policy");

            mContainer = container;
            return new LibraryControl(this);
        }
    }
}