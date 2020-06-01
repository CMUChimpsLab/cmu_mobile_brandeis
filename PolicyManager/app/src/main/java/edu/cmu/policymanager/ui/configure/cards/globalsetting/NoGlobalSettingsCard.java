package edu.cmu.policymanager.ui.configure.cards.globalsetting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.cmu.policymanager.R;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Notifies the user that there are no controls for this activity to display.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class NoGlobalSettingsCard {
    private View mCard;

    private NoGlobalSettingsCard(final Builder config) {
        mCard = LayoutInflater.from(config.mContext)
                              .inflate(
                                      R.layout.component_no_global_requested_permission,
                                      config.mContainer,
                                      false
                              );

        String title = config.mContext
                .getResources()
                .getString(
                        R.string.no_data_access_card_title,
                        config.mPermissionName
                );

        TextView titleDisplay = mCard.findViewById(R.id.no_global_data_access_card_title);
        titleDisplay.setText(title);

        String description = config.mContext
                .getResources()
                .getString(
                        R.string.no_global_data_access_card_description,
                        config.mPermissionName
                );

        TextView descriptionDisplay =
                mCard.findViewById(R.id.no_global_data_access_card_description);
        descriptionDisplay.setText(description);

        config.mContainer.addView(mCard);
    }

    /**
     * Set this card's visibility to View.GONE
     * */
    public void remove() { mCard.setVisibility(View.GONE); }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(final Context context) {
        Precondition.checkIfNull(context, "Cannot create builder from null context");
        return new Builder(context);
    }

    /**
     * Builder class for NoGlobalSettingsCard.
     * */
    public static class Builder {
        private final Context mContext;
        private CharSequence mPermissionName;
        private ViewGroup mContainer;

        private Builder(final Context context) { mContext = context; }

        /**
         * Sets the permission for which there are no global setting controls
         *
         * @param permissionName the permission there are no controls for
         * @return the Builder instance
         * */
        public Builder setPermissionName(final CharSequence permissionName) {
            Precondition.checkEmptyCharSequence(permissionName);

            mPermissionName = permissionName;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the NoGlobalSettingsCard instance this Builder created
         * */
        public NoGlobalSettingsCard attachTo(final ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");

            mContainer = container;
            return new NoGlobalSettingsCard(this);
        }
    }
}