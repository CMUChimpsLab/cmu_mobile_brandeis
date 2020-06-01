package edu.cmu.policymanager.ui.configure.cards.appsetting;

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
public class NoSettingsCard {
    private static final CharSequence CATEGORY_APP_INTERNAL_USE = "App Internal Use",
                                      CATEGORY_THIRD_PARTY_USE = "Third Party Use";

    private NoSettingsCard(Builder config) {
        View card = LayoutInflater.from(config.mContext)
                                  .inflate(
                                          R.layout.component_no_requested_permission,
                                          config.mContainer,
                                          false
                                  );

        String title = config.mContext
                             .getResources()
                             .getString(
                                     R.string.no_data_access_card_title,
                                     config.mPermissionCategory
                             );

        TextView titleDisplay = card.findViewById(R.id.no_data_access_card_title);
        titleDisplay.setText(title);

        int descriptionId = R.string.no_data_access_card_description_internal;

        if(config.mPermissionCategory.equals(CATEGORY_THIRD_PARTY_USE)) {
            descriptionId = R.string.no_data_access_card_description_library;
        }

        String description = config.mContext
                                   .getResources()
                                   .getString(
                                           descriptionId,
                                           config.mAppName
                                   );

        TextView descriptionDisplay = card.findViewById(R.id.no_data_access_card_description);
        descriptionDisplay.setText(description);

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
     * Builder class for NoSettingsCard.
     * */
    public static class Builder {
        private final Context mContext;
        private CharSequence mAppName, mPermissionCategory;
        private ViewGroup mContainer;

        private Builder(Context context) { mContext = context; }

        /**
         * Set the name of the app
         *
         * @param appName the name of the app
         * @return the Builder instance
         * */
        public Builder setAppName(CharSequence appName) {
            Precondition.checkEmptyCharSequence(appName);

            mAppName = appName;
            return this;
        }

        /**
         * Change the display of the card to indicate that there are no app internal
         * settings to configure.
         *
         * @return the Builder instance
         * */
        public Builder isAppInternalCategory() {
            mPermissionCategory = CATEGORY_APP_INTERNAL_USE;
            return this;
        }

        /**
         * Change the display of the card to indicate that there are no third-party library
         * settings to configure.
         *
         * @return the Builder instance
         * */
        public Builder isThirdPartyCategory() {
            mPermissionCategory = CATEGORY_THIRD_PARTY_USE;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the NoSettingsCard instance this Builder created
         * */
        public NoSettingsCard attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");

            if(mPermissionCategory == null || mPermissionCategory.length() == 0) {
                throw new IllegalStateException("Must set category to create this component");
            }

            mContainer = container;
            return new NoSettingsCard(this);
        }
    }
}