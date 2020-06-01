package edu.cmu.policymanager.ui.configure.cards.homescreen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.ActivityAllApps;
import edu.cmu.policymanager.validation.Precondition;
import edu.cmu.policymanager.viewmodel.W4PData;
import edu.cmu.policymanager.viewmodel.W4PGraph;

/**
 * Displays up to four apps that belong to a category, and will filter for that category
 * on the following screen when this category card is pressed.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 8/23/2018.
 */

public class CategoryCard {
    private ImageView mIconOne, mIconTwo, mIconThree, mIconFour;

    private CategoryCard(Builder config) {
        View layout = LayoutInflater.from(config.mContext)
                                    .inflate(
                                            R.layout.component_card_app_category,
                                            config.mContainer,
                                            false
                                    );

        mIconOne = layout.findViewById(R.id.allysiqi_category_app_top_left);
        mIconTwo = layout.findViewById(R.id.allysiqi_category_app_top_right);
        mIconThree = layout.findViewById(R.id.allysiqi_category_app_bottom_left);
        mIconFour = layout.findViewById(R.id.allysiqi_category_app_bottom_right);

        TextView categoryText = layout.findViewById(R.id.category_card_app_category),
                 count = layout.findViewById(R.id.category_card_app_count);

        setAppsText(config.mApps, count);
        addAppIcons(config.mApps);
        categoryText.setText(config.mCategory);

        layout.setOnClickListener(new ViewAppCategories(config.mContext, config.mCategory));

        config.mContainer.addView(layout);
    }

    private void setAppsText(List<W4PData> apps,
                             TextView count) {
        String cardText = "";
        int appsCount = apps.size();

        if(appsCount == 1) {
            cardText = appsCount + " App";
        } else {
            cardText = appsCount + " Apps";
        }

        count.setText(cardText);
    }

    private void addAppIcons(List<W4PData> apps) {
        int i = 0;

        for(W4PData app : apps) {
            if(i == 0) { app.getIcon().addIconToView(mIconOne); }
            if(i == 1) { app.getIcon().addIconToView(mIconTwo); }
            if(i == 2) { app.getIcon().addIconToView(mIconThree); }
            if(i == 3) { app.getIcon().addIconToView(mIconFour); }

            i++;
        }
    }

    private class ViewAppCategories implements View.OnClickListener {
        private String mCategory;
        private Context context;

        public ViewAppCategories(final Context context,
                                 final String category) {
            this.context = context;
            mCategory = category;
        }

        public void onClick(View v) {
            String selectedFilter = ActivityAllApps.SELECTED_ALL_APPS;

            if(mCategory.equalsIgnoreCase("Recently Installed")) {
                selectedFilter = ActivityAllApps.SELECTED_RECENTLY_INSTALLED_APPS;
            }

            Intent i = new Intent(context, ActivityAllApps.class);
            i.putExtra(
                    ActivityAllApps.INTENT_KEY_SELECTED_FILTER,
                    selectedFilter
            );

            context.startActivity(i);
        }
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(Context context) {
        Precondition.checkIfNull(context, "Cannot create component with null context");

        return new Builder(context);
    }

    /**
     * Builder class for CategoryCard.
     * */
    public static class Builder {
        private Context mContext;
        private String mCategory;
        private ViewGroup mContainer;
        private List<W4PData> mApps;

        private Builder(Context context) { mContext = context; }

        /**
         * Set the app category this card groups apps for.
         *
         * @param category the name of the app category
         * @return the Builder instance
         * */
        public Builder setCategory(CharSequence category) {
            Precondition.checkEmptyCharSequence(category);

            mCategory = category.toString();
            return this;
        }

        /**
         * Set the list of apps that belong to this category.
         *
         * @param apps the list of apps as W4PData
         * @return the Builder instance
         * */
        public Builder setApps(List<W4PData> apps) {
            Precondition.checkIfNull(apps, "Cannot have empty apps for card");

            mApps = apps;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the CategoryCard instance this Builder created
         * */
        public CategoryCard attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");
            Precondition.checkState(
                    mCategory != null, "Must set a category"
            );
            Precondition.checkState(
                    mApps != null, "Must set apps for category card"
            );

            mContainer = container;

            return new CategoryCard(this);
        }
    }
}