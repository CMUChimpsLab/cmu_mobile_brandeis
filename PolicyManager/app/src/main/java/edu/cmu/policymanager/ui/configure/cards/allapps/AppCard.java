package edu.cmu.policymanager.ui.configure.cards.allapps;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.ActivityAppSettings;
import edu.cmu.policymanager.validation.Precondition;
import edu.cmu.policymanager.viewmodel.W4PData;

/**
 * Displays an app as a card that when pressed, allows you to configure its
 * privacy settings.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class AppCard {
    private AppCard(final Builder config) {
        View card = LayoutInflater.from(config.mContext)
                                  .inflate(
                                          R.layout.component_app_card,
                                          config.mContainer,
                                          false
                                  );

        String packageName = config.mApp.getAndroidSystemName();

        ImageView icon = card.findViewById(R.id.app_card_icon);
        config.mApp.getIcon().addIconToView(icon);

        TextView appName = card.findViewById(R.id.app_card_title);
        appName.setText(config.mApp.getDisplayName());

        TextView appCategory = card.findViewById(R.id.app_card_category);

        appCategory.setText(R.string.peandroid_app);

        card.setOnClickListener(navigateToAppSettings(config.mContext, packageName));

        config.mContainer.addView(card);
    }

    private View.OnClickListener navigateToAppSettings(final Context context,
                                                       final String packageName) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appSettings = new Intent(context, ActivityAppSettings.class);
                appSettings.putExtra(ActivityAppSettings.INTENT_KEY_PACKAGE_NAME, packageName);
                context.startActivity(appSettings);
            }
        };
    }

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
     * Builder class for AppCard.
     * */
    public static class Builder {
        private final Context mContext;
        private W4PData mApp;
        private ViewGroup mContainer;

        private Builder(final Context context) { mContext = context; }

        /**
         * Set the app this card should render
         *
         * @param app the app as a W4PData
         * @return the Builder instance
         * */
        public Builder setApp(final W4PData app) {
            Precondition.checkIfNull(app, "Cannot set null app");

            if(!app.isWho()) {
                throw new IllegalArgumentException("Provided a non-app W4PData.");
            }

            mApp = app;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the AppCard instance this Builder created
         * */
        public AppCard attachTo(final ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");
            Precondition.checkState(mApp != null, "Must have app to display");

            mContainer = container;
            return new AppCard(this);
        }
    }
}